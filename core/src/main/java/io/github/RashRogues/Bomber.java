package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Bomber extends Enemy {

    private final int BASE_BOMBER_HEALTH = 2;
    private final int BASE_BOMBER_DAMAGE = 30;
    private final float BASE_BOMBER_ATTACK_SPEED = 0.1f;
    private final float BASE_BOMBER_MOVE_SPEED = 12f;
    private final float BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.4f;
    private final float BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.4f;
    private final float BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.55f;
    private final float BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.55f;

    Bomber(float x, float y, float size) {
        super(EntityType.BOMBER, RRGame.am.get(RRGame.RSC_BOMBER_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_BOMBER_HEALTH, BASE_BOMBER_DAMAGE, BASE_BOMBER_ATTACK_SPEED, BASE_BOMBER_MOVE_SPEED, this);
        setBoxPercentSize(BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        super.update(delta);
        // do bomber pathfinding
    }

}
