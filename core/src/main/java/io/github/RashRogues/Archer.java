package io.github.RashRogues;

import java.util.HashSet;

public class Archer extends Enemy {

    private enum State {
        IDLE,
        WALK,
        ATTACK,
        RETREAT,
        BREATHER,
        DIE
    };

    private final int BASE_ARCHER_HEALTH = 5;
    private final int BASE_ARCHER_DAMAGE = 5;
    private final float BASE_ARCHER_ATTACK_SPEED = 0.9f;
    private final float BASE_ARCHER_MOVE_SPEED = 3f;
    private final float BASE_ARCHER_RETREAT_SPEED = 4f;
    private final float ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR = 0.25f;
    private final float ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR = 0.45f;
    private final float ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR = 0.65f;
    private final int ARROW_DAMAGE = 30;
    private final int ATTENTION_SPAN = 90;
    private final float ATTACK_TIME_MAX = 0.7f;
    private float STRIKING_DISTANCE = 10f;
    private float RETREAT_DISTANCE  = 8f;
    private float RETREAT_THRESHOLD = 4f;
    private float BREATHER_TIME = 0.25f;

    private HashSet<Player> playerSet;
    private State state;

    private float attackTimer;
    private float attentionTimer;
    private float breatherTimer;

    private Player target;

    Archer(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_ARCHER_IMG), x, y, size, hasKey);
        this.stats = new EnemyStats(BASE_ARCHER_HEALTH, BASE_ARCHER_DAMAGE, BASE_ARCHER_ATTACK_SPEED, BASE_ARCHER_MOVE_SPEED, BASE_ARCHER_RETREAT_SPEED, this);
        setBoxPercentSize(ARCHER_HIT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HIT_BOX_PERCENT_HEIGHT_SCALAR, hitBox);
        setBoxPercentSize(ARCHER_HURT_BOX_PERCENT_WIDTH_SCALAR, ARCHER_HURT_BOX_PERCENT_HEIGHT_SCALAR, hurtBox);

        state = State.IDLE;
        this.playerSet = playerSet;
        attackTimer    = 0f;
        attentionTimer = 0f;
        breatherTimer  = 0f;
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

    private void attack(float delta) {
        attackTimer += delta;

        if (attackTimer > ATTACK_TIME_MAX) {

            float xDist = target.getX()+target.getWidth()/2 - this.getX()-this.getWidth()/2;
            float yDist = target.getY()+target.getHeight()/2 - this.getY()-this.getHeight()/2;
            float attackXDir = xDist / (float) Math.sqrt(Math.pow(xDist,2)+Math.pow(yDist,2));
            float attackYDir = yDist / (float) Math.sqrt(Math.pow(xDist,2)+Math.pow(yDist,2));

            if (Math.signum(attackXDir) == 1){
                this.flipped = false;
            }else{
                this.flipped = true;
            }

            new Arrow(getX(), getY(), attackXDir, attackYDir, ARROW_DAMAGE,
                    RRGame.STANDARD_PROJECTILE_SPEED, this.id);
            attackTimer = 0f;
        }
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
            if (distToP < closest) {
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

                if (target.stats.isDead()){
                    this.state = State.IDLE;
                    break;
                }

                // Choose Target
                if (attentionTimer >= ATTENTION_SPAN){
                    attentionTimer = 0;

                    findTarget();

                    // No Players Nearby -> Go idle
                    if (this.target == null){
                        this.state = State.IDLE;
                        break;
                    }
                }else{
                    attentionTimer++;
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
                    state          = State.ATTACK;
                    break;
                }

                move(false);

                break;

            case RETREAT:

                float dist = distanceTo(target);

                // Safe Distance -> Attack
                if (dist > RETREAT_DISTANCE) {
                    attentionTimer = 0;
                    xVelocity      = 0f;
                    yVelocity      = 0f;
                    state = State.BREATHER;
                    break;
                }

                move(true);

                break;

            case IDLE:
                findTarget();
                if (this.target != null){
                    this.state = State.WALK;
                }
                break;

            case ATTACK:

                // Target Player Dead -> go idle
                if (target.stats.isDead()){
                    state = State.IDLE;
                    break;
                }

                // Too Close -> Retreat
                if (distanceTo(target) < RETREAT_THRESHOLD){
                    attentionTimer = 0;
                    state = State.RETREAT;
                    break;
                }

                // Too Far -> Advance
                if (distanceTo(target) >= STRIKING_DISTANCE){
                    state = State.WALK;
                    attackTimer = 0;
                    break;
                }

                attack(delta);

                break;

            case DIE:

                break;

            case BREATHER:
                if (this.breatherTimer < this.BREATHER_TIME){
                    this.breatherTimer+=delta;
                }else{
                    state = State.WALK;
                    this.breatherTimer = 0;
                }
                break;
        }
        super.update(delta);
    }

}
