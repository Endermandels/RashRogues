package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Bomber extends Enemy {

    private final int BASE_BOMBER_HEALTH = 2;
    private final int BASE_BOMBER_DAMAGE = 30;
    private final float BASE_BOMBER_ATTACK_SPEED = 0.1f;
    private final float BASE_BOMBER_MOVE_SPEED = 12f;

    Bomber(Texture texture, int x, int y, float size) {
        super(EntityType.BOMBER, texture, x, y, size);
        this.stats = new EnemyStats(BASE_BOMBER_HEALTH, BASE_BOMBER_DAMAGE, BASE_BOMBER_ATTACK_SPEED, BASE_BOMBER_MOVE_SPEED, this);
    }
}
