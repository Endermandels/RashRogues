package io.github.RashRogues;

import com.badlogic.gdx.audio.Sound;

import java.util.HashSet;
import java.util.Random;

public class Merchant extends Enemy {

    private HashSet<Player> playerSet;

    private final float MERCHANT_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.66f;
    private final float MERCHANT_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.66f;
    private final float MERCHANT_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.45f;
    private final float MERCHANT_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.65f;

    Merchant(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_ARCHER_IMG), x, y, size, false, AnimationActor.MERCHANT);
        setBoxPercentSize(MERCHANT_HIT_BOX_PERCENT_WIDTH_SCALAR, MERCHANT_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(MERCHANT_HURT_BOX_PERCENT_WIDTH_SCALAR, MERCHANT_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);
        this.stats = new EnemyStats(999999, 0, 0,0, this);
        this.playerSet = playerSet;
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        this.setCurrentAnimation(AnimationAction.IDLE);
        super.update(delta);
    }

}
