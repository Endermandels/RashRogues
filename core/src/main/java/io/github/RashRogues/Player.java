package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Entity {

    private final int BASE_PLAYER_HEALTH = 100;
    private final int BASE_PLAYER_DAMAGE = 10;
    private final float BASE_PLAYER_ATTACK_SPEED = 0.5f;
    private final float ACCELERATION = 50.0f;
    private final float FRICTION = 25.0f;
    private final float BASE_PLAYER_MOVE_SPEED = 15.0f;
    private final float BASE_PLAYER_DEXTERITY = 10f;
    public PlayerStats stats;
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

    public Player(Texture texture, int x, int y, float width, float height) {
        super(EntityType.PLAYER, EntityAlignment.PLAYER, texture, x, y, width, height, Layer.PLAYER);
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
        // this will obviously change based on a number of factors later
    }

    public Player(Texture texture, int x, int y, float size) {
        this(texture, x, y, size, size);
    }

    public Player(int x, int y, int size){
        this(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y,size,size);
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
        // we likely want some resurrection sort of ability or even just a ghost camera you can move
        if (stats.isDead()) { this.dropKey(); this.removeSelf(); return; }
        adjustVelocity();
        super.update(delta);
        hurtBox.update(delta);
        keySprite.setX(getX()-getWidth()/2);
        keySprite.setY(getY()+getHeight()/2);
        if (attackTimer >= (1 / stats.getAttackSpeed())) { attack(); attackTimer = 0f; }
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

    public void attack() {
        // good spot for a sound effect
        float throwingKnifeXDir = Math.signum(xVelocity);
        float throwingKnifeYDir = Math.signum(yVelocity);
        if (throwingKnifeXDir == 0 && throwingKnifeYDir == 0) {
            if (flipped) throwingKnifeXDir = -1;
            else throwingKnifeXDir = 1;
        }
        new ThrowingKnife(getX(), getY(), throwingKnifeXDir, throwingKnifeYDir, stats.getDamage(),
                RRGame.STANDARD_PROJECTILE_SPEED);
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

    public void useAbility() {
        if (abilityTimer < abilityCooldown) { return; }
        // good spot for a sound effect
        abilityTimer = 0f;
        float bombXDir = Math.signum(xVelocity);
        float bombYDir = Math.signum(yVelocity);
        if (bombXDir == 0 && bombYDir == 0) {
            if (flipped) bombXDir = -1;
            else bombXDir = 1;
        }
        new SmokeBomb(getX(), getY(), bombXDir, bombYDir, SMOKE_BOMB_THROW_DISTANCE, RRGame.STANDARD_PROJECTILE_SPEED);
    }

    public void dropKey(){
        holdingKey = false;
        new Key(getX(),getY());
    }

    public void grabKey(){
        holdingKey = true;
    }

    public boolean isHoldingKey(){
        return holdingKey;
    }

    public void useConsumable() {
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
        if (xVelocity != 0 && yVelocity != 0) {
            xVelocity = (float) (xVelocity / Math.sqrt(2));
            yVelocity = (float) (yVelocity / Math.sqrt(2));
        }

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

        xVelocity = Math.max(-maxXVelocity, Math.min(xVelocity, maxXVelocity));
        yVelocity = Math.max(-maxYVelocity, Math.min(yVelocity, maxYVelocity));
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
        if (thingThatHurtMe.alignment == EntityAlignment.PLAYER) { return; }
        else if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.ENEMY) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
        }
        else if (thingThatHurtMe instanceof Enemy) {
            this.stats.takeDamage(((Enemy) thingThatHurtMe).stats.getDamage());
        }
        else if (thingThatHurtMe instanceof Key) {
            this.grabKey();
        }
        else if (thingThatHurtMe instanceof Door) {
            // actually don't need this
        }
        else {
            System.out.println("This shouldn't ever happen...");
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
