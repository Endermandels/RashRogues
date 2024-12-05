package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Swordsman extends Enemy {

    private enum State {
        WALK,
        ATTACK,
        DIE
    };

    private final int BASE_SWORDSMAN_HEALTH = 10;
    private final int BASE_SWORDSMAN_DAMAGE = 10;
    private final int ATTACK_DAMAGE = 50;
    private final float ATTACK_DURATION = 0.5f;
    private final float BASE_SWORDSMAN_ATTACK_SPEED = 0.5f;
    private final float BASE_SWORDSMAN_MOVE_SPEED = 0f;
    private final float SWORDSMAN_HIT_BOX_PERCENT_SCALAR = 0.4f;
    private final float SWORDSMAN_HURT_BOX_PERCENT_SCALAR = 0.58f;

    private Player player;
    private State state;

    private final float attackTimerMax = 1f;
    private float attackTimer;

    Swordsman(float x, float y, float size, Player player) {
        super(RRGame.am.get(RRGame.RSC_SWORDSMAN_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_SWORDSMAN_HEALTH, BASE_SWORDSMAN_DAMAGE, BASE_SWORDSMAN_ATTACK_SPEED, BASE_SWORDSMAN_MOVE_SPEED, this);
        setBoxPercentSize(SWORDSMAN_HIT_BOX_PERCENT_SCALAR, SWORDSMAN_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(SWORDSMAN_HURT_BOX_PERCENT_SCALAR, SWORDSMAN_HURT_BOX_PERCENT_SCALAR, hurtBox);
        this.player = player;
        state = State.WALK;
        attackTimer = 0f;
    }

    private void move() {
        // Get distance from player
        float xDist = player.getX()+player.getWidth()/2-getX()-getWidth()/2;
        float yDist = player.getY()+player.getHeight()/2-getY()-getHeight()/2;
        // Detect if the swordsman is within striking distance
        if (Math.abs(xDist) < 5f && Math.abs(yDist) < 5f) {
            // Strike
            xVelocity = 0f;
            yVelocity = 0f;
            state = State.ATTACK;
        } else {
            // Move towards player
            xVelocity = stats.getMoveSpeed() * Math.signum(xDist);
            yVelocity = stats.getMoveSpeed() * Math.signum(yDist);
            flipped = xVelocity < 0;
        }
    }

    private void attack(float delta) {
        // TODO: Play attack animation
        attackTimer += delta;
        if (attackTimer > attackTimerMax) {
            // Create melee attack
            float xOff;
            if (flipped) xOff = -5f;
            else xOff = 5f;
            new MeleeAttack(EntityAlignment.ENEMY,
                    RRGame.am.get(RRGame.RSC_SMOKE_BOMB_EXPLOSION_IMG),
                    getX() + xOff, getY(),
                    RRGame.SMOKE_BOMB_EXPLOSION_SIZE, RRGame.SMOKE_BOMB_EXPLOSION_SIZE,
                    ATTACK_DAMAGE, ATTACK_DURATION);
            attackTimer = 0f;
            state = State.WALK;
        }
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){
        if (state == State.WALK){
            move();
        } else if (state == State.ATTACK) {
            attack(delta);
        }
        super.update(delta);
    }

}
