package io.github.RashRogues;

import com.badlogic.gdx.math.Rectangle;

// HITBOX IS AN ATTACK, HURTBOX IS WHAT A HITBOX HITS
public class HurtBox extends Rectangle {

    public Entity parent;

    public HurtBox(float x, float y, float width, float height, Entity parent) {
        super(x, y, width, height);
        this.parent = parent;
    }

    public HurtBox(Rectangle rectangle, Entity parent) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height, parent);
    }

    public void update(float delta) {
        setCenter(parent.getX() + (parent.getWidth() / 2), parent.getY() + (parent.getHeight() / 2));
    }

    public void hurtByHitBox(HitBox hitBox) {
        if (hitBox.parent.equals(this.parent)) { return; }
        // please see HitBox.java's hitHurtBox function for a detailed explanation and example on which entities are
        // responsible for what.
        parent.onHurt(hitBox.parent);
        if (hitBox.getEffect() != Effect.NONE) { parent.addEffect(hitBox.getEffect()); }
    }
}
