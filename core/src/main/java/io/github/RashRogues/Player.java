package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

import static java.lang.Math.abs;

public class Player extends Entity {

    private final int BASE_PLAYER_HEALTH = 500;
    private final int BASE_PLAYER_DAMAGE = 10000;
    private final float BASE_PLAYER_ATTACK_SPEED = 0.5f;
    private final float ACCELERATION = 50.0f;
    private final float FRICTION = 25.0f;
    private final float BASE_PLAYER_MOVE_SPEED = 15.0f;
    private final float BASE_PLAYER_DEXTERITY = 10f;
    public PlayerStats stats;
    public int associatedPID = -1;
    protected HurtBox hurtBox;
    private final float PLAYER_HIT_BOX_PERCENT_SCALAR = 0.01f;
    private final float PLAYER_HURT_BOX_WIDTH_PERCENT_SCALAR = 0.2f;
    private final float PLAYER_HURT_BOX_HEIGHT_PERCENT_SCALAR = 0.4f;
    private float dashTimer;
    private final float DASH_DEXTERITY_CONVERTER = 10f;
    private final float DASH_DISTANCE = 6f;
    private float attackTimer;
    private float abilityTimer;
    private float abilityCooldown = 5f; // idk maybe this changes based on ability, not sure yet.
    private final float SMOKE_BOMB_THROW_DISTANCE = 10f;
    private float consumableTimer;
    private final float CONSUMABLE_COOLDOWN = 0.2f;
    private boolean holdingKey;
    private Sprite keySprite;
    private int healthPotionsHeld;
    private float deathTimer = 0f;
    private int numCoins;

    private Random rnd;
    private Sound pickupKeySFX;
    private Sound hurtSFX;
    private Sound shootSFX;

    public Player(Texture texture, float x, float y, float width, float height, int pid) {
        super(EntityAlignment.PLAYER, texture, x, y, width, height, Layer.PLAYER, AnimationActor.PLAYER1,
                ReplicationType.PLAYER, -1, -1);
        RRGame.globals.currentNumPlayers++;
        this.maxXVelocity = BASE_PLAYER_MOVE_SPEED;
        this.maxYVelocity = BASE_PLAYER_MOVE_SPEED;
        this.attackTimer = 0f;
        this.dashTimer = DASH_DEXTERITY_CONVERTER / BASE_PLAYER_DEXTERITY;
        this.abilityTimer = abilityCooldown;
        this.consumableTimer = CONSUMABLE_COOLDOWN;
        this.holdingKey = false;
        this.keySprite = new Sprite(RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class));
        this.keySprite.setSize(width*2, height*2);
        this.keySprite.setOrigin(width, height);
        this.healthPotionsHeld = 3;
        this.stats = new PlayerStats(BASE_PLAYER_HEALTH, BASE_PLAYER_DAMAGE, BASE_PLAYER_ATTACK_SPEED, BASE_PLAYER_MOVE_SPEED, BASE_PLAYER_DEXTERITY, this);
        hitBox.disableLength = 10000f;
        hurtBox = new HurtBox(hitBox, this);
        setBoxPercentSize(PLAYER_HIT_BOX_PERCENT_SCALAR, PLAYER_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(PLAYER_HURT_BOX_WIDTH_PERCENT_SCALAR, PLAYER_HURT_BOX_HEIGHT_PERCENT_SCALAR, hurtBox);
        this.associatedPID = pid;
        rnd = RRGame.globals.getRandom();
        pickupKeySFX = RRGame.am.get(RRGame.RSC_PICK_UP_KEY_SFX);
        hurtSFX = RRGame.am.get(RRGame.RSC_HURT_SFX);
        shootSFX = RRGame.am.get(RRGame.RSC_SHOOT_SFX);
        this.numCoins = 0;
        // this will obviously change based on a number of factors later
    }

    public Player(Texture texture, float x, float y, float size, int pid) {
        this(texture, x, y, size, size, pid);
    }

    public Player(float x, float y, int size, int pid){
        this(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y,size,size, pid);
    }

    /**
     * Ran every frame.
     * @param delta Time since last frame
     */
    public void update(float delta) {
        attackTimer += delta;
        dashTimer += delta;
        abilityTimer += delta;
        consumableTimer += delta;
        adjustVelocity();
        super.update(delta);
        // we likely want some resurrection sort of ability or even just a ghost camera you can move
        if (deathTimer >= RRGame.STANDARD_DEATH_DURATION) { this.removeSelf(); return; }
        if (stats.isDead()) { this.dropKey(); deathTimer += delta; return; }
        hurtBox.update(delta);
        keySprite.setX(getX()-getWidth()/2);
        keySprite.setY(getY()+getHeight()/2);
        if (attackTimer >= (1 / stats.getAttackSpeed())){
            int myPID    = this.associatedPID;
            long projNum = RRGame.globals.getProjectileNumber(myPID);

            attack(myPID,projNum);
            attackTimer = 0f;
        }
    }

    public void moveLeft(){
        xVelocity -= ACCELERATION;
        this.flipped = true;
    }

    public void moveRight(){
        xVelocity += ACCELERATION;
        this.flipped = false;
    }

    public void moveUp(){
        yVelocity += ACCELERATION;
    }

    public void moveDown(){
        yVelocity -= ACCELERATION;
    }

    /**
     *
     * @return percentage of ability time left
     */
    public float getAbilityTimeLeft() {
        return Math.min(abilityTimer / abilityCooldown, 1);
    }

    /**
     * Attack, tie any projectiles to a frame/pid
     * @param pid
     * @param frame
     * @return
     */
    public boolean attack(int pid, long frame) {
        // good spot for a sound effect
        //this converts a Vector3 position of pixels to a Vector3 position of units
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        Vector3 mouseLocation = RRGame.playerCam.unproject(new Vector3(x, y, 0));
        float xCenter = this.getX() + this.getWidth()/2;
        float yCenter = this.getY() + this.getHeight()/2;
        Vector3 throwingKnifeDir = new Vector3(mouseLocation.x-xCenter, mouseLocation.y-yCenter, 0);
        new ThrowingKnife(getX(), getY(), throwingKnifeDir.x, throwingKnifeDir.y, stats.getDamage(),
                RRGame.STANDARD_PROJECTILE_SPEED, pid, frame);
        shootSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        this.setCurrentAnimation(AnimationAction.ATTACK);
        return true;
    }

    public void dash() {
        if (dashTimer < (DASH_DEXTERITY_CONVERTER / stats.getDexterity())) { return; }
        // good spot for a sound effect
        dashTimer = 0f;
        float x = getX();
        float y = getY();
        float xOffset = 0f;
        float yOffset = 0f;
        if  (xVelocity < 0) {
            xOffset -= DASH_DISTANCE;
        }
        if (xVelocity > 0) {
            xOffset += DASH_DISTANCE;
        }
        if (yVelocity < 0) {
            yOffset -= DASH_DISTANCE;
        }
        if (yVelocity > 0) {
            yOffset += DASH_DISTANCE;
        }
        // this is to ensure dash is constant even when going diagonal
        if (xOffset != 0 && yOffset != 0) {
            xOffset = (float) (xOffset / Math.sqrt(2));
            yOffset = (float) (yOffset / Math.sqrt(2));
        }
        setX(x+xOffset);
        setY(y+yOffset);
    }

    public void useAbility(int pid, long frame) {
        if (abilityTimer < abilityCooldown) { return; }
        // good spot for a sound effect
        abilityTimer = 0f;
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        Vector3 mouseLocation = RRGame.playerCam.unproject(new Vector3(x, y, 0));
        float xCenter = this.getX() + this.getWidth()/2;
        float yCenter = this.getY() + this.getHeight()/2;
        Vector3 bombDir = new Vector3(mouseLocation.x-xCenter, mouseLocation.y-yCenter, 0);
        new SmokeBomb(getX(), getY(), bombDir.x, bombDir.y, SMOKE_BOMB_THROW_DISTANCE, RRGame.STANDARD_PROJECTILE_SPEED);
    }

    public void setHoldingKey(boolean holdingKey){
        this.holdingKey = holdingKey;
    }

    public void dropKey(){
        if (holdingKey){
            setHoldingKey(false);
            new Key(getX(),getY());
        }
    }

    public void grabKey(){
        //Players on server pick up keys directly, and tell clients that a player picked up a key.
        if (RRGame.globals.pid == 0){
            setHoldingKey(true);
            RRGame.globals.network.connection.dispatchKeyPickup(this.associatedPID);
            pickupKeySFX.play(0.2f);
        }
    }

    public boolean isHoldingKey(){
        return holdingKey;
    }

    public void grabCoin() {
        //Players on server pick up coins directly, and tell clients that a player picked up a coin.
        // slight desync is fine bc coins should be common enough its ok if two players pick up the same one
        if (RRGame.globals.pid == 0){
            numCoins++;
            //RRGame.globals.network.connection.dispatchCoinPickup(this.associatedPID);
            pickupKeySFX.play(0.1f);
        }
        System.out.println("Current coins " + numCoins);
    }

    public void spendCoins(int amount) {
        // idk how this will work for merchant but i'm adding the function here
        numCoins--;
    }

    public int getNumCoins() {
        return numCoins;
    }

    public void useConsumable(int pid, long frame) {
        // this is currently only healthPotions; this could be changed to consumablesHeld and diff consumables
        // but that is currently out of scope
        if (consumableTimer < CONSUMABLE_COOLDOWN || healthPotionsHeld <= 0) { return; }
        // good spot for a sound effect
        consumableTimer = 0f;
        healthPotionsHeld--;
        stats.heal(RRGame.HEALTH_POTION_HEAL_AMOUNT);
        System.out.println("New Health: " + stats.getHealth());
    }

    /**
     * Since we can do hitBox on background items, then there could be a way that this function could check
     * what entity it's intersecting with (a chest) and then do stuff like unlocking, idk. or maybe its automatic
     */
    public void interact() {
        System.out.println("interact?");
    }

    public void pickUpConsumable() {
        healthPotionsHeld++;
    }

    public void adjustVelocity() {
        // normalize diagonal movement
        Vector2 direction = new Vector2(xVelocity, yVelocity).nor();
        float adjustedMaxXVelocity = abs(direction.x) * maxXVelocity;
        float adjustedMaxYVelocity = abs(direction.y) * maxYVelocity;

        //apply horizontal friction
        if (xVelocity != 0){
            float xDir = Math.signum(xVelocity);
            xVelocity -= (xDir * FRICTION);
            if (Math.signum(xVelocity) != xDir){
                xVelocity = 0;
            }
        }

        //apply vertical friction
        if (yVelocity != 0){
            float yDir = Math.signum(yVelocity);
            yVelocity -= (yDir * FRICTION);
            if (Math.signum(yVelocity) != yDir){
                yVelocity = 0;
            }
        }

        xVelocity = Math.max(-adjustedMaxXVelocity, Math.min(xVelocity, adjustedMaxXVelocity));
        yVelocity = Math.max(-adjustedMaxYVelocity, Math.min(yVelocity, adjustedMaxYVelocity));
    }

    @Override
    public void onHit(Entity thingHit) {
        // player hitting a hurtbox shouldn't necessarily do anything. Maybe if we make it so walls have 'hurtboxes'
        // then that would happen but idk, for now the player has a hitbox because its an entity but it has a massive
        // disable time so this method should never really fire off anyway.
        return;
    }

    @Override
    public void onHurt(Entity thingThatHurtMe) {
        boolean tookDamage = false;
        if (thingThatHurtMe.alignment == EntityAlignment.PLAYER) { return; }
        else if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.ENEMY) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
            tookDamage = true;
            hurtSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        }
        else if (thingThatHurtMe instanceof Enemy) {
            this.stats.takeDamage(((Enemy) thingThatHurtMe).stats.getDamage());
            tookDamage = true;
            hurtSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        }
        else if (thingThatHurtMe instanceof Key) {
            this.grabKey();
        }
        else if (thingThatHurtMe instanceof Coin) {
            this.grabCoin();
        }
        else if (thingThatHurtMe instanceof Door || thingThatHurtMe instanceof Chest) {
            // just catch the things we know of that don't do anything
        }
        else {
            System.out.println("This shouldn't ever happen...");
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

    public void resetForNewRoom() {
        this.setPosition(RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y);
        this.holdingKey = false;
        this.attackTimer = 0f;
        this.dashTimer = 0f;
        this.abilityTimer = 0f;
        this.consumableTimer = 0f;
    }

    @Override
    public void draw(Batch batch) {
        if (holdingKey) {
            keySprite.draw(batch);
        }
        super.draw(batch);
    }
}
