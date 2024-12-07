package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class MeleeAttack extends Projectile {

    MeleeAttack(EntityAlignment ea, Texture texture, float x, float y, float width, float height,
                int damage, float duration) {
        super(ea, texture, x, y, width, height, damage,
                0f, false, duration);
        this.setBoxPercentSize(0.5f, 0.5f, hitBox);
    }
}
