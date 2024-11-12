package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PlayScreen extends ScreenAdapter {

    private RRGame game;
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Room> rooms;
    private Room currentRoom;
    private final int COLLISION_GRID_ROWS = 32;
    private final int COLLISION_GRID_COLS = 16;
    private int collisionGridRowSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_ROWS); // these change immediately
    private int collisionGridColSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_COLS);
    private LinkedHashSet<HitBox>[][] collisionGridHitBoxes;
    private LinkedHashSet<HurtBox>[][] collisionGridHurtBoxes;

    public PlayScreen(RRGame game) {
        this.game = game;
        this.player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, RRGame.PLAYER_SIZE);
        this.enemies = new ArrayList<Enemy>();
        Swordsman swordsman = new Swordsman(game.am.get(RRGame.RSC_SWORDSMAN_IMG), 50, 30, 10);
        enemies.add(swordsman);
        this.projectiles = new ArrayList<Projectile>();
        loadRooms();
        setNextRoom();
        createCollisionGrids();
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {

        // update room/objects

        // update player(s)
        player.takeInput();
        player.update(delta);
        game.playerCam.moveToPlayer(player.getX()+player.getWidth()/2f, player.getY()+player.getHeight()/2f, delta);

        // update enemies
        for (Iterator<Enemy> enemyIterator = enemies.iterator(); enemyIterator.hasNext();) {
            Enemy enemy = enemyIterator.next();
            enemy.update(delta);
            if (enemy.stats.isDead()) { enemyIterator.remove(); }
        }

        // update projectiles
        for (Iterator<Projectile> projectileIterator = projectiles.iterator(); projectileIterator.hasNext();) {
            Projectile projectile = projectileIterator.next();
            projectile.update(delta);
            if (projectile.removeNextUpdate) { projectileIterator.remove(); }
        }

        // check/handle collisions
        populateCollisionGrids();
        calculateCollisions();

        // update anything else
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.playerCam.update();
        game.batch.setProjectionMatrix(game.playerCam.combined);
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.batch.begin();

        // draw room/objects
        currentRoom.draw(game.batch);

        // draw player(s)
        player.draw(game.batch);

        // draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(game.batch);
        }

        // draw projectiles
        for (Projectile projectile : projectiles) {
            projectile.draw(game.batch);
        }

        // draw misc

        game.batch.end();

        // this is for debugging hitboxes, once hud is added this will be cleaner
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.shapeRenderer.setProjectionMatrix(game.playerCam.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(new Color(0, 1, 0, 0.5f));
        game.shapeRenderer.rect(player.hurtBox.getX(), player.hurtBox.getY(), player.hurtBox.getWidth(), player.hurtBox.getHeight());
        game.shapeRenderer.rect(enemies.get(0).hurtBox.getX(), enemies.get(0).hurtBox.getY(), enemies.get(0).hurtBox.getWidth(), enemies.get(0).hurtBox.getHeight());
        game.shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
        game.shapeRenderer.rect(player.hitBox.getX(), player.hitBox.getY(), player.hitBox.getWidth(), player.hitBox.getHeight());
        game.shapeRenderer.rect(enemies.get(0).hitBox.getX(), enemies.get(0).hitBox.getY(), enemies.get(0).hitBox.getWidth(), enemies.get(0).hitBox.getHeight());
        game.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void loadRooms() {
        this.rooms = new ArrayList<>();
        rooms.add(new Room(game.am.get(RRGame.RSC_ROOM1_IMG)));
        // other rooms will go below here
    }

    private void setNextRoom() {
        if (currentRoom == null) {
            // first room
            currentRoom = rooms.get(0);
        }
        else if (rooms.indexOf(currentRoom) >= rooms.size() - 1) {
            // last room
            // win screen?
            // this will crash the game for now most likely
            return;
        }
        else {
            currentRoom = rooms.get(rooms.indexOf(currentRoom) + 1);
        }
        game.playerCam.changeWorldSize(currentRoom.roomWidth, currentRoom.roomHeight);
        collisionGridRowSize = currentRoom.roomHeight / COLLISION_GRID_ROWS;
        collisionGridColSize = currentRoom.roomWidth / COLLISION_GRID_COLS;
    }

    private void createCollisionGrids() {
        collisionGridHitBoxes = new LinkedHashSet[COLLISION_GRID_ROWS][COLLISION_GRID_COLS];
        collisionGridHurtBoxes = new LinkedHashSet[COLLISION_GRID_ROWS][COLLISION_GRID_COLS];
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                collisionGridHitBoxes[i][j] = new LinkedHashSet<HitBox>();
                collisionGridHurtBoxes[i][j] = new LinkedHashSet<HurtBox>();
            }
        }
    }

    private void populateCollisionGrids() {
        // first, clear the grid
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                collisionGridHitBoxes[i][j] = new LinkedHashSet<HitBox>();
                collisionGridHurtBoxes[i][j] = new LinkedHashSet<HurtBox>();
            }
        }

        // place player hit/hurt boxes in the correct boxes
        // this needs to be updated for multiple players
        int leftSideColNum = clampedCollisionColNum((int) (player.hitBox.x) / collisionGridColSize);
        int rightSideColNum = clampedCollisionColNum((int) (player.hitBox.x + player.hitBox.width) / collisionGridColSize);
        int bottomSideRowNum = clampedCollisionRowNum((int) (player.hitBox.y) / collisionGridRowSize);
        int topSideRowNum = clampedCollisionRowNum((int) (player.hitBox.y + player.hitBox.height) / collisionGridRowSize);
        collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(player.hitBox);
        collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(player.hitBox);
        collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(player.hitBox);
        collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(player.hitBox);

        leftSideColNum = clampedCollisionColNum((int) (player.hurtBox.x) / collisionGridColSize);
        rightSideColNum = clampedCollisionColNum((int) (player.hurtBox.x + player.hurtBox.width) / collisionGridColSize);
        bottomSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y) / collisionGridRowSize);
        topSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y + player.hurtBox.height) / collisionGridRowSize);
        collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(player.hurtBox);
        collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(player.hurtBox);
        collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(player.hurtBox);
        collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(player.hurtBox);

        // place enemy hit/hurt boxes in the correct boxes
        for (Enemy enemy : enemies) {
            // this logic reduces the need for if statements at no cost by using hashSet
            // hitboxes
            leftSideColNum = clampedCollisionColNum((int) (enemy.hitBox.x) / collisionGridColSize);
            rightSideColNum = clampedCollisionColNum((int) (enemy.hitBox.x + enemy.hitBox.width) / collisionGridColSize);
            bottomSideRowNum = clampedCollisionRowNum((int) (enemy.hitBox.y) / collisionGridRowSize);
            topSideRowNum = clampedCollisionRowNum((int) (enemy.hitBox.y + enemy.hitBox.height) / collisionGridRowSize);
            collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(enemy.hitBox);
            collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(enemy.hitBox);
            collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(enemy.hitBox);
            collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(enemy.hitBox);

            // hurtboxes
            leftSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x) / collisionGridColSize);
            rightSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x + enemy.hurtBox.width) / collisionGridColSize);
            bottomSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y) / collisionGridRowSize);
            topSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y + enemy.hurtBox.height) / collisionGridRowSize);
            collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(enemy.hurtBox);
            collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(enemy.hurtBox);
            collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(enemy.hurtBox);
            collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(enemy.hurtBox);
        }

        // place projectile hitboxes in the correct boxes
        for (Projectile projectile : projectiles) {
            // hitboxes
            leftSideColNum = clampedCollisionColNum((int) (projectile.hitBox.x) / collisionGridColSize);
            rightSideColNum = clampedCollisionColNum((int) (projectile.hitBox.x + projectile.hitBox.width) / collisionGridColSize);
            bottomSideRowNum = clampedCollisionRowNum((int) (projectile.hitBox.y) / collisionGridRowSize);
            topSideRowNum = clampedCollisionRowNum((int) (projectile.hitBox.y + projectile.hitBox.height) / collisionGridRowSize);
            collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(projectile.hitBox);
            collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(projectile.hitBox);
            collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(projectile.hitBox);
            collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(projectile.hitBox);
        }
    }

    private int clampedCollisionColNum(int col) {
        return Math.max(0, Math.min(COLLISION_GRID_COLS-1, col));
    }

    private int clampedCollisionRowNum(int row) {
        return Math.max(0, Math.min(COLLISION_GRID_ROWS-1, row));
    }

    private void calculateCollisions() {
        // absolutely horrid looking nested for loops, but it has to be done and this is better than O(n*m)
        // more hitboxes than hurtboxes, so calculate if each hitbox hits a hurtbox
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                LinkedHashSet<HitBox> hitBoxes = collisionGridHitBoxes[i][j];
                LinkedHashSet<HurtBox> hurtBoxes = collisionGridHurtBoxes[i][j];
                for (Iterator<HitBox> hitBoxIterator = hitBoxes.iterator(); hitBoxIterator.hasNext();) {
                    HitBox hitBox = hitBoxIterator.next();
                    for (Iterator<HurtBox> hurtBoxIterator = hurtBoxes.iterator(); hurtBoxIterator.hasNext();) {
                        HurtBox hurtBox = hurtBoxIterator.next();
                        if (hitBox.overlaps(hurtBox)) {
                            hitBox.hitHurtBox(hurtBox);
                        }
                    }
                }
            }
        }
    }

}
