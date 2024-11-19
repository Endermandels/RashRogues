package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Archer extends Enemy {

    private final int BASE_ARCHER_HEALTH = 5;
    private final int BASE_ARCHER_DAMAGE = 5;
    private final float BASE_ARCHER_ATTACK_SPEED = 0.9f;
    private final float BASE_ARCHER_MOVE_SPEED = 3f;

    Archer(Texture texture, int x, int y, float size) {
        super(EntityType.ARCHER, texture, x, y, size);
        this.stats = new EnemyStats(BASE_ARCHER_HEALTH, BASE_ARCHER_DAMAGE, BASE_ARCHER_ATTACK_SPEED, BASE_ARCHER_MOVE_SPEED, this);
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
