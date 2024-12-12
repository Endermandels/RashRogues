package io.github.RashRogues;

public class BomberExplosion extends Projectile {

    private final float BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR = 0.63f;

    BomberExplosion(float x, float y, int damage) {
        super(EntityAlignment.ENEMY, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_EXPLOSION_IMG), x, y,
                RRGame.BOMBER_BOMB_EXPLOSION_SIZE, RRGame.BOMBER_BOMB_EXPLOSION_SIZE, damage,
                0, false, RRGame.BOMBER_BOMB_EXPLOSION_DURATION, AnimationActor.BOMBER_EXPLOSION);
        this.setBoxPercentSize(BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, hitBox);
    }
}