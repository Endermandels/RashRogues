package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Entity {

    private final int BASE_PLAYER_HEALTH = 100;
    private final int BASE_PLAYER_DAMAGE = 10;
    private final float BASE_PLAYER_ATTACK_SPEED = 0.5f;
    private final float ACCELERATION = 50.0f;
    private final float FRICTION = 100.0f;
    private final float BASE_PLAYER_MOVE_SPEED = 15.0f;
    private final float BASE_PLAYER_DEXTERITY = 10f;
    public PlayerStats stats;
    protected HurtBox hurtBox;
    private boolean leftMove;
    private boolean rightMove;
    private boolean downMove;
    private boolean upMove;
    private float dashTimer;
    private final float DASH_COOLDOWN = 1f;
    private float attackTimer;
    private float abilityTimer;
    private float abilityCooldown = 5f; // idk maybe this changes based on ability, not sure yet.
    private float consumableTimer;
    private final float CONSUMABLE_COOLDOWN = 0.2f;
    private boolean holdingKey;
    private Sprite keySprite;

    public Player(Texture texture, float x, float y, float width, float height) {
        super(EntityType.PLAYER, EntityAlignment.PLAYER, texture, x, y, width, height, Layer.PLAYER);
        RRGame.globals.currentNumPlayers++;
        this.maxXVelocity = BASE_PLAYER_MOVE_SPEED;
        this.maxYVelocity = BASE_PLAYER_MOVE_SPEED;
        this.attackTimer = 0f;
        this.dashTimer = 0f;
        this.abilityTimer = 0f;
        this.consumableTimer = 0f;
        this.holdingKey = false;
        this.keySprite = new Sprite(RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class));
        this.keySprite.setSize(width*2, height*2);
        this.keySprite.setOrigin(width, height);
        this.stats = new PlayerStats(BASE_PLAYER_HEALTH, BASE_PLAYER_DAMAGE, BASE_PLAYER_ATTACK_SPEED, BASE_PLAYER_MOVE_SPEED, BASE_PLAYER_DEXTERITY, this);
        hitBox.disableLength = 10000f;
        hurtBox = new HurtBox(hitBox, this);
        setBoxPercentSize(0.01f, 0.01f, hitBox);
        setBoxPercentSize(0.2f, 0.4f, hurtBox);
        // this will obviously change based on a number of factors later
    }

    Player(Texture texture, float x, float y, float size) {
        this(texture, x, y, size, size);
    }

    /**
     * Ran every frame.
     * @param delta
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

    public void moveLeft(boolean t) { leftMove = t; }
    public void moveRight(boolean t) { rightMove = t; }
    public void moveDown(boolean t) { downMove = t; }
    public void moveUp(boolean t) { upMove = t; }

    public void attack() {
        System.out.println("Attack");
    }

    public void dash() {
        if (dashTimer < DASH_COOLDOWN) { return; }
        dashTimer = 0f;
        System.out.println("Dash");
    }

    public void useAbility() {
        if (abilityTimer < abilityCooldown) { return; }
        abilityTimer = 0f;
        System.out.println("Use Ability");
    }

    public void useConsumable() {
        if (consumableTimer < CONSUMABLE_COOLDOWN) { return; }
        consumableTimer = 0f;
        System.out.println("Use Consumable");
    }

    public void adjustVelocity() {
        float xVel = 0;
        float yVel = 0;
        if  (leftMove) {
            xVel -= ACCELERATION;
            this.flipped = true;
        }
        if (rightMove) {
            xVel += ACCELERATION;
            this.flipped = false;
        }
        if (downMove) {
            yVel -= ACCELERATION;
        }
        if (upMove) {
            yVel += ACCELERATION;
        }

        // this is to ensure speed is constant even when going diagonal
        if (xVel != 0 && yVel != 0) {
            xVel = (float) (xVel / Math.sqrt(2));
            yVel = (float) (yVel / Math.sqrt(2));
        }
        if (xVel == 0 && xVelocity != 0) {
            float sign = Math.signum(xVelocity);
            xVelocity -= sign*FRICTION;
            if (sign != Math.signum(xVelocity)) {
                xVelocity = 0;
            }
        }
        if (yVel == 0 && yVelocity != 0) {
            float sign = Math.signum(yVelocity);
            yVelocity -= sign*FRICTION;
            if (sign != Math.signum(yVelocity)) {
                yVelocity = 0;
            }
        }

        xVelocity += xVel;
        yVelocity += yVel;

        xVelocity = Math.max(-maxXVelocity, Math.min(xVelocity, maxXVelocity));
        yVelocity = Math.max(-maxYVelocity, Math.min(yVelocity, maxYVelocity));
    }

    public boolean isHoldingKey() { return holdingKey; }
    public void grabKey() { holdingKey = true; }
    public void dropKey() {
        holdingKey = false;
        new Key(getX(), getY());
    }

    public void onHit(Entity thingHit) {
        // player hitting a hurtbox shouldn't necessarily do anything. Maybe if we make it so walls have 'hurtboxes'
        // then that would happen but idk, for now the player has a hitbox because its an entity but it has a massive
        // disable time so this method should never really fire off anyway.
        return;
    }

    public void onHurt(Entity thingThatHurtMe) {
        if (thingThatHurtMe instanceof Projectile && thingThatHurtMe.alignment == EntityAlignment.ENEMY) {
            this.stats.takeDamage(((Projectile) thingThatHurtMe).damage);
        }
        else if (thingThatHurtMe instanceof Enemy) {
            this.stats.takeDamage(((Enemy) thingThatHurtMe).stats.damage);
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
