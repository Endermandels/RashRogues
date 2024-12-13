package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class Bomb extends Entity {

    protected Projectile explosion;
    protected float duration;
    private float travelTimer;
    private boolean fuseLit;
    private float fuseDuration;
    private float fuseTimer;
    private float speed; // not useful right now, but I've learned my lesson from needing to refactor Projectile

    private Random rnd;
    private Sound explosionSFX;

    Bomb(EntityAlignment alignment, Texture texture, float x, float y, float width, float height,
         float xDirection, float yDirection, float distance, float fuseDuration, Projectile explosion, float speed,
         AnimationActor animationActor) {
        super(alignment, texture, x, y, width, height, Layer.PROJECTILE, animationActor,
                ReplicationType.CLIENTSIDE, -1, -1);
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

        rnd = RRGame.globals.getRandom();
        explosionSFX = RRGame.am.get(RRGame.RSC_EXPLOSION_SFX);
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
            }
        }
        if (fuseLit) {
            this.setCurrentAnimation(AnimationAction.ATTACK);
            fuseTimer += delta;
            if (fuseTimer >= fuseDuration) {

                // stop fuseLitAnimation
                fuseLit = false;
                explosionSFX.play(0.2f, rnd.nextFloat(0.7f, 1.3f), 0);
                explosion = explosion.makeProjectile(true);
                explosion.setPosition(this.getX()+this.getWidth()/2-explosion.getWidth()/2, this.getY()+this.getHeight()/2-explosion.getHeight()/2);
                if (explosion instanceof SmokeBombExplosion) {
                    System.out.println(this.getX()+this.getWidth()/2 + " " + this.getY()+this.getHeight()/2);
                    ParticleEffectPool.PooledEffect effect = PlayScreen.smokeParticleEffectPool.obtain();
                    effect.setPosition(this.getX()+this.getWidth()/2, this.getY()+this.getHeight()/2);
                    PlayScreen.smokeParticleEffects.add(effect);
                }
                this.removeSelf();
            }
        }
    }
}
