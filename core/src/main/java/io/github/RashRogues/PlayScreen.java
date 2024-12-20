package io.github.RashRogues;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.*;

public class PlayScreen extends ScreenAdapter implements RRScreen {

    private boolean debug = false;
    private RRGame game;
    private HUD hud;
    public GUI gui;
    private Room currentRoom;
    private Player player;
    private Door currentDoor;
    private ArrayList<Room> rooms;
    private HashSet<Entity> localEntities;
    private HashSet<Entity> newlyAddedEntities;
    private HashSet<Entity> entitiesToRemove;
    private PriorityQueue<Entity> renderQueue;
    private HashMap<Integer, Boolean> inputs;
    private Random rnd = RRGame.globals.getRandom();
    public static CollisionGrid collisionGrid = new CollisionGrid();
    public static ParticleEffectPool smokeParticleEffectPool;
    public static Array<ParticleEffectPool.PooledEffect> smokeParticleEffects;

    //Debugging
    private static BitmapFont font = new BitmapFont(Gdx.files.internal("Fonts/debug.fnt"),false);
    private static Entity debugEntity = null;

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
        createHUDAndInputs();
        initPlayer();
        setNextRoom();

        if (this.currentRoom.getRoomType() == RoomType.BATTLE){
            player.setPosition(RRGame.PLAYER_SPAWN_X,RRGame.PLAYER_SPAWN_Y);
        }

        if (this.currentRoom.getRoomType() == RoomType.MERCHANT){
            player.setPosition(RRGame.PLAYER_SPAWN_MERCHANT_X,RRGame.PLAYER_SPAWN_MERCHANT_Y);
        }

        this.game.network.connection.dispatchCreatePlayer(player);

    }

    private void initPlayer(){

        player = new Player(1,1, (int) RRGame.PLAYER_SIZE, RRGame.globals.pid);

        font.getData().setScale(3f);
        smokeParticleEffectPool = new ParticleEffectPool(RRGame.am.get(RRGame.RSC_SMOKE_PARTICLE_EFFECT, ParticleEffect.class), 5, 10);
        smokeParticleEffects = new Array<>();

        RRGame.globals.addPlayer(RRGame.globals.pid,player);

        gui = new GUI(player);
        game.playerCam.center();
        game.playerCam.bind(player);

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
        byte[] keyMask = new byte[9];
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
            this.player.useConsumable(RRGame.globals.pid, RRGame.globals.frame);
            inputs.put(Input.Keys.E, false);
            keyMask[5] = 1;
        }
        if (inputs.get(Input.Keys.Q)){
            this.player.useAbility(RRGame.globals.pid, RRGame.globals.frame);
            inputs.put(Input.Keys.Q, false);
            keyMask[6] = 1;
        }

        if (this.player.shopping) {
            return;
        }

        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        player.mouseLocation = RRGame.playerCam.unproject(new Vector3(x, y, 0));

        game.network.connection.dispatchKeys(keyMask, RRGame.globals.frame, this.player.getX(), this.player.getY(), player.mouseLocation.x, player.mouseLocation.y);
        RRGame.globals.frame++;
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

        /* Update Camera's Location and Set its Projection Matrix */
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
        currentRoom.update(delta);

        gui.update();
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.batch.setProjectionMatrix(game.playerCam.combined);
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1f);
        game.batch.begin();
        currentRoom.draw(game.batch);
        while (!renderQueue.isEmpty()){
            Entity e = renderQueue.poll();
            if (!(e instanceof GUIElement)) e.draw(game.batch);
        }
        for (ParticleEffectPool.PooledEffect effect : smokeParticleEffects) {
            effect.draw(game.batch, delta);

            // Ensure we allow the effect to complete if it's looping
            effect.allowCompletion();

            if (effect.isComplete()) {
                effect.free();
                smokeParticleEffects.removeValue(effect, true);
            }
        }

        game.batch.end();


        game.hudBatch.begin();
        gui.draw(game.hudBatch);
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

        Vector3 mouse = new Vector3();
        mouse.x = Gdx.input.getX();
        mouse.y = Gdx.input.getY();
        mouse.z = 0;
        game.playerCam.unproject(mouse);

        for (Entity e : localEntities){
            if ((mouse.x > e.hitBox.getX() && mouse.x < e.hitBox.getX() + e.hitBox.getWidth()) && (mouse.y > e.hitBox.getY() && mouse.y < e.hitBox.getY() + e.hitBox.getHeight())){
                debugEntity = e;
                break;
            }
        }

        if (debugEntity != null) {
            game.hudBatch.begin();
            font.getData().setScale(3f);
            font.draw(game.hudBatch, "Entity: " + debugEntity.toString(), 5, Gdx.graphics.getHeight()-30);
            font.draw(game.hudBatch, "Repl. Type: " + debugEntity.replicationType, 5, Gdx.graphics.getHeight()-60);
            font.getData().setScale(2.5f);
            font.draw(game.hudBatch, "ID: " + debugEntity.id, 5, Gdx.graphics.getHeight() - 90);
            font.draw(game.hudBatch, "Number: " + debugEntity.number, 5, Gdx.graphics.getHeight() - 110);
            font.draw(game.hudBatch, "Creator: " + debugEntity.pid, 5, Gdx.graphics.getHeight() - 130);
            game.hudBatch.end();
        }

    }

    @Override
    public void resize(int width, int height) {
        hud.resize(width, height, game);
        gui.resize(width, height);
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
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM_MERCHANT_IMG),
                10, 19, 0, 0, game.room2Music, RoomType.MERCHANT));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM1_IMG),
                35, 301, 10, 0, game.room1Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM2_IMG),
                35, 301, 30, 5, game.room2Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM_MERCHANT_IMG),
                10, 19, 0, 0, game.room2Music, RoomType.MERCHANT));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM3_IMG),
                35, 301, 50, 10, game.room3Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM1_IMG),
                35, 301, 60, 15, game.room1Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM_MERCHANT_IMG),
                10, 19, 0, 0, game.room2Music, RoomType.MERCHANT));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM2_IMG),
                35, 301, 80, 15, game.room2Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM3_IMG),
                35, 301, 100, 20, game.room3Music, RoomType.BATTLE));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM_MERCHANT_IMG),
                10, 19, 0, 0, game.room2Music, RoomType.MERCHANT));
        rooms.add(new Room(RRGame.am.get(RRGame.RSC_ROOM_MERCHANT_IMG),
                10, 19, 0, 0, game.room3Music, RoomType.KING));


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
            game.setScreen(new WinScreen(game));
            return;
        }
        else {
            currentRoom.stopMusic();
            currentRoom = rooms.get(rooms.indexOf(currentRoom) + 1);
        }
        HashSet<Entity> tempLocalEntities = new HashSet<>();
        for (Entity e : localEntities) {
            if (e instanceof Player) {
                Player player = (Player) e;
                player.resetForNewRoom(currentRoom.getRoomType());
                tempLocalEntities.add(e);
            }
        }
        currentRoom.spawnInitialEntities();
        localEntities = tempLocalEntities;
        currentDoor = new Door(currentRoom.doorPositionX, currentRoom.doorPositionY, currentRoom.doorUnlockedByDefault);
        game.playerCam.changeWorldSize(currentRoom.roomWidth, currentRoom.roomHeight, currentRoom.doorPositionX, currentRoom.doorPositionY);
        collisionGrid.updateCollisionGridRoomValues(currentRoom.roomWidth, currentRoom.roomHeight);
        gui = new GUI(RRGame.globals.players.get(RRGame.globals.pid));
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
            static final String help = "Teleport to a specific location. Usage: tp <pid> <x> <y> ";
            @Override
            public String execute(String[] cmd) {
                try {
                    if (cmd.length != 4){
                        return help;
                    }

                    int pid     = Integer.parseInt(cmd[1]);
                    int x       = Integer.parseInt(cmd[2]);
                    int y       = Integer.parseInt(cmd[3]);

                    Player p    = RRGame.globals.players.get(pid);

                    if (p == null){
                        return "Player with id " + Integer.toString(pid) + " does not exist.";
                    }

                    if (x < 0 || x > currentRoom.roomWidth-p.getWidth() || y < 0 ||
                            y > currentRoom.roomHeight-p.getHeight()) return "Cannot teleport out of bounds.";

                    p.setPosition(x, y);
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
                            RRGame.globals.network.connection.dispatchCommand(cmd);
                            break;
                        case "d":
                        case "damage":
                        case "Damage":
                            player.stats.increaseDamage(Integer.parseInt(amount));
                            RRGame.globals.network.connection.dispatchCommand(cmd);
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
                            RRGame.globals.network.connection.dispatchCommand(cmd);
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
                            RRGame.globals.network.connection.dispatchCommand(cmd);
                            break;
                        case "dx":
                        case "de":
                        case "dex":
                        case "dexterity":
                        case "Dexterity":
                        case "Dex":
                            player.stats.increaseDexterity(Float.parseFloat(amount));
                            RRGame.globals.network.connection.dispatchCommand(cmd);
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

        hud.registerAction("reset", new HUDActionCommand() {
            static final String help = "Reset a player to default stats and status.";

            @Override
            public String execute(String[] cmd) {
                try{
                    if (cmd.length != 2){
                        return "Invalid number of arguments.";
                    }
                    String pidStr = cmd[1];
                    int pid = Integer.parseInt(pidStr);
                    Player p = RRGame.globals.players.get(pid);
                    if (p == null){
                        return "No player with pid " + Integer.toString(pid) + " found!";
                    }
                    p.resetPlayer();

                } catch (Exception e){
                   return "Syntax of the command is: reset <pid>";
                }
                return "player reset successfully.";
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
                            new Archer(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, false);
                            break;
                        case "b":
                        case "bomber":
                        case "Bomber":
                            new Bomber(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, false);
                            break;
                        case "s":
                        case "swordsman":
                        case "Swordsman":
                            new Swordsman(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, false);
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

                // Do Not Allow Inputs Other Than Movement In Merchant Room
                if (currentRoom.getRoomType() == RoomType.MERCHANT){
                    return true;
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

    @Override
    public void nextScreen(Screen screen) {
        return;
    }

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

    public void executeCommand(String[] cmd){
        this.hud.executeCommand(cmd);
    }

    public void dispose(){
        for (ParticleEffectPool.PooledEffect effect : smokeParticleEffects) {
            effect.free();
        }
    }

    public GUI getGUI(){
        return this.gui;
    }

    @Override
    public Room getRoom() {
        return this.currentRoom;
    }

    @Override
    public void dropCoins(float x, float y, int enemyLevel) {
        System.out.println("DROPPING COINS FROM THE SERVER At X = " + x + " and Y = " + y + " level " + enemyLevel);
        int valCoin = rnd.nextInt(2*(enemyLevel+1));
        float dx = (rnd.nextInt(Enemy.MAX_DROP_DISTANCE) - 1) / 2;
        float dy = (rnd.nextInt(Enemy.MAX_DROP_DISTANCE) - 1) / 2;
        new Coin(x + dx, x + dy, valCoin);
        System.out.println("newcoin!");
    }
}
