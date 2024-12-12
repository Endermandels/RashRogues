package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

public abstract class Entity extends Sprite {

    protected float maxXVelocity;
    protected float maxYVelocity;
    protected float xVelocity;
    protected float yVelocity;
    protected HitBox hitBox;
    protected float hitBoxWidthScalar;
    protected float hitBoxHeightScalar;
    protected float hurtBoxWidthScalar;
    protected float hurtBoxHeightScalar;
    protected boolean flipped;
    protected EntityAlignment alignment;
    protected Layer layer;
    private HashMap<Effect, Float> activeEffects;
    public ReplicationType replicationType;

    private BitmapFont font = new BitmapFont();

    // Used for Animation
    private static AnimationHandler ah = new AnimationHandler();
    private Map<AnimationAction, AnimationInfo> animations;
    private AnimationInfo currentAnimationInfo;
    private AnimationAction currentAnimationAction;
    private float animationTimer;
    private boolean animationsActive;

    // Used For Networking
    public int id     = -1;
    public int pid    = -1;
    public long frame = -1;

    /**
     * Create an Entity on the current screen.
     * Entities will be updated and drawn every frame.
     * @param alignment
     * @param texture
     * @param x
     * @param y
     * @param width
     * @param height
     * @param layer
     * @param replicationType
     * @param creatorPID
     * @param number
     */
    public Entity(EntityAlignment alignment, Texture texture, float x, float y,
                     float width, float height, Layer layer, AnimationActor animationActor,
                     ReplicationType replicationType, int creatorPID, long number) {
        super(texture);
        setSize(width, height);
        setOrigin(width / 2, height / 2);
        setPosition(x, y);
        hitBox = new HitBox(this.getBoundingRectangle(), this);
        this.maxXVelocity = 15.0f;
        this.maxYVelocity = 15.0f;
        this.hitBoxWidthScalar = 1.0f;
        this.hitBoxHeightScalar = 1.0f;
        this.hurtBoxWidthScalar = 1.0f;
        this.hurtBoxHeightScalar = 1.0f;
        this.flipped = false;
        this.layer = layer;
        this.alignment = alignment;
        this.activeEffects = new HashMap<Effect, Float>();
        this.replicationType = replicationType;
        RRGame.globals.registerEntity(this, replicationType, creatorPID, number);
        this.toggleAnimations(true);
        if (animationActor == null) {
            // this is redundant but necessary unless we want a whole extra layer of nested if loops!
            this.animations = null;
        }
        else if (this instanceof Player) {
            switch (RRGame.globals.pid) {
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
                    this.setUpAnimations(animationActor);
                    break;
            }
        }
        else {
            this.setUpAnimations(animationActor);
        }
    }

    /**
     * Ran Every Frame.
     * Calls update on children classes.
     * @param delta
     */
    public void update(float delta) {

        // change animation state if necessary
        // if an animation wasn't set up and animations aren't active, there will just be the default static sprite image
        if (animations != null && animationsActive) {
            animationTimer += delta;
            if (xVelocity != 0 || yVelocity != 0) {
                setCurrentAnimation(AnimationAction.MOVE);
            }
            if (currentAnimationInfo.isAnimationFinished(animationTimer)) {
                setCurrentAnimation(AnimationAction.DEFAULT);
            }
            setRegion(currentAnimationInfo.getCurrentFrame(animationTimer));
        }
        else {
            setRegion(getTexture());
        }


        // update any timers for effects; once the time has elapsed, remove that effect
        for (Effect effect : activeEffects.keySet()) {
            Float effectTimer = activeEffects.get(effect);
            activeEffects.put(effect, (effectTimer-delta));
            if (effectTimer-delta <= 0) { this.removeEffect(effect); }
        }

        float x = getX();
        float y = getY();
        if (flipped) {
            this.setScale(-Math.abs(getScaleX()), getScaleY());
        }
        else {
            this.setScale(Math.abs(getScaleX()), getScaleY());
        }

        setX(x + delta * xVelocity);
        setY(y + delta * yVelocity);

        // apply room limits on all entities. camera is also bound this way so hud and other things won't be ruined by this.
        x = getX();
        y = getY();
        if (x < 0) { setX(0); }
        else if (x+getWidth() > RRGame.playerCam.roomWidth) { setX(RRGame.playerCam.roomWidth-getWidth()); }
        if (y < 0) { setY(0); }
        else if (y+getHeight() > RRGame.playerCam.roomHeight) { setY(RRGame.playerCam.roomHeight-getHeight()); }

        hitBox.update(delta);
    }

    private void setUpAnimations(AnimationActor animationActor) {
        this.animationTimer = 0f;
        if (Entity.ah.animations.get(animationActor) == null) return;
        this.animations = Entity.ah.animations.get(animationActor);
        this.currentAnimationInfo = this.animations.get(AnimationAction.DEFAULT);
        this.currentAnimationAction = AnimationAction.DEFAULT;
    }

    protected boolean setCurrentAnimation(AnimationAction action) {
        // if somehow we've asked it to do something that doesn't exist, then don't crash
        // additionally, death animations take priority over everything
        if (this.animations == null || !this.animations.containsKey(action) || this.currentAnimationAction == AnimationAction.DIE) {
            return false;
        }
        // move only has priority over the idle animation, no others
        if (action == AnimationAction.MOVE && this.currentAnimationAction != AnimationAction.DEFAULT) {
            return false;
        }
        this.currentAnimationInfo = this.animations.get(action);
        // if it's the same action, then skip resetting it, UNLESS it's the hurt animation
        if (this.currentAnimationAction != action || this.currentAnimationAction == AnimationAction.HURT) {
            this.currentAnimationAction = action;
            this.animationTimer = 0f;
            return true;
        }
        return false;
    }

    protected void toggleAnimations(boolean onOrOff) {
        this.animationsActive = onOrOff;
    }

    protected boolean isAnimationFinished() { return this.currentAnimationInfo.isAnimationFinished(animationTimer); }

    /**
     * Hit or Hurt Box has its width and height multiplied by WidthScalar and HeightScalar respectively.
     * The values last used are saved as xBoxWidthScalar and xBoxHeightScalar where x is the type of box adjusted.
     * @param widthScalar
     * @param heightScalar
     * @param box
     */
    protected void setBoxPercentSize(float widthScalar, float heightScalar, Rectangle box) {
        if (box instanceof HitBox) {
            this.hitBoxWidthScalar = widthScalar;
            this.hitBoxHeightScalar = heightScalar;
            box.setSize(hitBoxWidthScalar * this.getWidth(), hitBoxHeightScalar * this.getHeight());
        }
        else if (box instanceof HurtBox) {
            this.hurtBoxWidthScalar = widthScalar;
            this.hurtBoxHeightScalar = heightScalar;
            box.setSize(hurtBoxWidthScalar * this.getWidth(), hurtBoxHeightScalar * this.getHeight());
        }
    }

    // meant to be overridden
    public void onHit(Entity thingHit) {
        System.out.println("No defined behavior; add this function to the Entity");
    }

    // meant to be overridden by entities with hurtBox
    public void onHurt(Entity thingThatHurtMe) {
        System.out.println("No defined behavior; add this function to the Entity");
    }

    /**
     * Add an effect to this entity-- generally only called by this entity's HurtBox.
     * @param effect
     */
    public void addEffect(Effect effect) {
        activeEffects.put(effect, effect.getDuration());
    }

    /**
     * Remove an effect from this entity-- generally only called when the effect timer hits 0.
     * @param effect
     */
    public void removeEffect(Effect effect) {
        activeEffects.remove(effect);
    }

    /**
     * Check if this entity has a particular effect active. Used anywhere this information is useful.
     * @param effect
     * @return
     */
    public boolean hasEffect(Effect effect) {
        return activeEffects.containsKey(effect);
    }

    public void setMaxMoveSpeeds(float xMaxMoveSpeed, float yMaxMoveSpeed) {
        this.maxXVelocity = xMaxMoveSpeed;
        this.maxYVelocity = yMaxMoveSpeed;
    }

    protected void removeSelf() {
        // Only the host has the authority to remove an Entity directly.
        // Clients will be instructed to do so via the network.
        // Entities marked with clientSideOnly may be removed by anyone.
        if (RRGame.globals.pid == 0 || replicationType == ReplicationType.CLIENTSIDE) {
            RRGame.globals.deregisterEntity(this);
        }
    }
}
