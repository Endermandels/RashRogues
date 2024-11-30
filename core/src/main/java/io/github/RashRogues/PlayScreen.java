package io.github.RashRogues;

import Networking.Network;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private HashSet<Entity> newlyAddedEntities;
    private HashSet<Entity> entitiesToRemove;
    private PriorityQueue<Entity> renderQueue;
    private HashMap<Integer, Boolean> inputs;
    public static CollisionGrid collisionGrid = new CollisionGrid();

    public PlayScreen(RRGame game) {

        /* Initialization */
        RRGame.globals.currentScreen = this;
        this.game = game;
        this.localEntities  = new HashSet<>();
        this.newlyAddedEntities = new HashSet<>();
        this.entitiesToRemove = new HashSet<>();
        this.renderQueue    = new PriorityQueue<>(new EntityComparator());
        initInputs();
        loadRooms();
        setNextRoom();
        createHUDAndInputs();

        /* Player Creation */
        player = new Player(RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, (int) RRGame.PLAYER_SIZE);
        this.game.network.connection.dispatchCreatePlayer(player);

        /* Instance Creation */
        new Swordsman(50, 30, 10);
        new Key(30, 280);

        /* Camera Setup */
        game.playerCam.bind(player);
        game.playerCam.center();
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    /**
     * Poll inputs
     * This method executes inputs locally, then sends them over the network.
     */
    public void getInputs() {
        byte[] keyMask = new byte[8];
        if (inputs.get(Input.Keys.UP)) {
            this.player.moveUp();
            keyMask[0] = 1;
        }
        if (inputs.get(Input.Keys.DOWN)) {
            this.player.moveDown();
            keyMask[1] = 1;
        }
        if (inputs.get(Input.Keys.RIGHT)) {
            this.player.moveRight();
            keyMask[2] = 1;
        }
        if (inputs.get(Input.Keys.LEFT)) {
            this.player.moveLeft();
            keyMask[3] = 1;
        }
        if (inputs.get(Input.Keys.SPACE)) {
            this.player.dash();
            inputs.put(Input.Keys.SPACE, false);
            keyMask[4] = 1;
        }
        if (inputs.get(Input.Keys.E)) {
            this.player.useConsumable();
            inputs.put(Input.Keys.E, false);
            keyMask[5] = 1;
        }
        if (inputs.get(Input.Keys.Q)){
            this.player.useAbility();
            inputs.put(Input.Keys.Q, false);
            keyMask[6] = 1;
        }
        game.network.connection.dispatchKeys(keyMask);
    }

    public void update(float delta) {
        /* Process Updates from the Network */
        game.network.connection.processMessages();
        getInputs();

        /* Execute Update Event For All Entities In the Room */
        for ( Entity e : localEntities ){
            e.update(delta);
            renderQueue.add(e);
        }

        game.playerCam.update(delta);

        /* check/handle collisions */
        collisionGrid.populateCollisionGrid(localEntities);
        collisionGrid.calculateCollisions();

        /* add entites that were created last frame to the render/update list */
        localEntities.addAll(newlyAddedEntities);
        newlyAddedEntities.clear();

        /* remove entities that 'died' last frame from the render/update list */
        localEntities.removeAll(entitiesToRemove);
        entitiesToRemove.clear();

        /*
         Determine if all the players are at the door to progress to the next room
         the door kill itself when it's ready to move on, so we just need to check:
        */
        if (!localEntities.contains(currentDoor)) { setNextRoom(); }
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.playerCam.update();
        game.batch.setProjectionMatrix(game.playerCam.combined);
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
        debugRender();
    }

    public void debugRender(){
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

    private void initInputs(){
        this.inputs = new HashMap<>();
        this.inputs.put(Input.Keys.RIGHT, false);
        this.inputs.put(Input.Keys.LEFT, false);
        this.inputs.put(Input.Keys.UP, false);
        this.inputs.put(Input.Keys.DOWN, false);
        this.inputs.put(Input.Keys.SPACE, false);
        this.inputs.put(Input.Keys.E, false);
        this.inputs.put(Input.Keys.Q, false);
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

        hud.registerAction("addPots", new HUDActionCommand() {
            static final String help = "Add health potions to your inventory. Usage: addPots <amount> ";
            @Override
            public String execute(String[] cmd) {
                try {
                    int amount = Integer.parseInt(cmd[1]);
                    for (int i = 0; i < amount; i++) {
                        player.pickUpConsumable();
                    }
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("is", new HUDActionCommand() {
            static final String help = "Increase a stat. Usage: is <statName> <amount> ";
            @Override
            public String execute(String[] cmd) {
                try {
                    String statName = cmd[1];
                    String amount = cmd[2];
                    switch (statName) {
                        case "h":
                        case "health":
                        case "Health":
                            player.stats.increaseHealth(Integer.parseInt(amount));
                            break;
                        case "d":
                        case "damage":
                        case "Damage":
                            player.stats.increaseDamage(Integer.parseInt(amount));
                            break;
                        case "as":
                        case "attackSpeed":
                        case "AttackSpeed":
                        case "attack_speed":
                        case "attackspeed":
                        case "atkspd":
                        case "AtkSpd":
                        case "atkSpd":
                            player.stats.increaseAttackSpeed(Float.parseFloat(amount));
                            break;
                        case "ms":
                        case "moveSpeed":
                        case "MoveSpeed":
                        case "move_speed":
                        case "movespeed":
                        case "MoveSpd":
                        case "movespd":
                        case "moveSpd":
                            player.stats.increaseMoveSpeed(Float.parseFloat(amount));
                            break;
                        case "dx":
                        case "de":
                        case "dex":
                        case "dexterity":
                        case "Dexterity":
                        case "Dex":
                            player.stats.increaseDexterity(Float.parseFloat(amount));
                            break;
                        default:
                            return "Valid statNames: health, damage, attackSpeed, moveSpeed, dexterity";
                    }
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("spawn", new HUDActionCommand() {
            static final String help = "Spawn an Enemy at a location. Usage: spawn <EnemyType> <x> <y> " +
                    "\nValid EnemyTypes: Archer, Bomber, Swordsman ";
            @Override
            public String execute(String[] cmd) {
                try {
                    String enemyClassName = cmd[1];
                    int x = Integer.parseInt(cmd[2]);
                    int y = Integer.parseInt(cmd[3]);
                    if (x < 0 || x > currentRoom.roomWidth-RRGame.STANDARD_ENEMY_SIZE || y < 0 ||
                            y > currentRoom.roomHeight-RRGame.STANDARD_ENEMY_SIZE) return "Cannot spawn out of bounds";
                    switch (enemyClassName) {
                        case "a":
                        case "archer":
                        case "Archer":
                            new Archer(x, y, RRGame.STANDARD_ENEMY_SIZE);
                            break;
                        case "b":
                        case "bomber":
                        case "Bomber":
                            new Bomber(x, y, RRGame.STANDARD_ENEMY_SIZE);
                            break;
                        case "s":
                        case "swordsman":
                        case "Swordsman":
                            new Swordsman(x, y, RRGame.STANDARD_ENEMY_SIZE);
                            break;
                        default:
                            return "Valid EnemyTypes: Archer, Bomber, Swordsman";
                    }
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
        /*
        proposition 11/22 CT - Executing player actions on both keyup and keydown has presented a huge challenge in the
        networking.

        We can still using the multiplexor, but instead of acting on both key-up/key-down, we are instead setting
        the state of those keys to an input hashmap that we can poll during the update event.

        This way we only have to worry about whether the key is being pressed or not during a given frame.
        Otherwise we have to worry about which key-down pertains to which key-up etc, and which frames those happened on.
        */
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
                    inputs.put(Input.Keys.LEFT, true);
                }

                if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                    inputs.put(Input.Keys.RIGHT, true);
                }

                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    inputs.put(Input.Keys.DOWN, true);
                }

                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    inputs.put(Input.Keys.UP, true);
                }

                if (keycode == Input.Keys.SPACE) {
                    inputs.put(Input.Keys.SPACE, true);
                }

                if (keycode == Input.Keys.E) {
                    inputs.put(Input.Keys.E, true);
                }

                if (keycode == Input.Keys.Q) {
                    inputs.put(Input.Keys.Q, true);
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
                    inputs.put(Input.Keys.LEFT,false);
                }
                if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
                    inputs.put(Input.Keys.RIGHT,false);
                }
                if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
                    inputs.put(Input.Keys.DOWN,false);
                }
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
                    inputs.put(Input.Keys.UP,false);
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
        this.newlyAddedEntities.add(entity);
    }

    /**
     * This is called any time a class inheriting Entity is removed from this Screen.
     * See the Entity class constructor for more information.
     * @param entity A Locally Instantiated Entity
     */
    public void removeEntity(Entity entity) {
        this.entitiesToRemove.add(entity);
    }
}
