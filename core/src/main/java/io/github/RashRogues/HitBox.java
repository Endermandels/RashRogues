package io.github.RashRogues;

import com.badlogic.gdx.math.Rectangle;

// HITBOX IS AN ATTACK, HURTBOX IS WHAT A HITBOX HITS
public class HitBox extends Rectangle {

    public Entity parent;
    public float disableTimer;
    protected float disableLength;

    public HitBox(float x, float y, float width, float height, Entity parent) {
        super(x, y, width, height);
        this.parent = parent;
        this.disableTimer = 0f;
        this.disableLength = 1f;
    }

    public HitBox(Rectangle rectangle, Entity parent) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height, parent);
    }

    public void update(float delta) {
        setCenter(parent.getX() + (parent.getWidth() / 2), parent.getY() + (parent.getHeight() / 2));
        disableTimer += delta;
    }

    public void hitHurtBox(HurtBox hurtBox) {
        if ((hurtBox.parent.equals(this.parent)) || (disableTimer < disableLength)) { return; }
        disableTimer = 0f;
        // keep in mind that an entity should only deal with itself; hit/hurt boxes will notify the entities, but
        // the entities should only be reading from each other, NOT writing each other's variables!
        // Example: arrow hits player. Arrow's onHit function is called, it verifies that the player is alive and
        // that it is a player and not an enemy. It then sets its removeNextUpdate variable to true so that it will
        // be removed since it hit a player and should disappear when the target is hit.
        // On the other hand, the player's onHurt function is called. The player calls takeDamage on itself, using
        // the value from the arrow.
        hurtBox.hurtByHitBox(this);
        System.out.println(parent + " just hit " + hurtBox.parent);
        parent.onHit(hurtBox.parent);
    }
}
