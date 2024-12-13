package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.HashSet;
import java.util.Random;

import static java.lang.Math.abs;

public class Player extends Entity {

    private final int BASE_PLAYER_HEALTH = 500;
    private final int BASE_PLAYER_DAMAGE = 10;
    private final float BASE_PLAYER_ATTACK_SPEED = 1f;
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
    private final float MERCHANT_COOLDOWN = 2f;
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
    public int healthPotionsHeld;
    private float deathTimer = 0f;
    private float merchantTimer = MERCHANT_COOLDOWN;
    private int numCoins;
    private HashSet<BuyableItem> purchasedItems;
    public boolean shopping = false;

    private Random rnd;
    private Sound pickupKeySFX;
    private Sound hurtSFX;
    private Sound shootSFX;
    private Sound dashSFX;
    private Sound purchaseSFX;
    private Sound invalidSFX;

    public Vector3 mouseLocation = new Vector3(0f,0f,0f);

    public Player(Texture texture, float x, float y, float width, float height, int pid) {
        super(EntityAlignment.PLAYER, texture, x, y, width, height, Layer.PLAYER, AnimationActor.PLAYER1,
                ReplicationType.PLAYER, -1, -1);
        RRGame.globals.currentNumPlayers++;
        this.associatedPID = pid;
        resetPlayer();
        this.keySprite = new Sprite(RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class));
        this.keySprite.setSize(width*2, height*2);
        this.keySprite.setOrigin(width, height);
        hitBox.disableLength = 10000f;
        hurtBox = new HurtBox(hitBox, this);
        setBoxPercentSize(PLAYER_HIT_BOX_PERCENT_SCALAR, PLAYER_HIT_BOX_PERCENT_SCALAR, hitBox);
        setBoxPercentSize(PLAYER_HURT_BOX_WIDTH_PERCENT_SCALAR, PLAYER_HURT_BOX_HEIGHT_PERCENT_SCALAR, hurtBox);
        rnd = RRGame.globals.getRandom();
        pickupKeySFX = RRGame.am.get(RRGame.RSC_PICK_UP_KEY_SFX);
        hurtSFX = RRGame.am.get(RRGame.RSC_HURT_SFX);
        shootSFX = RRGame.am.get(RRGame.RSC_SHOOT_SFX);
        dashSFX = RRGame.am.get(RRGame.RSC_DASH_SFX);
        switch (this.associatedPID) {
            case 0:
                this.setUpAnimations(AnimationActor.PLAYER1);
                break;
            case 1:
                this.setUpAnimations(AnimationActor.PLAYER2);
                break;
            case 2:
                this.setUpAnimations(AnimationActor.PLAYER3);
                break;
            case 3:
                this.setUpAnimations(AnimationActor.PLAYER4);
                break;
            default:
                this.setUpAnimations(AnimationActor.PLAYER1);
                break;
        }
        purchaseSFX = RRGame.am.get(RRGame.RSC_SHOP_PURCHASE);
        invalidSFX = RRGame.am.get(RRGame.RSC_SHOP_INVALID);
        this.numCoins = 200;
        this.purchasedItems = new HashSet<>();
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
        if (shopping){
            return;
        }

        if (merchantTimer < MERCHANT_COOLDOWN){
            merchantTimer+=delta;
        }

        attackTimer += delta;
        dashTimer += delta;
        abilityTimer += delta;
        consumableTimer += delta;
        adjustVelocity();
        super.update(delta);

        // We have played our death anim, and are ready to reset.
        // If we are on the server, reset and inform client of the reset.

        // 1. move player back to spawn
        if (deathTimer >= RRGame.STANDARD_DEATH_DURATION && RRGame.globals.pid == 0) {
            String[] cmd = {"tp", Integer.toString(this.associatedPID), Integer.toString(RRGame.PLAYER_SPAWN_X), Integer.toString(RRGame.PLAYER_SPAWN_Y)};
            RRGame.globals.executeCommandOnCurrentScreen(cmd);
            RRGame.globals.network.connection.dispatchCommand(cmd);
        }

        //2. reset player's hp/stats etc.
        if (deathTimer >= RRGame.STANDARD_DEATH_DURATION && RRGame.globals.pid == 0) {
            String[] cmd = {"reset", Integer.toString(this.associatedPID)};
            RRGame.globals.executeCommandOnCurrentScreen(cmd);
            RRGame.globals.network.connection.dispatchCommand(cmd);
            return;
        }

        if (stats.isDead() && RRGame.globals.pid == 0) {
            this.dropKey();
            deathTimer += delta;
            return;
        }
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

    public void resetPlayer() {
        this.maxXVelocity = BASE_PLAYER_MOVE_SPEED;
        this.maxYVelocity = BASE_PLAYER_MOVE_SPEED;
        this.attackTimer = 0f;
        this.dashTimer = DASH_DEXTERITY_CONVERTER / BASE_PLAYER_DEXTERITY;
        this.abilityTimer = abilityCooldown;
        this.consumableTimer = CONSUMABLE_COOLDOWN;
        this.holdingKey = false;
        this.healthPotionsHeld = 3;
        this.stats = new PlayerStats(BASE_PLAYER_HEALTH, BASE_PLAYER_DAMAGE, BASE_PLAYER_ATTACK_SPEED, BASE_PLAYER_MOVE_SPEED, BASE_PLAYER_DEXTERITY, this);
        this.numCoins = 0;
        this.deathTimer = 0f;
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
        if (RRGame.globals.currentScreen.getRoom().getRoomType() == RoomType.MERCHANT){
            return false;
        }
        // good spot for a sound effect
        //this converts a Vector3 position of pixels to a Vector3 position of units
        float xCenter = this.getX() + this.getWidth()/2;
        float yCenter = this.getY() + this.getHeight()/2;
        Vector3 throwingKnifeDir = new Vector3(mouseLocation.x-xCenter, mouseLocation.y-yCenter, 0);
        new ThrowingKnife(getX(), getY(), throwingKnifeDir.x, throwingKnifeDir.y, stats.getDamage(),
                RRGame.STANDARD_PROJECTILE_SPEED, pid, frame);
        shootSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        this.setCurrentAnimation(AnimationAction.ATTACK);
        return true;
    }

    /**
     *
     * @return percentage of time left on dash cooldown
     */
    public float getDashTimeLeft() {
        return Math.min(dashTimer / (DASH_DEXTERITY_CONVERTER / stats.getDexterity()), 1);
    }

    public void dash() {
        if (dashTimer < (DASH_DEXTERITY_CONVERTER / stats.getDexterity())) { return; }
        // good spot for a sound effect
        dashSFX.play(0.5f, rnd.nextFloat(0.5f, 1f), 0f);
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
        float xCenter = this.getX() + this.getWidth()/2;
        float yCenter = this.getY() + this.getHeight()/2;
        Vector3 bombDir = new Vector3(mouseLocation.x-xCenter, mouseLocation.y-yCenter, 0);
        new SmokeBomb(getX(), getY(), bombDir.x, bombDir.y, SMOKE_BOMB_THROW_DISTANCE, RRGame.STANDARD_PROJECTILE_SPEED);
    }

    public void setHoldingKey(boolean holdingKey){
        if (holdingKey){
            pickupKeySFX.play(0.2f);
        }
        this.holdingKey = holdingKey;
    }

    public void dropKey(){
        if (holdingKey){
            RRGame.globals.network.connection.dispatchKeyDrop(this.getX(), this.getY());
            setHoldingKey(false);
            new Key(getX(),getY());
        }
    }

    public void grabKey(int keyID){
        if (RRGame.globals.getKey(keyID) == null){
            System.out.println(">>! Warning: key wasn't registered properly!");
            return;
        }

        //Players on server pick up keys directly, and tell clients that a player picked up a key.
        if (RRGame.globals.pid == 0){
            setHoldingKey(true);
            RRGame.globals.network.connection.dispatchKeyPickup(this.associatedPID, keyID);
            pickupKeySFX.play(0.2f);
        }

        // (key will destroy itself upon collision)
    }

    public boolean isHoldingKey(){
        return holdingKey;
    }

    public void grabCoin(Coin coin) {
        if (RRGame.globals.pid == this.associatedPID){
            for (int ii = 0; ii < coin.value; ii++) {
                numCoins++;
                pickupKeySFX.play(0.1f);
            }
        }
    }

    public void buyItem(BuyableItem item, int cost) {

        // We can't afford this item, or we already own it and it's non-disposable.
       if (numCoins < cost || (purchasedItems.contains(item) && RRGame.globals.nonRepurchasableItems.contains(item))){
           this.invalidSFX.play(0.2f);
           return;
       }

       // Buy the item
       this.purchaseSFX.play(0.2f);
       numCoins -= cost;
       this.purchasedItems.add(item);

       // Apply/Give Item To Player
       switch(item){
           case HEALTH_POTION:
               this.healthPotionsHeld+=1;
           break;

           case RING:
               this.stats.increaseHealth(25);
           break;

           case DAGGER:
               this.stats.increaseAttackSpeed(1);
           break;

           case CLOAK:
               this.stats.increaseMoveSpeed(5);
           break;
       }

       // tell others we purchased an upgrade.
       RRGame.globals.network.connection.dispatchUpgrade(this.associatedPID, item);
    }

    public int getCoins(){
        return this.numCoins;
    }

    public int getNumCoins(){
        return this.numCoins;
    }

    public void startShopping(){
        if (RRGame.globals.pid == this.associatedPID){
            GUI gui = RRGame.globals.currentScreen.getGUI();
            gui.openStore();
        }
        this.shopping = true;
    }

    public void stopShopping(){
        if (RRGame.globals.pid == this.associatedPID){
            RRGame.globals.network.connection.dispatchLeaveMerchant(this.associatedPID);
            GUI gui = RRGame.globals.currentScreen.getGUI();
            gui.closeStore();
        }
        this.shopping = false;
    }

    public void useConsumable(int pid, long frame) {
        // this is currently only healthPotions; this could be changed to consumablesHeld and diff consumables
        // but that is currently out of scope
        if (consumableTimer < CONSUMABLE_COOLDOWN || healthPotionsHeld <= 0
                || stats.getHealth() == stats.getMaxHealth()) { return; }
        // good spot for a sound effect
        consumableTimer = 0f;
        healthPotionsHeld--;
        stats.heal(RRGame.HEALTH_POTION_HEAL_AMOUNT);
    }

    public void pickUpConsumable() {
        healthPotionsHeld++;
    }

    public int getHealthPotionCount() { return healthPotionsHeld; }

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
        if (shopping){
            return;
        }

        boolean tookDamage = false;
        if (thingThatHurtMe.alignment == EntityAlignment.PLAYER) { return; }
        else if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.ENEMY) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
            tookDamage = true;
            hurtSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        }
        else if (thingThatHurtMe instanceof Merchant) {
            if (this.merchantTimer >= MERCHANT_COOLDOWN){
                this.startShopping();
                this.merchantTimer = 0;
            }
            return;
        }
        else if (thingThatHurtMe instanceof Enemy) {
            this.stats.takeDamage(((Enemy) thingThatHurtMe).stats.getDamage());
            tookDamage = true;
            hurtSFX.play(0.5f, rnd.nextFloat(0.5f, 2f), 0);
        }
        else if (thingThatHurtMe instanceof Key) {
            this.grabKey(thingThatHurtMe.id);
        }
        else if (thingThatHurtMe instanceof Coin) {
            this.grabCoin((Coin)thingThatHurtMe);
        }
        else if (thingThatHurtMe instanceof Door || thingThatHurtMe instanceof Chest) {
            // just catch the things we know of that don't do anything
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

    public void resetForNewRoom(RoomType roomType) {

        switch (roomType){

            case BATTLE:
                this.setPosition(RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y);
                this.holdingKey = false;
                this.attackTimer = 0f;
                this.dashTimer = DASH_DEXTERITY_CONVERTER / stats.getDexterity();
                this.abilityTimer = abilityCooldown;
                this.consumableTimer = CONSUMABLE_COOLDOWN;

            break;

            case MERCHANT:
                this.setPosition(RRGame.PLAYER_SPAWN_MERCHANT_X, RRGame.PLAYER_SPAWN_MERCHANT_Y);
                this.holdingKey = false;
                this.attackTimer = 0f;
                this.dashTimer = 0f;
                this.abilityTimer = 0f;
                this.consumableTimer = 0f;
            break;

            case KING:
                this.setPosition(RRGame.PLAYER_SPAWN_MERCHANT_X, RRGame.PLAYER_SPAWN_MERCHANT_Y);
                this.holdingKey = false;
                this.attackTimer = 0f;
                this.dashTimer = 0f;
                this.abilityTimer = 0f;
                this.consumableTimer = 0f;
                break;

        }
    }

    @Override
    public void draw(Batch batch) {
        if (holdingKey) {
            keySprite.draw(batch);
        }
        super.draw(batch);
    }


    public HashSet<BuyableItem> getPurchasedItems(){
        return this.purchasedItems;
    }
}
