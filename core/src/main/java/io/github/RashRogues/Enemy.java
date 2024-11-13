package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public abstract class Enemy extends Entity {

    protected EnemyStats stats;
    protected HurtBox hurtBox;

    Enemy(EntityType type, Texture texture, int x, int y, float width, float height) {
        super(type, texture, x, y, width, height, Layer.ENEMY);
        hurtBox = new HurtBox(hitBox, this);
        // this will obviously change based on a number of factors later
    }

    Enemy(EntityType type, Texture texture, int x, int y, float size) {
        this(type, texture, x, y, size, size);
    }

    /**
     * Ran every frame.
     * @param delta
     */
    private void Update(float delta){
        hurtBox.update(delta);

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

    public abstract void updateEnemy(float delta);

    public void updateEntity(float delta) {this.Update(delta);this.updateEnemy(delta);}

}
