package io.github.RashRogues;

public class Stats {

    protected int health;
    protected int damage;
    protected float attackSpeed;
    protected float moveSpeed;
    protected Entity parent;
    protected boolean dead;

    Stats(int health, int damage, float attackSpeed, float moveSpeed, Entity parent) {
        this.health = health;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.parent = parent;
        this.dead = false;
    }

    public int getHealth() { return health; }
    public void increaseHealth(int amount) { health += amount; }
    public int getDamage() { return damage; }
    public void increaseDamage(int amount) { damage += amount; }
    public float getAttackSpeed() { return attackSpeed; }
    public void increaseAttackSpeed(float amount) { attackSpeed+=amount; }
    public float getMoveSpeed() { return moveSpeed; }
    public void increaseMoveSpeed(float amount) { moveSpeed+=amount; }

    public void takeDamage(int damage) {
        this.health -= damage;
        System.out.println("Yeouch! Just took " + damage + " damage!");
        if (this.health <= 0) {
            this.dead = true;
            System.out.println("I'm dead...");
        }
    }

    public boolean isDead() { return this.dead; }

}
