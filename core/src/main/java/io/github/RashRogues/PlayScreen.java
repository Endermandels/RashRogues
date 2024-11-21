package io.github.RashRogues;

import Networking.Network;
import UI.Button;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class PlayScreen extends ScreenAdapter implements RRScreen {

    private boolean debug = false;

    private RRGame game;
    private HUD hud;
    private Room currentRoom;
    private Player player;
    private Door currentDoor;
    private ArrayList<Room> rooms;
    private HashSet<Entity> localEntities;
    private PriorityQueue<Entity> renderQueue;
    public static CollisionGrid collisionGrid = new CollisionGrid();

    public PlayScreen(RRGame game) {
        /* Initialization */
        RRGame.globals.currentScreen = this;
        this.game = game;
        this.localEntities  = new HashSet<>();
        this.renderQueue    = new PriorityQueue<>(new EntityComparator());
        loadRooms();
        setNextRoom();
        createHUDAndInputs();

        /* Instance Creation */
        new Swordsman(RRGame.am.get(RRGame.RSC_SWORDSMAN_IMG), 50, 30, 10);
        player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG), RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, RRGame.PLAYER_SIZE);
        new Key(30, 280);

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

        for ( Entity e : localEntities ){
            e.update(delta);
            renderQueue.add(e);
        }
        game.playerCam.update(delta);


        // check/handle collisions
        collisionGrid.populateCollisionGrid(localEntities);
        collisionGrid.calculateCollisions();

        // determine if all the players are at the door to progress to the next room
        // the door kill itself when it's ready to move on, so we just need to check:
        if (!localEntities.contains(currentDoor)) { setNextRoom(); }
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

        for (Entity e : localEntities) {

            // hitBoxes - red
            game.shapeRenderer.setColor(new Color(1, 0, 0, 0.4f));
            // draw the hitBoxes
            drawHitBox(e.hitBox);

            // hurtBoxes - green
            game.shapeRenderer.setColor(new Color(0, 1, 0, 0.4f));
            // populate the hurtBoxes if enemy or player
            if (e instanceof Player) {
                Player player = (Player) e;
                drawHurtBox(player.hurtBox);
            }
            else if (e instanceof Enemy) {
                Enemy enemy = (Enemy) e;
                drawHurtBox(enemy.hurtBox);
            }
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
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM1_IMG), 35, 301));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM2_IMG), 35, 301));
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
        HashSet<Entity> tempLocalEntities = new HashSet<>();
        for (Entity e : localEntities) {
            if (e instanceof Player) {
                Player player = (Player) e;
                player.resetForNewRoom();
                tempLocalEntities.add(e);
            }
        }
        localEntities = tempLocalEntities;
        currentDoor = new Door(currentRoom.doorPositionX, currentRoom.doorPositionY);
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

        hud.registerAction("tp", new HUDActionCommand() {
            static final String help = "Teleport to a specific location. Usage: tp <x> <y> ";
            @Override
            public String execute(String[] cmd) {
                try {
                    int x = Integer.parseInt(cmd[1]);
                    int y = Integer.parseInt(cmd[2]);
                    if (x < 0 || x > currentRoom.roomWidth-player.getWidth() || y < 0 ||
                            y > currentRoom.roomHeight-player.getHeight()) return "Cannot tp out of bounds";
                    player.setPosition(x, y);
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
        hud.registerView("Number of Players:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(RRGame.globals.currentNumPlayers);
            }
        });

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
                    // cancel any selections
                }
                if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                    player.moveLeft(true);
                }
                if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                    player.moveRight(true);
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    player.moveDown(true);
                }
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    player.moveUp(true);
                }
                if (keycode == Input.Keys.SPACE) {
                    player.dash();
                }
                if (keycode == Input.Keys.E) {
                    player.useAbility();
                }
                if (keycode == Input.Keys.Q) {
                    player.useConsumable();
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
