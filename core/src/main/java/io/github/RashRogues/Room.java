package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Room extends Sprite {
    // other stuff like bounding box idk yet, would be nice to keep all that logic out of PlayScreen.java
    public int roomWidth;
    public int roomHeight;

    Room(Texture texture) {
        super(texture);
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
    }
}
