package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Swordsman extends Enemy {

    private final int BASE_SWORDSMAN_HEALTH = 10;
    private final int BASE_SWORDSMAN_DAMAGE = 10;
    private final float BASE_SWORDSMAN_ATTACK_SPEED = 0.5f;
    private final float BASE_SWORDSMAN_MOVE_SPEED = 3f;

    Swordsman(Texture texture, int x, int y, float size) {
        super(EntityType.SWORDSMAN, texture, x, y, size);
        this.stats = new EnemyStats(BASE_SWORDSMAN_HEALTH, BASE_SWORDSMAN_DAMAGE, BASE_SWORDSMAN_ATTACK_SPEED, BASE_SWORDSMAN_MOVE_SPEED, this);
        setBoxPercentSize(0.4f, 0.4f, hitBox);
        setBoxPercentSize(0.58f, 0.58f, hurtBox);
    }

    /**
     * Update gamelogic for this object. Ran per frame.
     * @param delta
     */
    public void updateEnemy(float delta) {

    }
}
