package io.github.RashRogues;

public class BombExplosion extends Projectile {

    private final float BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR = 0.63f;

    BombExplosion(EntityAlignment alignment, float x, float y, int damage) {
        super(alignment, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_EXPLOSION_IMG), x, y,
                RRGame.SMOKE_BOMB_EXPLOSION_SIZE, RRGame.SMOKE_BOMB_EXPLOSION_SIZE, damage,
                0, false, RRGame.BOMB_EXPLOSION_DURATION);
        this.setBoxPercentSize(BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, hitBox);
    }
}