package io.github.RashRogues;

public class ThrowingKnife extends Projectile {

    private final float THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR = 0.35f;

    ThrowingKnife(float x, float y, float xDirection, float yDirection, int damage, float speed) {
        super(EntityAlignment.PLAYER, RRGame.am.get(RRGame.RSC_THROWING_KNIFE_IMG), x, y, RRGame.THROWING_KNIFE_SIZE, RRGame.THROWING_KNIFE_SIZE,
                xDirection, yDirection, damage, -45f, true,
                RRGame.STANDARD_PROJECTILE_DISTANCE, speed);
        this.setBoxPercentSize(THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR, THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR, hitBox);
    }
}
