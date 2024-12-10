package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashSet;

public class Swordsman extends Enemy {

    private enum State {
        WALK,
        ATTACK,
        DIE
    };

    private final int BASE_SWORDSMAN_HEALTH = 100;
    private final int BASE_SWORDSMAN_DAMAGE = 10;
    private final int ATTACK_DAMAGE = 50;
    private final float ATTACK_DURATION = 0.5f;
    private final float BASE_SWORDSMAN_ATTACK_SPEED = 0.5f;
    private final float BASE_SWORDSMAN_MOVE_SPEED = 1.5f;
    private final float SWORDSMAN_HIT_BOX_PERCENT_SCALAR = 0.4f;
    private final float SWORDSMAN_HURT_BOX_PERCENT_SCALAR = 0.58f;

    private HashSet<Player> playerSet;
    private State state;

    private final float attackTimerMax = 1f;
    private float attackTimer;
    private float attackX, attackY;

    Swordsman(float x, float y, float size, HashSet<Player> playerSet) {
        super(RRGame.am.get(RRGame.RSC_SWORDSMAN_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_SWORDSMAN_HEALTH, BASE_SWORDSMAN_DAMAGE, BASE_SWORDSMAN_ATTACK_SPEED, BASE_SWORDSMAN_MOVE_SPEED, this);
        setBoxPercentSize(SWORDSMAN_HIT_BOX_PERCENT_SCALAR, SWORDSMAN_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(SWORDSMAN_HURT_BOX_PERCENT_SCALAR, SWORDSMAN_HURT_BOX_PERCENT_SCALAR, hurtBox);

        this.playerSet = playerSet;
        state = State.WALK;
        attackTimer = 0f;
    }

    private void move() {
        // Get distance from each player
        float xDist = 1000000000f;
        float yDist = 1000000000f;
        Player p = null;
        for (Player player : playerSet) {
            if (player.stats.isDead()) continue;
            float xd = player.getX()+player.getWidth()/2-getX()-getWidth()/2;
            float yd = player.getY()+player.getHeight()/2-getY()-getHeight()/2;
            if (xd*xd + yd*yd < xDist*xDist + yDist*yDist) {
                // player is closer using euclidean distance
                xDist = xd;
                yDist = yd;
                p = player;
            }
        }
        if (p == null) return;
        // Detect if the swordsman is within striking distance
        if (Math.sqrt(xDist*xDist + yDist*yDist) < 4f) {
            // Strike
            xVelocity = 0f;
            yVelocity = 0f;
            attackX = p.getX();
            attackY = p.getY();
            state = State.ATTACK;
        } else {
            // Move towards player
            if (Math.abs(xDist) > 0.1f)
                xVelocity = stats.getMoveSpeed() * Math.signum(xDist);
            else
                xVelocity = 0f;
            if (Math.abs(yDist) > 0.1f)
                yVelocity = stats.getMoveSpeed() * Math.signum(yDist);
            else
                yVelocity = 0f;
            flipped = xVelocity < 0f;
        }
    }

    private void attack(float delta) {
        // TODO: Play attack animation
        attackTimer += delta;
        if (attackTimer > attackTimerMax) {
            // Create melee attack
            new MeleeAttack(EntityAlignment.ENEMY,
                    RRGame.am.get(RRGame.RSC_SMOKE_BOMB_EXPLOSION_IMG),
                    attackX-5, attackY-3,
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
