package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Archer extends Enemy {

    private final int BASE_ARCHER_HEALTH = 5;
    private final int BASE_ARCHER_DAMAGE = 5;
    private final float BASE_ARCHER_ATTACK_SPEED = 0.9f;
    private final float BASE_ARCHER_MOVE_SPEED = 3f;
    private final float ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.25f;
    private final float ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.65f;

    Archer(float x, float y, float size) {
        super(RRGame.am.get(RRGame.RSC_ARCHER_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_ARCHER_HEALTH, BASE_ARCHER_DAMAGE, BASE_ARCHER_ATTACK_SPEED, BASE_ARCHER_MOVE_SPEED, this);
        setBoxPercentSize(ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        super.update(delta);
        // do archer pathfinding
    }

}
