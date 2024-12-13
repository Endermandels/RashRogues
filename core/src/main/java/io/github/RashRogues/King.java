package io.github.RashRogues;

import java.util.HashSet;

public class King extends Enemy {

    private HashSet<Player> playerSet;

    private final float KING_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.66f;
    private final float KING_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.66f;
    private final float KING_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.45f;
    private final float KING_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.65f;

    King(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_ARCHER_IMG), x, y, size, hasKey, AnimationActor.KING);
        setBoxPercentSize(KING_HIT_BOX_PERCENT_WIDTH_SCALAR, KING_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(KING_HURT_BOX_PERCENT_WIDTH_SCALAR, KING_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);
        this.stats = new EnemyStats(1, 0, 0,0,0, this);
        this.playerSet = playerSet;
        this.setCurrentAnimation(AnimationAction.OPEN);
        this.enemyLevel = 1;
    }

    @Override
    public void setTarget(Player player) {
        return;
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        super.update(delta);
    }

    @Override
    public void dropCoins() {
        super.dropCoins();
        Chest chest = new Chest(this.getX(), this.getY());
        chest.AMOUNT_NEEDED_TO_OPEN = 0f;
        chest.coinValue = 1000;
    }
}
