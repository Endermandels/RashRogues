package io.github.RashRogues;

public class SwordsmanSwing extends Projectile {

    private final float SWORDSMAN_ATTACK_HIT_BOX_PERCENT_SCLAR = 0.5f;

    SwordsmanSwing(float x, float y, int damage) {
        super(EntityAlignment.ENEMY, RRGame.am.get(RRGame.RSC_SWORDSMAN_SWING_IMG), x, y,
                RRGame.SWORDSMAN_SWING_SIZE, RRGame.SWORDSMAN_SWING_SIZE, damage, 0f, false,
                RRGame.STANDARD_MELEE_DURATION, AnimationActor.SWORDSMAN_SWING);
        this.setBoxPercentSize(SWORDSMAN_ATTACK_HIT_BOX_PERCENT_SCLAR, SWORDSMAN_ATTACK_HIT_BOX_PERCENT_SCLAR, hitBox);
    }
}