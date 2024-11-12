package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Enemy extends Entity {

    protected EnemyStats stats;
    protected HurtBox hurtBox;

    Enemy(Texture texture, int x, int y, float width, float height) {
        super(texture, x, y, width, height);
        hurtBox = new HurtBox(hitBox, this);
        // this will obviously change based on a number of factors later
    }

    public void update(float delta) {
        super.update(delta);
        hurtBox.update(delta);
    }

    Enemy(Texture texture, int x, int y, float size) {
        this(texture, x, y, size, size);
    }

    protected void levelUpEnemy() {
        stats.health += 3;
        stats.damage += 3;
        stats.attackSpeed += 0.2f;
        stats.moveSpeed += 0.5f;
    }

    @Override
    public void onHit(Entity thingHit) {
        // maybe have the enemy back away? it doesn't really care if it hits anything, it just does the damage
        return;
    }

    @Override
    public void onHurt(Entity thingThatHurtMe) {
        if (thingThatHurtMe instanceof Projectile) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
        }
        else if (thingThatHurtMe instanceof Player) { // this should actually be a Projectile as well
            this.stats.takeDamage(((Player) thingThatHurtMe).stats.damage);
        }
        else {
            // if an enemy hitBox is what hurt us, then ignore it
            return;
        }
    }
}
