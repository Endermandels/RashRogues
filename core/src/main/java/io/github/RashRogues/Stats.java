package io.github.RashRogues;

public class Stats {

    private int maxHealth;
    private int health;
    private int damage;
    private float attackSpeed;
    private float moveSpeed;
    private Entity parent;
    private boolean dead;

    Stats(int health, int damage, float attackSpeed, float moveSpeed, Entity parent) {
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.parent = parent;
        this.dead = false;
    }

    public int getHealth() { return health; }
    public void increaseHealth(int amount) { health += amount; maxHealth += amount; }
    public int getDamage() { return damage; }
    public void increaseDamage(int amount) { damage += amount; }
    public float getAttackSpeed() { return attackSpeed; }
    public void increaseAttackSpeed(float amount) { attackSpeed+=amount; }
    public float getMoveSpeed() { return moveSpeed; }
    public void increaseMoveSpeed(float amount) { moveSpeed+=amount; parent.setMaxMoveSpeeds(moveSpeed, moveSpeed); }
    public int getMaxHealth() { return maxHealth; }

    public void takeDamage(int damage) {
        this.health -= damage;

        // Only The Server Has The Authority to kill.
        // Server communicate these deaths to clients.
        if (this.health <= 0 && RRGame.globals.pid == 0) {
            this.dead = true;

            //Dispatch Kill Player Command To Clients
            if (this.parent instanceof Player){
               RRGame.globals.network.connection.dispatchKillPlayer(((Player) this.parent).associatedPID);
            }

            //todo: communicate other entities deaths

        }
    }

    public void heal(int amount) {
        this.health = Math.min(maxHealth, this.health + amount);
    }

    public boolean isDead() { return this.dead; }

    public void kill(){
        this.health = 0;
        this.dead   = true;
    }

}
