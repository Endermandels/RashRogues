package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Enemy extends Entity {

    Enemy(Texture texture, int x, int y, float width, float height) {
        super(texture, x, y, width, height);
    }

    Enemy(Texture texture, int x, int y, float size) {
        this(texture, x, y, size, size);
    }
}
