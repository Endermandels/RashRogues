package io.github.RashRogues;

import Networking.Network;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class PlayScreen extends ScreenAdapter implements RRScreen {

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
    public static CollisionGrid collisionGrid = new CollisionGrid();

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
        createHUDAndInputs();

        /* Instance Creation */
        new Swordsman(game.am.get(RRGame.RSC_SWORDSMAN_IMG), 50, 30, 10);
        player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, RRGame.PLAYER_SIZE);

        this.game.network.connection.dispatchCreatePlayer((int) player.getX(), (int) player.getY());

        /* Camera Setup */
        game.playerCam.bind(player);
        game.playerCam.center();
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {
        game.network.connection.processMessages();

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
        collisionGrid.populateCollisionGrid(localEntities);
        collisionGrid.calculateCollisions();
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
        collisionGrid.updateCollisionGridRoomValues(currentRoom.roomWidth, currentRoom.roomHeight);
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
                byte[] inputMask = new byte[8];
                if (keycode == Input.Keys.ESCAPE) {
                    // cancel any selections
                }
                if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                    player.moveLeft(true);
                    inputMask[0] = (byte) 1;
                }
                if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                    player.moveRight(true);
                    inputMask[1] = (byte) 1;
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    player.moveDown(true);
                    inputMask[2] = (byte) 1;
                }
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    player.moveUp(true);
                    inputMask[3] = (byte) 1;
                }
                if (keycode == Input.Keys.SPACE) {
                    player.dash();
                    inputMask[4] = (byte) 1;
                }
                if (keycode == Input.Keys.E) {
                    player.useAbility();
                    inputMask[5] = (byte) 1;
                }
                if (keycode == Input.Keys.Q) {
                    player.useConsumable();
                    inputMask[6] = (byte) 1;
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                    player.moveLeft(false);
                }
                if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                    player.moveRight(false);
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    player.moveDown(false);
                }
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    player.moveUp(false);
                }
                return true;
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
        System.out.println("registyerd");
        this.localEntities.add(entity);
    }

    /**
     * This is called any time a class inheriting Entity is removed from this Screen.
     * See the Entity class constructor for more information.
     * @param entity A Locally Instantiated Entity
     */
    public void removeEntity(Entity entity) {
        this.localEntities.remove(entity);
    }
}
