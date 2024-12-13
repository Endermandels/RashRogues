package io.github.RashRogues;

public class PlayerStats extends Stats {

    private float dexterity;

    PlayerStats(int health, int damage, float attackSpeed, float moveSpeed, float dexterity, Entity parent) {
        super(health, damage, attackSpeed, moveSpeed, 0f, parent);
        this.dexterity = dexterity;
    }

    public float getDexterity() { return dexterity; }
    public void increaseDexterity(float amount) { dexterity += amount; }
}
