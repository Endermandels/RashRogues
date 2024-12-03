package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Projectile extends Entity {
    // Melee Projectiles are just projectiles that don't move and only last a small amount of time (animation time)

    // when more projectiles are created, ideally follow the pattern in the enemy class and its subclasses.
    // for now, there's one projectile and it does 1 damage and doesn't move
    protected int damage;
    protected boolean removeNextUpdate = false;
    private boolean onlyHitOneTarget;
    protected float duration;
    protected float travelTimer;
    private float speed;
    private float distance;

    /**
     * Typical Ranged Projectile (direction, distance, and speed based)
     * @param alignment
     * @param texture
     * @param x
     * @param y
     * @param width
     * @param height
     * @param xDirection
     * @param yDirection
     * @param damage
     * @param degreesOffsetFromFacingRight
     * @param onlyHitOneTarget
     * @param distance
     * @param speed
     */
    Projectile(EntityAlignment alignment, Texture texture, float x, float y, float width, float height,
               float xDirection, float yDirection, int damage, float degreesOffsetFromFacingRight,
               boolean onlyHitOneTarget, float distance, float speed) {
        super(alignment, texture, x, y, width, height, Layer.PROJECTILE);
        Vector2 direction = new Vector2(xDirection, yDirection).nor();
        this.damage = damage;
        this.distance = distance;
        this.speed = speed;
        this.duration = distance / speed;
        this.xVelocity = direction.x * speed;
        this.yVelocity = direction.y * speed;
        this.setRotation(degreesOffsetFromFacingRight+direction.angleDeg());
        this.onlyHitOneTarget = onlyHitOneTarget;
        this.travelTimer = 0f;
    }

    /**
     * Explosion or Melee Projectile (duration based)
     * @param alignment
     * @param texture
     * @param x
     * @param y
     * @param width
     * @param height
     * @param damage
     * @param degreesOffsetFromFacingRight
     * @param onlyHitOneTarget
     * @param duration
     */
    Projectile(EntityAlignment alignment, Texture texture, float x, float y, float width, float height,
               int damage, float degreesOffsetFromFacingRight, boolean onlyHitOneTarget, float duration) {
        super(alignment, texture, x, y, width, height, Layer.PROJECTILE);
        this.damage = damage;
        this.speed = 0;
        this.duration = duration;
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.setRotation(degreesOffsetFromFacingRight+90);
        this.onlyHitOneTarget = onlyHitOneTarget;
        this.travelTimer = 0f;
    }

    /**
     * Creates a copy of this projectile-- most commonly used in Bombs to reset projectile timers.
     * Important to understand is that while the projectile passed to a bomb may have expired and called removeSelf(),
     * the bomb still has a reference to it, so it still exists (albeit placed at -100, -100 and not interacting with
     * anything). Bomb, once the fuseTimer is finished, will create a new copy of the projectile and immediately kill
     * itself, meaning the only reference to this returned projectile is localEntities, and it will then act like
     * a normal projectile with a specific duration.
     * @param explosionOrMelee should be true if an explosion or a melee projectile (unmoving).
     * @return A copy of this projectile
     */
    public Projectile makeProjectile(boolean explosionOrMelee) {
        Projectile returnProjectile;
        if (!explosionOrMelee) {
            Vector2 direction = new Vector2(xVelocity, yVelocity);
            float degOffset = this.getRotation() - direction.angleDeg();
            returnProjectile = new Projectile(this.alignment, this.getTexture(), this.getX(), this.getY(), this.getWidth(),
                    this.getHeight(), this.xVelocity, this.yVelocity, this.damage, degOffset, this.onlyHitOneTarget,
                    this.distance, this.speed);
        }
        else {
            returnProjectile =  new Projectile(this.alignment, this.getTexture(), this.getX(), this.getY(), this.getWidth(),
                    this.getHeight(), this.damage, this.getRotation(), this.onlyHitOneTarget, this.duration);
        }
        // because this step is done on each subclass, we need to do it here
        returnProjectile.setBoxPercentSize(this.hitBoxWidthScalar, this.hitBoxHeightScalar, returnProjectile.hitBox);
        return returnProjectile;
    }

    /**
     * Runs Every Frame
     * @param delta
     */
    public void update(float delta) {
        if (removeNextUpdate) { this.removeSelf(); return;}
        super.update(delta);
        travelTimer += delta;
        if (travelTimer >= duration) { removeNextUpdate = true;}
        // technically this isn't even needed right now because a projectile just wants to move whatever
        // direction it's going; however, I foresee specific projectiles needing special logic
    }

    @Override
    public void onHit(Entity thingHit) {
        if (!(this.alignment.equals(thingHit.alignment)) && onlyHitOneTarget) {
            removeNextUpdate = true;
        }
    }
}
