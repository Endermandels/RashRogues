package io.github.RashRogues;

/**
 * Effects, in nearly all cases, should be handled by the Hit/Hurt boxes by simply calling
 * hitBox.setEffect(Effect.x). When that hitBox hits a hurtBox, the hurtBox will tell the entity
 * to add that effect. Effects do not currently stack; instead, it will reset the duration if they
 * get hit multiple times by the same effect. For example, a smoke bomb will apply smoke to all enemies
 * it hits, and if an enemy is still in it after a second (default disableLength), the timer will reset
 * back from 4s to 5s. Entity takes care of all the timers and removing once the effect has been added.
 */
public enum Effect {
    NONE(0f),
    SMOKE(5f);
    // burn, whatever other things we want

    Effect(float duration){
        this.duration = duration;
    }

    private float duration;

    /**
     * If you want to change the default duration of the effect.
     * @param duration duration of the effect once applied.
     */
    public void setDuration(float duration) { this.duration = duration; }

    public float getDuration(){
        return duration;
    }
}
