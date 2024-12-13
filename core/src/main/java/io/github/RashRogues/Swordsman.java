package io.github.RashRogues;

import com.badlogic.gdx.audio.Sound;

import java.util.HashSet;
import java.util.Random;

public class Swordsman extends Enemy {

    private enum State {
        IDLE,
        WALK,
        ATTACK,
        RETREAT,
        DIE
    };

    private final int BASE_SWORDSMAN_HEALTH = 50;
    private final int BASE_SWORDSMAN_DAMAGE = 30;
    private final int ATTACK_DAMAGE = 100;
    private final float BASE_SWORDSMAN_ATTACK_SPEED = 0.5f;
    private final float BASE_SWORDSMAN_RETREAT_SPEED = 1f;
    private final float BASE_SWORDSMAN_MOVE_SPEED = 4f;
    private final float SWORDSMAN_HIT_BOX_PERCENT_SCALAR = 0.4f;
    private final float SWORDSMAN_HURT_BOX_PERCENT_SCALAR = 0.58f;
    private final float STRIKING_DISTANCE = 4f;
    private final float ATTENTION_DISTANCE = 20f; // Distance at which a player is worth moving
    private final int ATTENTION_SPAN = 60;
    private final float ATTACK_TIME_MAX = 0.7f;

    private HashSet<Player> playerSet;
    private State state;

    private float attackTimer;
    private float attentionTimer;
    private float attackX, attackY;

    private Player target;

    private Random rnd;
    private Sound swipeSFX;

    Swordsman(float x, float y, float size, HashSet<Player> playerSet, boolean hasKey) {
        super(RRGame.am.get(RRGame.RSC_SWORDSMAN_IMG), x, y, size, hasKey, AnimationActor.SWORDSMAN);
        this.stats = new EnemyStats(BASE_SWORDSMAN_HEALTH, BASE_SWORDSMAN_DAMAGE, BASE_SWORDSMAN_ATTACK_SPEED, BASE_SWORDSMAN_MOVE_SPEED, BASE_SWORDSMAN_RETREAT_SPEED, this);
        setBoxPercentSize(SWORDSMAN_HIT_BOX_PERCENT_SCALAR, SWORDSMAN_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(SWORDSMAN_HURT_BOX_PERCENT_SCALAR, SWORDSMAN_HURT_BOX_PERCENT_SCALAR, hurtBox);

        this.playerSet = playerSet;
        state = State.IDLE;
        attackTimer    = 0f;
        attentionTimer = 0f;
        swipeSFX = RRGame.am.get(RRGame.RSC_SWORD_SWIPE_SFX);
        rnd = RRGame.globals.getRandom();

    }

    private void move() {
        float xDist = target.getX()+target.getWidth()/2-getX()-getWidth()/2;
        float yDist = target.getY()+target.getHeight()/2-getY()-getHeight()/2;
        float distanceToTarget = (float) Math.sqrt(Math.pow(xDist,2) + Math.pow(yDist,2));

        // Move Towards Our Target
        if (Math.abs(xDist) > 0.1f) {
            xVelocity = stats.getMoveSpeed() * xDist / distanceToTarget;
        } else {
            xVelocity = 0f;
        }
        if (Math.abs(yDist) > 0.1f) {
            yVelocity = stats.getMoveSpeed() * yDist / distanceToTarget;
        } else {
                yVelocity = 0f;
        }
        flipped = xVelocity < 0f;
    }

    private void attack(float delta) {
        attackTimer += delta;

        if (attackTimer > ATTACK_TIME_MAX) {
            // Create melee attack
            new SwordsmanSwing(attackX, attackY, ATTACK_DAMAGE);
            attackTimer = 0f;
            swipeSFX.play(0.2f,rnd.nextFloat(0.5f, 2.0f),0);

            //after each attack, see if there is a better target available.
            this.findTarget();
        }
    }

    private float distanceTo(Player p){
        float xd = p.getX()+p.getWidth()/2-getX()-getWidth()/2;
        float yd = p.getY()+p.getHeight()/2-getY()-getHeight()/2;
        return (float) (Math.sqrt(Math.pow(xd,2) + Math.pow(yd,2)));
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

        if (p == null) return;

        attackX = target.getX() - RRGame.SWORDSMAN_SWING_SIZE/2;
        attackY = target.getY() - RRGame.SWORDSMAN_SWING_SIZE/2;
    }

    @Override
    public void setTarget(Player player){
        Player newTarget = RRGame.globals.players.get(pid);
        if (newTarget == target){
            return;
        }

        if (newTarget == null){
            this.state = State.IDLE;
            return;
        }
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
                this.setCurrentAnimation(AnimationAction.MOVE);

                // If Attention Span Is Up -> Choose Target
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

                // Check If We Are Close Enough To Attack Target
                if (distanceTo(target) < STRIKING_DISTANCE) {
                    attentionTimer = 0;
                    xVelocity      = 0f;
                    yVelocity      = 0f;
                    attackX        = target.getX() - RRGame.SWORDSMAN_SWING_SIZE/2;
                    attackY        = target.getY() - RRGame.SWORDSMAN_SWING_SIZE/2;
                    state          = State.ATTACK;
                    break;
                }

                move();

                break;

            case IDLE:
                this.setCurrentAnimation(AnimationAction.IDLE);
                xVelocity = 0f;
                yVelocity = 0f;
                findTarget();
                if (this.target != null){
                    this.state = State.WALK;
                }
                break;

            case ATTACK:

                if (target == null || target.stats.isDead()){
                    state = State.IDLE;
                    break;
                }
                if (distanceTo(target) >= STRIKING_DISTANCE && attackTimer == 0f){
                    state = State.WALK;
                    break;
                }

                this.setCurrentAnimation(AnimationAction.ATTACK);

                attack(delta);

                break;

            case DIE:

                break;

        }

        super.update(delta);
    }

}
