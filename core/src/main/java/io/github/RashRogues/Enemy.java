package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public abstract class Enemy extends Entity {

    protected EnemyStats stats;
    protected HurtBox hurtBox;
    // early thinking is that there can be a variable named "hasKey" and would drop the key, idk. determined in Room.

    Enemy(Texture texture, float x, float y, float width, float height) {
        super(EntityAlignment.ENEMY, texture, x, y, width, height, Layer.ENEMY,false);
        hurtBox = new HurtBox(hitBox, this);
        // this will obviously change based on a number of factors later
    }

    Enemy(Texture texture, float x, float y, float size) {
        this(texture, x, y, size, size);
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta) {
        if (stats.isDead()) { this.removeSelf(); return; }
        super.update(delta);
        hurtBox.update(delta);
    }

    /**
     * Make this enemy stronger
     */
    public void levelUpEnemy() {
        stats.increaseHealth(3);
        stats.increaseDamage(3);
        stats.increaseAttackSpeed(0.2f);
        stats.increaseMoveSpeed(0.2f);
    }

    @Override
    public void onHit(Entity thingHit) {
        // maybe have the enemy back away? it doesn't really care if it hits anything, it just does the damage
        return;
    }

    @Override
    public void onHurt(Entity thingThatHurtMe) {
        if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.PLAYER) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
        }
        else {
            // if an enemy hitBox is what hurt us, then ignore it
            return;
        }
    }

}
