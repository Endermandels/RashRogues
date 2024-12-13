package io.github.RashRogues;

import Networking.ReplicationType;
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
    private long number;
    private int creator;
    private AnimationActor animationActor;

    /**
     * Typical Ranged Projectile (direction, distance, and speed based)
     * This projectile will be synced.
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
     * @param pid Creator of projectile.
     * @param number How many projectiles have came before.
     */
    Projectile(EntityAlignment alignment, Texture texture, float x, float y, float width, float height,
               float xDirection, float yDirection, int damage, float degreesOffsetFromFacingRight,
               boolean onlyHitOneTarget, float distance, float speed, AnimationActor animationActor, ReplicationType replicationType, int pid, long number) {
        super(alignment,texture,x,y,width,height,Layer.PROJECTILE, animationActor, replicationType, pid, number);
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
        this.animationActor = animationActor;
        this.number = number;
        this.creator = pid;
        setCurrentAnimation(AnimationAction.ATTACK);
    }

    /**
     * Explosion or Melee Projectile (duration based)
     * Duration based projectiles do not need synced because they destroy themselves.
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
    Projectile(EntityAlignment alignment, Texture texture, float x, float y, float width, float height, int damage,
               float degreesOffsetFromFacingRight, boolean onlyHitOneTarget, float duration, AnimationActor animationActor) {
        super(alignment, texture, x, y, width, height, Layer.PROJECTILE, animationActor,
                ReplicationType.CLIENTSIDE,-1,-1);
        this.damage = damage;
        this.speed = 0;
        this.duration = duration;
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.setRotation(degreesOffsetFromFacingRight+90);
        this.onlyHitOneTarget = onlyHitOneTarget;
        this.travelTimer = 0f;
        this.animationActor = animationActor;
        this.number = -1;
        this.creator = -1;
        setCurrentAnimation(AnimationAction.ATTACK);
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
                    this.distance, this.speed, this.animationActor, this.replicationType, this.creator, this.number);
        }
        else if (this instanceof SmokeBombExplosion){
            // this is annoying but i'm tired and cant think of another easy way to do this:
            returnProjectile =  new SmokeBombExplosion(this.getX(), this.getY());
        }
        else {
            returnProjectile =  new Projectile(this.alignment, this.getTexture(), this.getX(), this.getY(), this.getWidth(),
                    this.getHeight(), this.damage, this.getRotation(), this.onlyHitOneTarget, this.duration,
                    this.animationActor);
        }
        // because this step is done on each subclass, we need to do it here
        returnProjectile.setBoxPercentSize(this.hitBoxWidthScalar, this.hitBoxHeightScalar, returnProjectile.hitBox);
        returnProjectile.hitBox.setEffect(this.hitBox.getEffect());
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
