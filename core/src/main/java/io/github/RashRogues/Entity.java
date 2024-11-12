package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Entity extends Sprite {

    protected float maxXVelocity;
    protected float maxYVelocity;
    protected float xVelocity;
    protected float yVelocity;
    protected HitBox hitBox;
    protected boolean flipped;

    Entity(Texture texture, int x, int y, float width, float height) {
        super(texture);
        setSize(width, height);
        setOrigin(width/2, height/2);
        setPosition(x, y);
        hitBox = new HitBox(this.getBoundingRectangle(), this);
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
        hitBox.update(delta);
    }

    protected void setBoxPercentSize(float widthScalar, float heightScalar, Rectangle box) {
        box.setSize(widthScalar * this.getWidth(), heightScalar * this.getHeight());
    }

    // meant to be overridden
    public void onHit(Entity thingHit) {
        System.out.println("No defined behavior; add this function to the Entity");
    }

    // meant to be overridden
    public void onHurt(Entity thingThatHurtMe) {
        System.out.println("No defined behavior; add this function to the Entity");
    }
}
