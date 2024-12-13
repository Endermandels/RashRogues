package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Random;

public abstract class Enemy extends Entity {

    public static final int MAX_DROP_DISTANCE = 3;

    public EnemyStats stats;
    private int enemyLevel;
    protected HurtBox hurtBox;
    protected boolean hasKey;
    private Sprite keySprite;
    private float deathTimer = 0f;
    private Random rnd;
    private Sound hurtSFX;
    private float smokeTimer = 0f;
    private final float POSSIBLE_CHANGE_DIR_TIME = 0.7f;
    private boolean oppositeX = false;
    private boolean oppositeY = false;

    Enemy(Texture texture, float x, float y, float width, float height, boolean hasKey, AnimationActor animationActor) {
        super(EntityAlignment.ENEMY, texture, x, y, width, height, Layer.ENEMY, animationActor,
                ReplicationType.ENTITY_NUMBER,-1,-1);
        this.enemyLevel = 1;
        hurtBox = new HurtBox(hitBox, this);
        this.hasKey = hasKey;
        this.keySprite = new Sprite(RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class));
        this.keySprite.setSize(width/2, height/2);
        this.keySprite.setOrigin(width/4, height/4);
        rnd = RRGame.globals.getRandom();
        hurtSFX = RRGame.am.get(RRGame.RSC_HURT_ENEMY_SFX);
        // this will obviously change based on a number of factors later
    }

    Enemy(Texture texture, float x, float y, float size, boolean hasKey, AnimationActor animationActor) {
        this(texture, x, y, size, size, hasKey, animationActor);
    }

    public abstract void setTarget(Player player);

    /**
     * Ran every frame.
     * @param delta
     */
    public void update(float delta) {
        if (this.hasEffect(Effect.SMOKE)) {
            smokeTimer += delta;
            if (oppositeX) {
                xVelocity *= -1;
            }
            if (oppositeY) {
                yVelocity *= -1;
            }
            if (smokeTimer > POSSIBLE_CHANGE_DIR_TIME) {
                smokeTimer = 0f;
                if (rnd.nextBoolean()) {
                    oppositeX = true;
                }
                else {
                    oppositeX = false;
                }
                if (rnd.nextBoolean()) {
                    oppositeY = true;
                }
                else {
                    oppositeY = false;
                }
            }
        }
        flipped = xVelocity < 0f;
        super.update(delta);
        if (deathTimer >= RRGame.STANDARD_DEATH_DURATION) { this.dropKey(); this.dropCoins(); this.removeSelf(); return; }
        if (stats.isDead()) { deathTimer += delta; return; }
        hurtBox.update(delta);
        keySprite.setX(getX()+getWidth()/4);
        keySprite.setY(getY()+getHeight()/4);
    }

    public void dropKey(){
        if ((hasKey) && (RRGame.globals.pid == 0)) {
            hasKey = false;
            RRGame.globals.network.connection.dispatchKeyDrop(getX(),getY());
            new Key(getX(),getY());
        }
    }

    /**
     * Drop coins of an amount equal to or less than twice this Enemy's level
     */
    public void dropCoins() {
        if (RRGame.globals.pid == 0){
            int numCoin = rnd.nextInt(2*(enemyLevel+1));
            for (int ii = 0; ii < numCoin; ii++) {
                float x = (rnd.nextInt(MAX_DROP_DISTANCE) - 1) / 2;
                float y = (rnd.nextInt(MAX_DROP_DISTANCE) - 1) / 2;
                new Coin(getX() + x, getY() + y);
            }
            RRGame.globals.network.connection.dispatchCoinDrop(getX(),getY(), enemyLevel);
        }
    }

    /**
     * Make this enemy stronger
     */
    public void levelUpEnemy() {
        stats.increaseHealth(3);
        stats.increaseDamage(3);
        stats.increaseAttackSpeed(0.2f);
        stats.increaseMoveSpeed(0.2f);
        this.enemyLevel++;
    }

    @Override
    public void onHit(Entity thingHit) {
        // maybe have the enemy back away? it doesn't really care if it hits anything, it just does the damage
        return;
    }

    @Override
    public void onHurt(Entity thingThatHurtMe) {
        boolean tookDamage = false;
        if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.PLAYER) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
            tookDamage = true;
            hurtSFX.play(0.8f, rnd.nextFloat(0.5f, 1f), 0);
        }
        else {
            // if an enemy hitBox is what hurt us, then ignore it
            return;
        }

        if (stats.isDead()) {
            // sound
            this.setCurrentAnimation(AnimationAction.DIE);
        }
        else if (tookDamage) {
            // sound
            this.setCurrentAnimation(AnimationAction.HURT);
        }
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        if (hasKey) {
            keySprite.draw(batch);
        }
    }
}
