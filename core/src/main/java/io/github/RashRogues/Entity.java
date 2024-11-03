package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Entity extends Sprite {

    protected float maxXVelocity;
    protected float maxYVelocity;
    private float xVelocity;
    private float yVelocity;
    protected Rectangle boundingBox;
    protected boolean flipped;

    Entity(Texture texture, int x, int y) {
        super(texture);
        setPosition(x, y);
        boundingBox = this.getBoundingRectangle();
        // maybe make edits here idk how good it will be but this gives us a baseline
        this.maxXVelocity = 100.0f;
        this.maxYVelocity = 100.0f;
        this.flipped = false;
    }

    public void update(float delta) {
        float x = getX();
        float y = getY();
        if (flipped) {
            this.setScale(-Math.abs(getScaleX()), getScaleY());
        }
        else {
            this.setScale(Math.abs(getScaleX()), getScaleY());
        }

        setX(x + delta * xVelocity);
        setY(y + delta * yVelocity);
    }

    protected void setXVelocity(float xVelocity) {
        this.xVelocity = xVelocity;
    }

    protected void setYVelocity(float yVelocity) {
        this.yVelocity = yVelocity;
    }


}
