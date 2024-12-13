package io.github.RashRogues;

import java.util.HashSet;

public class Bomber extends Enemy {

    private enum State {
        IDLE,
        WALK,
        ATTACK,
        WINDUP,
        COOLDOWN,
        RETREAT,
        DIE
    };

    private final int BASE_BOMBER_HEALTH = 40;
    private final int BASE_BOMBER_DAMAGE = 30;
    private final float BASE_BOMBER_ATTACK_SPEED = 0.1f;
    private final float BASE_BOMBER_MOVE_SPEED = 5f;
    private final float BASE_BOMBER_RETREAT_SPEED = 3.5f;
    private final float BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.4f;
    private final float BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.4f;
    private final float BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.55f;
    private final float BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.55f;
    private final int BOMB_DAMAGE = 200;
    private final float BOMB_DISTANCE = 10f;
    private final float BOMB_SPEED = 15f;
    private final float ATTENTION_DISTANCE = 25f; // Distance at which a player is worth moving
    private final int ATTENTION_SPAN = 60;
    private final float WINDUP_TIME = 0.9f;
    private final float COOLDOWN_TIME = 1.0f;
    private float STRIKING_DISTANCE = 10f;
    private float RETREAT_DISTANCE = 9f;
    private float RETREAT_THRESHOLD = 5f;

    private HashSet<Player> playerSet;
    private State state;

    private float attentionTimer;
    private float windupTimer;
    private float cooldownTimer;

    private Player target;

    Bomber(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_BOMBER_IMG), x, y, size, hasKey, AnimationActor.BOMBER);
        this.stats = new EnemyStats(BASE_BOMBER_HEALTH, BASE_BOMBER_DAMAGE, BASE_BOMBER_ATTACK_SPEED, BASE_BOMBER_MOVE_SPEED, BASE_BOMBER_RETREAT_SPEED, this);
        setBoxPercentSize(BOMBER_HIT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(BOMBER_HURT_BOX_PERCENT_WIDTH_SCALAR, BOMBER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);

        state = State.IDLE;
        this.playerSet = playerSet;
        attentionTimer = 0f;
        windupTimer    = 0f;
        cooldownTimer    = 0f;
    }


    private void move(boolean retreat) {
        float xDist = target.getX()+target.getWidth()/2-getX()-getWidth()/2;
        float yDist = target.getY()+target.getHeight()/2-getY()-getHeight()/2;
        float distanceToTarget = (float) Math.sqrt(Math.pow(xDist,2) + Math.pow(yDist,2));
        float speed = 0f;

        if (retreat){
            speed = stats.getRetreatSpeed();
        }else{
            speed = stats.getMoveSpeed();
        }


        // Move Towards Our Target
        if (Math.abs(xDist) > 0.1f) {
            xVelocity = speed * xDist / distanceToTarget;
        } else {
            xVelocity = 0f;
        }
        if (Math.abs(yDist) > 0.1f) {
            yVelocity = speed * yDist / distanceToTarget;
        } else {
            yVelocity = 0f;
        }

        if (retreat){
            xVelocity *= -1;
            yVelocity *= -1;
        }

        flipped = xVelocity < 0f;
    }

    private void attack() {
            float xDist = target.getX()+target.getWidth()/2 - this.getX()-this.getWidth()/2;
            float yDist = target.getY()+target.getHeight()/2 - this.getY()-this.getHeight()/2;
            float attackXDir = xDist / (float) Math.sqrt(Math.pow(xDist,2)+Math.pow(yDist,2));
            float attackYDir = yDist / (float) Math.sqrt(Math.pow(xDist,2)+Math.pow(yDist,2));
            new BomberBomb(getX(), getY(), attackXDir, attackYDir, BOMB_DISTANCE, BOMB_SPEED, BOMB_DAMAGE);
    }

    private float distanceTo(Player p) {
        float xd = p.getX() + p.getWidth() / 2 - getX() - getWidth() / 2;
        float yd = p.getY() + p.getHeight() / 2 - getY() - getHeight() / 2;
        return (float) (Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2)));
    }

    /**
     * Find a target.
     * @return Player target, or null if no suitable targets.
     */
    private void findTarget(){
        Player p = null;
        float closest = 99999999;
        for (Player player : playerSet) {
            if (player.stats.isDead()){
                continue;
            }
            float distToP = distanceTo(player);
            if (distToP < closest && distToP < ATTENTION_DISTANCE) {
                p       = player;
                closest = distToP;
            }
        }
        this.target = p;

        // Sync client to match the new target.
        if (RRGame.globals.pid == 0){
            int pid = -1;
            if (p != null){
                pid = p.associatedPID;
            }
            RRGame.globals.network.connection.dispatchTarget(id, pid);
        }
    }

    @Override
    public void setTarget(Player player) {
        Player newTarget = RRGame.globals.players.get(pid);

        // 1. We are already chasing this target
        if (newTarget == target){
            return;
        }

        // 2. Deselect current target.
        if (newTarget == null){
            this.state = State.IDLE;
            return;
        }

        // 3. New Target (server is correcting us.)
        System.out.println(">>! Warning: Client targeting differed from Server's and had to be corrected!");
        this.target = newTarget;
        this.state = State.WALK;
    }

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta){

        switch (state){
            case WALK:
               setCurrentAnimation(AnimationAction.MOVE);
               attentionTimer++;
                // Choose Target
                if (attentionTimer >= ATTENTION_SPAN) {
                    attentionTimer = 0;
                    findTarget();
                    // No Players Nearby -> Go idle
                    if (this.target == null) {
                        this.state = State.IDLE;
                        break;
                    }
                }

                // Too Close -> Retreat
                if (distanceTo(target) < RETREAT_THRESHOLD){
                    attentionTimer = 0;
                    state = State.RETREAT;
                    break;
                }

                // Close Enough -> Attack
                if (distanceTo(target) < STRIKING_DISTANCE) {
                    attentionTimer = 0;
                    xVelocity      = 0f;
                    yVelocity      = 0f;

                    // Turn towards player.
                    float xDist = target.getX()+target.getWidth()/2 - this.getX()-this.getWidth()/2;
                    if (Math.signum(xDist) == 1){
                        this.flipped = false;
                    }else{
                        this.flipped = true;
                    }

                    // Start Winding Up
                    state = State.WINDUP;
                    break;
                }

                move(false);

                break;

            case RETREAT:
                setCurrentAnimation(AnimationAction.MOVE);

                float dist = distanceTo(target);

                // Safe Distance -> Attack
                if (dist > RETREAT_DISTANCE) {
                    attentionTimer = 0;
                    xVelocity      = 0f;
                    yVelocity      = 0f;
                    state          = State.COOLDOWN;
                    break;
                }

                move(true);

                break;

            case IDLE:
                setCurrentAnimation(AnimationAction.IDLE);
                xVelocity = 0f;
                yVelocity = 0f;

                windupTimer    = 0;
                cooldownTimer  = 0;
                attentionTimer = 0;

                findTarget();
                if (this.target != null){
                    this.state = State.WALK;
                }
                break;

            case WINDUP:
                setCurrentAnimation(AnimationAction.ATTACK);
                windupTimer += delta;
                if (windupTimer >= WINDUP_TIME){
                    this.state = State.ATTACK;
                    break;
                }
                break;

            case COOLDOWN:
                setCurrentAnimation(AnimationAction.IDLE);
                cooldownTimer += delta;

                // Done Cooling Down -> Go Idle
                if (cooldownTimer >= COOLDOWN_TIME){
                    this.state = State.IDLE;
                    break;
                }

                float d = distanceTo(target);

                // Too Close -> Retreat
                if (d < RETREAT_DISTANCE){
                    state = State.RETREAT;
                    break;
                }

                break;

            case ATTACK:
                // Target Player Dead Or No Target -> go idle
                if (target == null || target.stats.isDead()){
                    state = State.IDLE;
                    break;
                }

                attack();

                state = State.COOLDOWN;

                break;

            case DIE:

                break;
        }
        super.update(delta);
    }

}
