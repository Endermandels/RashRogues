package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Entity extends Sprite {

    protected float maxXVelocity;
    protected float maxYVelocity;
    protected float xVelocity;
    protected float yVelocity;
    protected Rectangle boundingBox;
    protected boolean flipped;
    protected Layer layer;

    Entity(Texture texture, int x, int y, float width, float height, Layer layer) {
        super(texture);
        RRGame.instances.add(this);
        this.layer= layer;

        setSize(width, height);
        setOrigin(width/2, height/2);
        setPosition(x, y);
        boundingBox = this.getBoundingRectangle();
        // maybe make edits here idk how good it will be but this gives us a baseline
        this.maxXVelocity = 15.0f;
        this.maxYVelocity = 15.0f;
        // this can be whatever, I imagine each enemy might have its own speed but for now this works,
        // just override the maxVelocities on the subclasses.
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
}
