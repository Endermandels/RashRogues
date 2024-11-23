package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Projectile extends Entity {
    // Melee Projectiles are just projectiles that don't move and only last a small amount of time (animation time)

    // when more projectiles are created, ideally follow the pattern in the enemy class and its subclasses.
    // for now, there's one projectile and it does 1 damage and doesn't move
    protected int damage;
    protected boolean removeNextUpdate = false;

    Projectile(EntityAlignment alignment, Texture texture, int x, int y, float width, float height,
               float xVelocity, float yVelocity, int damage) {
        super(EntityType.PROJECTILE, alignment, texture, x, y, width, height, Layer.PROJECTILE);
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.damage = damage;
    }

    /**
     * Runs Every Frame
     * @param delta
     */
    public void update(float delta) {
        if (removeNextUpdate) { this.removeSelf(); return;}
        super.update(delta);
        // technically this isn't even needed right now because a projectile just wants to move whatever
        // direction it's going; however, I foresee specific projectiles needing special logic
    }

    @Override
    public void onHit(Entity thingHit) {
        if (this.alignment == EntityAlignment.ENEMY && thingHit.alignment == EntityAlignment.PLAYER
        || this.alignment == EntityAlignment.PLAYER && thingHit.alignment == EntityAlignment.ENEMY) {
            removeNextUpdate = true;
        }
    }
}
