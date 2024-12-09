package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashSet;

public class Archer extends Enemy {

    private enum State {
        WALK,
        ATTACK,
        DIE
    };

    private final int BASE_ARCHER_HEALTH = 5;
    private final int BASE_ARCHER_DAMAGE = 5;
    private final float BASE_ARCHER_ATTACK_SPEED = 0.9f;
    private final float BASE_ARCHER_MOVE_SPEED = 3f;
    private final float ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.25f;
    private final float ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.65f;
    private final int ARROW_DAMAGE = 30;

    private HashSet<Player> playerSet;
    private State state;

    private final float attackTimerMax = 0.6f;
    private float attackTimer;
    private float attackXDir, attackYDir;

    Archer(float x, float y, float size, HashSet<Player> playerSet) {
        super(RRGame.am.get(RRGame.RSC_ARCHER_IMG), x, y, size);
        this.stats = new EnemyStats(BASE_ARCHER_HEALTH, BASE_ARCHER_DAMAGE, BASE_ARCHER_ATTACK_SPEED, BASE_ARCHER_MOVE_SPEED, this);
        setBoxPercentSize(ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);

        state = State.WALK;
        this.playerSet = playerSet;
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
        if (Math.abs(xDist) < 20f && Math.abs(yDist) < 20f) {
            // Strike
            xVelocity = 0f;
            yVelocity = 0f;
            attackXDir = xDist / (float) Math.sqrt(xDist*xDist + yDist*yDist);
            attackYDir = yDist / (float) Math.sqrt(xDist*xDist + yDist*yDist);
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
            // Spawn arrow
            new Arrow(getX(), getY(), attackXDir, attackYDir, ARROW_DAMAGE,
                    RRGame.STANDARD_PROJECTILE_SPEED);
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
