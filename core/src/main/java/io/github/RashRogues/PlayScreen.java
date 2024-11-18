package io.github.RashRogues;

import Networking.Network;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class PlayScreen extends ScreenAdapter implements Screen {

    private boolean debug = false;

    private RRGame game;
    private HUD hud;
    private Room currentRoom;
    private Player player;
    private ArrayList<Room> rooms;
    private HashSet<Entity> localEntities;
    private PriorityQueue<Entity> renderQueue;
    private ArrayList<Projectile> debugProjectileRenderList;
    private ArrayList<Player> debugPlayerRenderList;
    private ArrayList<Enemy> debugEnemyRenderList;
    private final int COLLISION_GRID_ROWS = 32;
    private final int COLLISION_GRID_COLS = 16;
    private int collisionGridRowSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_ROWS); // these change immediately
    private int collisionGridColSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_COLS);
    private LinkedHashSet<HitBox>[][] collisionGridHitBoxes;
    private LinkedHashSet<HurtBox>[][] collisionGridHurtBoxes;

    public PlayScreen(RRGame game) {
        /* Initialization */
        RRGame.globals.currentScreen = this;
        this.game = game;
        this.localEntities  = new HashSet<>();
        this.renderQueue    = new PriorityQueue<>(new EntityComparator());
        this.debugProjectileRenderList = new ArrayList<>();
        this.debugPlayerRenderList = new ArrayList<>();
        this.debugEnemyRenderList = new ArrayList<>();
        loadRooms();
        setNextRoom();
        //createCollisionGrids();
        createHUDAndInputs();

        /* Instance Creation */
        new Swordsman(game.am.get(RRGame.RSC_SWORDSMAN_IMG), 50, 30, 10);
        player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, RRGame.PLAYER_SIZE);

        /* Camera Setup */
        game.playerCam.bind(player);
        game.playerCam.center();

        if (game.network.type == Network.EndpointType.SERVER){
            game.network.connection.dispatchCreate(player);
        }
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {
        game.network.connection.processMessages();
        if (game.network.type == Network.EndpointType.SERVER){
            game.network.connection.dispatchUpdate(this.player);
        }

        if (debug) {
            debugProjectileRenderList.clear();
            debugPlayerRenderList.clear();
            debugEnemyRenderList.clear();
        }

        for ( Entity e : localEntities ){
            e.update(delta);
            renderQueue.add(e);
            if (debug) {
                if (e instanceof Projectile) { debugProjectileRenderList.add((Projectile) e); }
                else if (e instanceof Player) { debugPlayerRenderList.add((Player) e); }
                else if (e instanceof Enemy) { debugEnemyRenderList.add((Enemy) e); }
            }
        }
        game.playerCam.update(delta);


        // check/handle collisions
        // populateCollisionGrids();
        // calculateCollisions();
    }

    @Override
    public void render(float delta) {

        /* Update Instances and Enqueue for rendering */
        update(delta);

        /* Update Camera Position */
        game.playerCam.update();
        game.batch.setProjectionMatrix(game.playerCam.combined);

        /* Render Background and Instances */
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1f);
        game.batch.begin();
        currentRoom.draw(game.batch);
        while (!renderQueue.isEmpty()){
            renderQueue.poll().draw(game.batch);
        }
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
        for (Player player : debugPlayerRenderList) {
            drawHurtBox(player.hurtBox);
        }
        for (Enemy enemy : debugEnemyRenderList) {
            drawHurtBox(enemy.hurtBox);
        }

        // hitBoxes - red
        game.shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
        for (Player player : debugPlayerRenderList) {
            drawHitBox(player.hitBox);
        }
        for (Enemy enemy : debugEnemyRenderList) {
            drawHitBox(enemy.hitBox);
        }
        for (Projectile projectile : debugProjectileRenderList) {
            drawHitBox(projectile.hitBox);
        }

        game.shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        hud.resize(width, height, game);
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

    //TODO: make it work with new render/entity organization system.
    private void populateCollisionGrids() {
        // first, clear the grid
//        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
//            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
//                collisionGridHitBoxes[i][j] = new LinkedHashSet<HitBox>();
//                collisionGridHurtBoxes[i][j] = new LinkedHashSet<HurtBox>();
//            }
//        }
//
//        // place player hit/hurt boxes in the correct boxes
//        // this needs to be updated for multiple players
//        int leftSideColNum = clampedCollisionColNum((int) (player.hitBox.x) / collisionGridColSize);
//        int rightSideColNum = clampedCollisionColNum((int) (player.hitBox.x + player.hitBox.width) / collisionGridColSize);
//        int bottomSideRowNum = clampedCollisionRowNum((int) (player.hitBox.y) / collisionGridRowSize);
//        int topSideRowNum = clampedCollisionRowNum((int) (player.hitBox.y + player.hitBox.height) / collisionGridRowSize);
//        collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(player.hitBox);
//        collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(player.hitBox);
//        collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(player.hitBox);
//        collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(player.hitBox);
//
//        leftSideColNum = clampedCollisionColNum((int) (player.hurtBox.x) / collisionGridColSize);
//        rightSideColNum = clampedCollisionColNum((int) (player.hurtBox.x + player.hurtBox.width) / collisionGridColSize);
//        bottomSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y) / collisionGridRowSize);
//        topSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y + player.hurtBox.height) / collisionGridRowSize);
//        collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(player.hurtBox);
//        collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(player.hurtBox);
//        collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(player.hurtBox);
//        collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(player.hurtBox);
//
//        // place enemy hit/hurt boxes in the correct boxes
//        for (Enemy enemy : enemies) {
//            // this logic reduces the need for if statements at no cost by using hashSet
//            // hitboxes
//            leftSideColNum = clampedCollisionColNum((int) (enemy.hitBox.x) / collisionGridColSize);
//            rightSideColNum = clampedCollisionColNum((int) (enemy.hitBox.x + enemy.hitBox.width) / collisionGridColSize);
//            bottomSideRowNum = clampedCollisionRowNum((int) (enemy.hitBox.y) / collisionGridRowSize);
//            topSideRowNum = clampedCollisionRowNum((int) (enemy.hitBox.y + enemy.hitBox.height) / collisionGridRowSize);
//            collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(enemy.hitBox);
//            collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(enemy.hitBox);
//            collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(enemy.hitBox);
//            collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(enemy.hitBox);
//
//            // hurtboxes
//            leftSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x) / collisionGridColSize);
//            rightSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x + enemy.hurtBox.width) / collisionGridColSize);
//            bottomSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y) / collisionGridRowSize);
//            topSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y + enemy.hurtBox.height) / collisionGridRowSize);
//            collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(enemy.hurtBox);
//            collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(enemy.hurtBox);
//            collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(enemy.hurtBox);
//            collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(enemy.hurtBox);
//        }
//
//        // place projectile hitboxes in the correct boxes
//        for (Projectile projectile : projectiles) {
//            // hitboxes
//            leftSideColNum = clampedCollisionColNum((int) (projectile.hitBox.x) / collisionGridColSize);
//            rightSideColNum = clampedCollisionColNum((int) (projectile.hitBox.x + projectile.hitBox.width) / collisionGridColSize);
//            bottomSideRowNum = clampedCollisionRowNum((int) (projectile.hitBox.y) / collisionGridRowSize);
//            topSideRowNum = clampedCollisionRowNum((int) (projectile.hitBox.y + projectile.hitBox.height) / collisionGridRowSize);
//            collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(projectile.hitBox);
//            collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(projectile.hitBox);
//            collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(projectile.hitBox);
//            collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(projectile.hitBox);
//        }
    }

    private int clampedCollisionColNum(int col) {
        return Math.max(0, Math.min(COLLISION_GRID_COLS-1, col));
    }

    private int clampedCollisionRowNum(int row) {
        return Math.max(0, Math.min(COLLISION_GRID_ROWS-1, row));
    }

    //TODO: make it work with new render/entity organization system.
    private void calculateCollisions() {
        // absolutely horrid looking nested for loops, but it has to be done and this is better than O(n*m)
        // more hitboxes than hurtboxes, so calculate if each hitbox hits a hurtbox
//        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
//            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
//                LinkedHashSet<HitBox> hitBoxes = collisionGridHitBoxes[i][j];
//                LinkedHashSet<HurtBox> hurtBoxes = collisionGridHurtBoxes[i][j];
//                for (Iterator<HitBox> hitBoxIterator = hitBoxes.iterator(); hitBoxIterator.hasNext();) {
//                    HitBox hitBox = hitBoxIterator.next();
//                    for (Iterator<HurtBox> hurtBoxIterator = hurtBoxes.iterator(); hurtBoxIterator.hasNext();) {
//                        HurtBox hurtBox = hurtBoxIterator.next();
//                        if (hitBox.overlaps(hurtBox)) {
//                            hitBox.hitHurtBox(hurtBox);
//                        }
//                    }
//                }
//            }
//        }
    }

    public void createHUDAndInputs() {
        hud = new HUD(RRGame.am.get(RRGame.RSC_MONO_FONT));

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.ALWAYS);

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

    public void drawHurtBox(HurtBox hurtBox) {
        game.shapeRenderer.rect(hurtBox.getX(), hurtBox.getY(), hurtBox.getWidth(), hurtBox.getHeight());
    }

    public void drawHitBox(HitBox hitBox) {
        game.shapeRenderer.rect(hitBox.getX(), hitBox.getY(), hitBox.getWidth(), hitBox.getHeight());
    }

    public void nextScreen() {return;}

    /**
     * This is called every time a class inheriting Entity is instantiated on this Screen.
     * See the Entity class constructor for more information.
     * @param entity A Locally Instantiated Entity
     */
    public void registerEntity(Entity entity) {
        this.localEntities.add(entity);
    }
}
