package io.github.RashRogues;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

public class PlayScreen extends ScreenAdapter {

    private boolean debug = false;

    private RRGame game;
    private HUD hud;
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
        createHUDAndInputs();

    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {

        // update room/objects

        // update player(s)
        if (!hud.isOpen()) {
            player.takeInput();
        }
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

        game.hudBatch.begin();
        hud.draw(game.hudBatch);
        game.hudBatch.end();

        // only debugging needs the ShapeRenderer, so we can have nice formatting by having an early return condition
        if (!debug) { return; }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.shapeRenderer.setProjectionMatrix(game.playerCam.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        // hurtBoxes - green
        game.shapeRenderer.setColor(new Color(0, 1, 0, 0.5f));
        game.shapeRenderer.rect(player.hurtBox.getX(), player.hurtBox.getY(), player.hurtBox.getWidth(),
                player.hurtBox.getHeight());
        for (Enemy enemy : enemies) {
            game.shapeRenderer.rect(enemy.hurtBox.getX(), enemy.hurtBox.getY(), enemy.hurtBox.getWidth(),
                    enemy.hurtBox.getHeight());
        }

        // hitBoxes - red
        game.shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
        game.shapeRenderer.rect(player.hitBox.getX(), player.hitBox.getY(), player.hitBox.getWidth(),
                player.hitBox.getHeight());
        for (Enemy enemy : enemies) {
            game.shapeRenderer.rect(enemy.hitBox.getX(), enemy.hitBox.getY(), enemy.hitBox.getWidth(),
                    enemy.hitBox.getHeight());
        }
        for (Projectile projectile : projectiles) {
            game.shapeRenderer.rect(projectile.hitBox.getX(), projectile.hitBox.getY(), projectile.hitBox.getWidth(),
                    projectile.hitBox.getHeight());
        }

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

    public void createHUDAndInputs() {
        hud = new HUD(game.am.get(RRGame.RSC_MONO_FONT));

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands
        hud.registerAction("debug", new HUDActionCommand() {
            static final String help = "Toggle debug views on or off. Usage: debug ";
            @Override
            public String execute(String[] cmd) {
                try {
                    debug = !debug;
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // HUD Data


        // we're adding an input processor AFTER the HUD has been created,
        // so we need to be a bit careful here and make sure not to clobber
        // the HUD's input controls. Do that by using an InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // let the HUD's input processor handle things first....
        multiplexer.addProcessor(Gdx.input.getInputProcessor());
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (hud.isOpen()) { return false; }
                if (keycode == Input.Keys.ESCAPE) {
                    // there should be a way to put movement in here as well, file this under a later issue
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

}
