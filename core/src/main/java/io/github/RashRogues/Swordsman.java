package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Swordsman extends Enemy {

    private final int BASE_SWORDSMAN_HEALTH = 10;
    private final int BASE_SWORDSMAN_DAMAGE = 10;
    private final float BASE_SWORDSMAN_ATTACK_SPEED = 0.5f;
    private final float BASE_SWORDSMAN_MOVE_SPEED = 3f;
    private final float SWORDSMAN_HIT_BOX_PERCENT_SCALAR = 0.4f;
    private final float SWORDSMAN_HURT_BOX_PERCENT_SCALAR = 0.58f;

    private Player player;

    Swordsman(float x, float y, float size, Player player) {
        super(RRGame.am.get(RRGame.RSC_SWORDSMAN_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_SWORDSMAN_HEALTH, BASE_SWORDSMAN_DAMAGE, BASE_SWORDSMAN_ATTACK_SPEED, BASE_SWORDSMAN_MOVE_SPEED, this);
        setBoxPercentSize(SWORDSMAN_HIT_BOX_PERCENT_SCALAR, SWORDSMAN_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(SWORDSMAN_HURT_BOX_PERCENT_SCALAR, SWORDSMAN_HURT_BOX_PERCENT_SCALAR, hurtBox);
        this.player = player;
    }

    private void move(float delta) {
        // Move towards player
        xVelocity = maxXVelocity * Math.signum(player.getX()+player.getWidth()/2-getX()-getWidth()/2);
        yVelocity = maxYVelocity * Math.signum(player.getY()+player.getHeight()/2-getY()-getHeight()/2);
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        move(delta);
        super.update(delta);
    }

}
