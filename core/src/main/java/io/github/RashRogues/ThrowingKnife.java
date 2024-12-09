package io.github.RashRogues;

public class ThrowingKnife extends Projectile {

    private final float THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR = 0.35f;

    /**
     * Create a throwing knife.
     * This knife will be synced.
     * @param x
     * @param y
     * @param xDirection
     * @param yDirection
     * @param damage
     * @param speed
     * @param pid Player whom created the knife
     * @param frame Frame on which the knife was created.
     */
    ThrowingKnife(float x, float y, float xDirection, float yDirection, int damage, float speed, int pid, long frame) {
        super(EntityAlignment.PLAYER, RRGame.am.get(RRGame.RSC_THROWING_KNIFE_IMG), x, y, RRGame.THROWING_KNIFE_SIZE, RRGame.THROWING_KNIFE_SIZE,
                xDirection, yDirection, damage, -45f, true,
                RRGame.STANDARD_PROJECTILE_DISTANCE, speed, pid, frame, false);
        this.setBoxPercentSize(THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR, THROWING_KNIFE_HIT_BOX_PERCENT_SCALAR, hitBox);
    }

    /**
     * Create a throwing knife.
     * This knife will not be synced.
     * @param x
     * @param y
     * @param xDirection
     * @param yDirection
     * @param damage
     * @param speed
     */
    ThrowingKnife(float x, float y, float xDirection, float yDirection, int damage, float speed) {
        this(x,y,xDirection,yDirection,damage,speed,-1,-1);
    }

}
