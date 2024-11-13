package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Projectile extends Entity {
    // I think it might be worth separating this further into PlayerProjectile and EnemyProjectile.
    // This would make it so that enemies and players can check if the projectiles are friendly or not.

    // Melee Projectiles are just projectiles that don't move and only last a small amount of time (animation time)

    // when more projectiles are created, ideally follow the pattern in the enemy class and its subclasses.
    // for now, there's one projectile and it does 1 damage and doesn't move
    protected int damage;
    protected float moveSpeed;
    protected boolean removeNextUpdate = false;

    Projectile(Texture texture, int x, int y, float width, float height) {
        super(EntityType.PROJECTILE, texture, x, y, width, height, Layer.PROJECTILE);
        this.damage = 1;
        this.moveSpeed = 0.0f;
    }

    /**
     * Polymorphic: called by parent class.
     * Game logic. runs every frame.
     * @param delta
     */
    public void updateEntity(float delta) {

    }

    @Override
    public void onHit(Entity thingHit) {
        removeNextUpdate = true;
    }

}
