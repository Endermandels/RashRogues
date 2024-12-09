package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Bomb extends Entity {

    protected Projectile explosion;
    protected float duration;
    private float travelTimer;
    private boolean fuseLit;
    private float fuseDuration;
    private float fuseTimer;
    private float speed; // not useful right now, but I've learned my lesson from needing to refactor Projectile

    Bomb(EntityAlignment alignment, Texture texture, float x, float y, float width, float height,
         float xDirection, float yDirection, float distance, float fuseDuration, Projectile explosion, float speed) {
        super(alignment, texture, x, y, width, height, Layer.PROJECTILE, false);
        this.explosion = explosion;
        this.explosion.setPosition(-100, -100); // out of sight out of mind
        Vector2 direction = new Vector2(xDirection, yDirection).nor();
        this.speed = speed;
        this.duration = distance / speed;
        this.xVelocity = direction.x * speed;
        this.yVelocity = direction.y * speed;
        this.travelTimer = 0f;
        this.fuseLit = false;
        this.fuseDuration = fuseDuration;
        this.fuseTimer = 0f;
        this.clientSideOnly = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        travelTimer += delta;
        if (travelTimer >= duration) {
            xVelocity = 0;
            yVelocity = 0;
            if (!fuseLit) {
                fuseLit = true;
                // play fuseLit animation for fuseDuration amount of time
            }
        }
        if (fuseLit) {
            fuseTimer += delta;
            if (fuseTimer >= fuseDuration) {

                // stop fuseLitAnimation
                fuseLit = false;
                explosion = explosion.makeProjectile(true, true);
                explosion.setPosition(this.getX()+this.getWidth()/2-explosion.getWidth()/2, this.getY()+this.getHeight()/2-explosion.getHeight()/2);
                this.removeSelf();
            }
        }
    }
}
