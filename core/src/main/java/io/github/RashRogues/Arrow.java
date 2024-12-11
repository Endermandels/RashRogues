package io.github.RashRogues;

import Networking.ReplicationType;

public class Arrow extends Projectile {

    private final float ARROW_HIT_BOX_PERCENT_SCALAR = 0.35f;

    Arrow(float x, float y, float xDirection, float yDirection, int damage, float speed) {
        super(EntityAlignment.ENEMY, RRGame.am.get(RRGame.RSC_ARROW_IMG), x, y, RRGame.ARROW_SIZE, RRGame.ARROW_SIZE,
                xDirection, yDirection, damage, -45f, true,
                RRGame.STANDARD_PROJECTILE_DISTANCE, speed, null, -1, -1);
        this.setBoxPercentSize(ARROW_HIT_BOX_PERCENT_SCALAR, ARROW_HIT_BOX_PERCENT_SCALAR, hitBox);
    }
}
