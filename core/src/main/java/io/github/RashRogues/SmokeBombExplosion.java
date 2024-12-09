package io.github.RashRogues;

public class SmokeBombExplosion extends Projectile {

    private final float SMOKE_BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR = 0.63f;

    SmokeBombExplosion(float x, float y) {
        super(EntityAlignment.PLAYER, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_EXPLOSION_IMG), x, y,
                RRGame.SMOKE_BOMB_EXPLOSION_SIZE, RRGame.SMOKE_BOMB_EXPLOSION_SIZE, 0,
                0, false, RRGame.SMOKE_BOMB_EXPLOSION_DURATION);
        this.setBoxPercentSize(SMOKE_BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, SMOKE_BOMB_EXPLOSION_HIT_BOX_PERCENT_SCALAR, hitBox);
        this.hitBox.setEffect(Effect.SMOKE);
        this.clientSideOnly = true;
    }
}
