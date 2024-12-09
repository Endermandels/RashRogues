package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.HashSet;

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
    public boolean clientSideOnly = false;

    // Used For Networking
    public int id     = -1;
    public int pid    = -1;
    public long frame = -1;

    protected Entity(EntityAlignment alignment, Texture texture, float x, float y,
                     float width, float height, Layer layer, boolean replicated, int pid, long frame, boolean clientside) {
        super(texture);
        this.clientSideOnly = clientside;
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

        this.pid = pid;
        this.frame = frame;

        //add our entity to the current screen.
        RRGame.globals.registerEntity(this, replicated, pid, frame);
    }

    /**
     * Create an Entity on the current screen.
     * Entities will be updated and drawn every frame.
     *
     */
    protected Entity(EntityAlignment alignment, Texture texture, float x, float y,
                     float width, float height, Layer layer) {
        this(alignment, texture, x, y, width, height, layer, false,-1,-1, true);
    }
   /**
    * Create an Entity on the current screen.
    * Entities will be updated and drawn every frame.
    *
    * If the 'replicated' parameter is true, this Entity will
    * be associated with its equivalent entity on all endpoints. Consequently, major game events
    * such as deaths will be reflected across these intertwined entities.
    * The replicated option MUST ONLY BE USED FOR DETERMINISTIC ENTITIES.
    * See Network documentation is more info is desired.
    * When in doubt, set replicated to false.
    */
   protected Entity(EntityAlignment alignment, Texture texture, float x, float y,
                    float width, float height, Layer layer, boolean replicated) {
        this(alignment, texture, x, y, width, height, layer, replicated,-1,-1, false);
   }

    /**
     * Ran Every Frame.
     * Calls update on children classes.
     * @param delta
     */
    public void update(float delta) {

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

        hitBox.update(delta);
    }

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
        if (RRGame.globals.pid == 0 || this.clientSideOnly) {
            RRGame.globals.deregisterEntity(this);
        }
    }

}
