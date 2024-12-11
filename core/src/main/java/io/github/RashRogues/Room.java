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

    protected int numEnemies;
    protected int difficulty;
    protected Music music;
    // enemies would likely be spawned in here
    // this would include the one that drops the key.

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

        this.numEnemies = numEnemies;
        this.difficulty = difficulty;
    }

    void spawnInitialEnemies() {
        Random rnd = RRGame.globals.getRandom();
        int numKeys = 5;
        for (int i = 0; i < numEnemies; i++) {
            float x = rnd.nextFloat(10, roomWidth-10);
            float y = rnd.nextFloat(10, roomHeight);
            if (numKeys > 0) {
                // Key enemies should be up towards the door
                y = rnd.nextFloat(roomHeight-10, roomHeight);
            }
            int choice = rnd.nextInt(3);
            Enemy e = null;
            switch (choice) {
                case 0:
                    e = new Swordsman(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, numKeys-- > 0);
                    break;
                case 1:
                    e = new Archer(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, numKeys-- > 0);
                    break;
                case 2:
                    e = new Bomber(x, y, RRGame.STANDARD_ENEMY_SIZE, RRGame.globals.playersSet, numKeys-- > 0);
                    break;
            }
            for (int j = 0; j < difficulty; j++){
                e.levelUpEnemy();
            }
        }
        music.play();
    }

    void stopMusic() {
        music.stop();
    }
}
