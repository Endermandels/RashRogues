package io.github.RashRogues;

import java.util.HashSet;

public class Bomber extends Enemy {

    private enum State {
        WALK,
        ATTACK,
        DIE
    };

    private final int BASE_BOMBER_HEALTH = 200;
    private final int BASE_BOMBER_DAMAGE = 30;
    private final float BASE_BOMBER_ATTACK_SPEED = 0.1f;
    private final float BASE_BOMBER_MOVE_SPEED = 2f;
    private final float BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.4f;
    private final float BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.4f;
    private final float BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.55f;
    private final float BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.55f;
    private final int BOMB_DAMAGE = 100;
    private final float BOMB_DISTANCE = 10f;
    private final float BOMB_SPEED = 10f;
    private final float BOMB_FUSE_DURATION = 1f;


    private HashSet<Player> playerSet;
    private State state;

    private final float attackTimerMax = 1.2f;
    private final float windupTime = attackTimerMax * 11 / 18;
    private boolean coolingDown = false;
    private float attackTimer;
    private float attackXDir, attackYDir;

    Bomber(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_BOMBER_IMG), x, y, size, hasKey, AnimationActor.BOMBER);
        this.stats = new EnemyStats(BASE_BOMBER_HEALTH, BASE_BOMBER_DAMAGE, BASE_BOMBER_ATTACK_SPEED, BASE_BOMBER_MOVE_SPEED, this);
        setBoxPercentSize(BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);

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

        if (Math.sqrt(xDist*xDist + yDist*yDist) < 8f) {
            // Too close, back up
            if (Math.abs(xDist) > 0.1f)
                xVelocity = stats.getMoveSpeed() * Math.signum(-xDist);
            else
                xVelocity = 0f;
            if (Math.abs(yDist) > 0.1f)
                yVelocity = stats.getMoveSpeed() * Math.signum(-yDist);
            else
                yVelocity = 0f;
            flipped = xVelocity < 0f;
        } else if (Math.sqrt(xDist*xDist + yDist*yDist) < 10f) {
            // Start shooting
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
        attackTimer += delta;
        // again this will need to be changed when we use stats, whenever we level up we need to
        // recalculate the windup time bc it's just 11/18 of whatever the attack speed is
        if (attackTimer > windupTime && !coolingDown) {
            // Spawn bomb
            new BomberBomb(getX(), getY(), attackXDir, attackYDir, BOMB_DISTANCE, BOMB_SPEED, BOMB_DAMAGE);
            coolingDown = true;
        }
        if (attackTimer > attackTimerMax) {
            attackTimer = 0f;
            coolingDown = false;
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
            this.setCurrentAnimation(AnimationAction.ATTACK);
            attack(delta);
        }
        super.update(delta);
    }

}
