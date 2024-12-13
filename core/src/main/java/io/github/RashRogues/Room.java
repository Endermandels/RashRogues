package io.github.RashRogues;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Random;

public class Room extends Sprite {
    // other stuff like bounding box idk yet, would be nice to keep all that logic out of PlayScreen.java
    public int roomWidth;
    public int roomHeight;
    protected int doorPositionX;
    protected int doorPositionY;
    protected boolean doorUnlockedByDefault;

    private RoomType roomType;

    private final int BASE_NUM_CHESTS = 20;
    private int numChests;


    protected int numEnemies;
    protected int difficulty;
    protected Music music;
    // enemies would likely be spawned in here
    // this would include the one that drops the key.

    protected final float SPAWN_TIMER_DECAY = 0.5f;
    protected final float SPAWN_TIMER_MIN = 0.5f;
    protected float spawnTimerMax = 5f;
    protected float spawnTimer;

    protected Random rnd;

    /**
     * Create a battle room.
     * @param texture
     * @param doorPositionX
     * @param doorPositionY
     * @param numEnemies
     * @param difficulty
     * @param music
     */
    Room(Texture texture, int doorPositionX, int doorPositionY, int numEnemies, int difficulty, Music music, RoomType roomType) {
        super(texture);
        this.music = music;

        switch (roomType) {
            case MERCHANT:
                roomWidth = (int) RRGame.MERCHANT_ROOM_WIDTH;
                roomHeight = texture.getHeight() * roomWidth / texture.getWidth();
                createMerchantRoom(doorPositionX,doorPositionY);
                break;

            case BOSS:
                createBossRoom();
                break;

            case KING:
                createKingRoom();
                break;

            case BATTLE:
                roomWidth = (int) RRGame.WORLD_WIDTH;
                roomHeight = texture.getHeight() * roomWidth / texture.getWidth();
                createBattleRoom(doorPositionX,doorPositionY,numEnemies,difficulty);
                break;
        }
        this.roomType = roomType;
    }

    private void createBattleRoom(int doorPositionX, int doorPositionY, int numEnemies, int difficulty){
        this.doorPositionX = doorPositionX;
        this.doorPositionY = doorPositionY;
        /*
        the logic below scales whatever the size of the map is to be 500m wide and
        whatever the proportional height is to that based on the texture.
        Example:
        Room A's texture is 1000 px wide and 2000 px tall. The logic in the constructor
        adjusts the width to be 500 meters and the height to be 1000m.
         */
        setSize(roomWidth, roomHeight);
        setPosition(0, 0);

        // current thinking is that a diff constructor can be used for boss and merchant rooms, or maybe subclasses,
        // which will then set numEnemies and numChests things to 0 and would have diff spawning rules
        this.numEnemies = numEnemies;
        this.difficulty = difficulty;
        this.numChests = BASE_NUM_CHESTS-(difficulty / 10);
        spawnTimer = 0;
        rnd = RRGame.globals.getRandom();
        this.roomType = RoomType.BATTLE;
        doorUnlockedByDefault = false;
    }

    private void createMerchantRoom(int doorPositionX, int doorPositionY){
        setSize(roomWidth, roomHeight);
        setPosition(0, 0);
        this.doorPositionX = doorPositionX;
        this.doorPositionY = doorPositionY;
        doorUnlockedByDefault = true;
    }

    private void createBossRoom(){

    }

    private void createKingRoom(){

    }

    private void spawnEnemy(float x, float y, boolean hasKey) {
        int choice = rnd.nextInt(3);
        Enemy e = null;
        switch (choice) {
            case 0:
                e = new Swordsman(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, hasKey);
                break;
            case 1:
                e = new Archer(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, hasKey);
                break;
            case 2:
                e = new Bomber(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, hasKey);
                break;
        }
        for (int j = 0; j < difficulty; j++){
            e.levelUpEnemy();
        }
    }

    void spawnInitialEntities() {

        switch (roomType){

            case BATTLE:
                int numKeys = 5;
                for (int i = 0; i < numEnemies; i++) {
                    float x = rnd.nextFloat(10, roomWidth-10);
                    float y = rnd.nextFloat(10, doorPositionY-10);
                    if (numKeys > 0) {
                        // Key enemies should be up towards the door
                        y = rnd.nextFloat(doorPositionY-30, doorPositionY-10);
                    }
                    spawnEnemy(x, y, numKeys-- > 0);
                }
                for (int ii = 0; ii < numChests; ii++) {
                    float x = rnd.nextFloat(10, roomWidth-10);
                    float y = rnd.nextFloat(10, doorPositionY-10);
                    new Chest(x, y);
                }
                music.play();
                break;

            case MERCHANT:

                new Merchant(RRGame.MERCHANT_SPAWN_X,RRGame.MERCHANT_SPAWN_Y,RRGame.MERCHANT_SIZE,RRGame.globals.playersSet,false);


                break;
        }



    }

    void stopMusic() {
        music.stop();
    }

    void update(float delta) {

        switch (roomType){

            case BATTLE:
                spawnTimer += delta;
                if (spawnTimer > spawnTimerMax) {
                    spawnTimer = 0;
                    spawnTimerMax = Math.max(SPAWN_TIMER_MIN, spawnTimerMax-SPAWN_TIMER_DECAY);
                    float x = rnd.nextFloat(10, roomWidth-10);
                    float y = rnd.nextFloat(0, 10);
                    spawnEnemy(x, y, false);
                }
            break;


            case MERCHANT:

            break;


            case BOSS:

            break;


            case KING:

            break;
        }
    }

    public RoomType getRoomType(){
        return this.roomType;
    }
}
