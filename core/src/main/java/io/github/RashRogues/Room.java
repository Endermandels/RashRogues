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

    Room(Texture texture, int doorPositionX, int doorPositionY, int numEnemies, int difficulty, Music music) {
        super(texture);
        this.doorPositionX = doorPositionX;
        this.doorPositionY = doorPositionY;
        this.music = music;
        /*
        the logic below scales whatever the size of the map is to be 500m wide and
        whatever the proportional height is to that based on the texture.
        Example:
        Room A's texture is 1000 px wide and 2000 px tall. The logic in the constructor
        adjusts the width to be 500 meters and the height to be 1000m.
         */
        roomWidth = (int) RRGame.WORLD_WIDTH;
        roomHeight = texture.getHeight() * roomWidth / texture.getWidth();
        setSize(roomWidth, roomHeight);
        setPosition(0, 0);

        // current thinking is that a diff constructor can be used for boss and merchant rooms, or maybe subclasses,
        // which will then set numEnemies and numChests things to 0 and would have diff spawning rules
        this.numEnemies = numEnemies;
        this.difficulty = difficulty;
        this.numChests = BASE_NUM_CHESTS-(difficulty / 10);
        spawnTimer = 0;
        rnd = RRGame.globals.getRandom();
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
    }

    void stopMusic() {
        music.stop();
    }

    void update(float delta) {
        // Spawn enemies
        spawnTimer += delta;
        if (spawnTimer > spawnTimerMax) {
            spawnTimer = 0;
            spawnTimerMax = Math.max(SPAWN_TIMER_MIN, spawnTimerMax-SPAWN_TIMER_DECAY);
            float x = rnd.nextFloat(10, roomWidth-10);
            float y = rnd.nextFloat(0, 10);
            spawnEnemy(x, y, false);
        }
    }
}
