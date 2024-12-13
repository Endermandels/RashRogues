package io.github.RashRogues;

public class Stats {

    private int maxHealth;
    private int health;
    private int damage;
    private float attackSpeed;
    private float moveSpeed;
    private float retreatSpeed;
    private Entity parent;
    private boolean dead;

    Stats(int health, int damage, float attackSpeed, float moveSpeed, float retreatSpeed, Entity parent) {
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.retreatSpeed = retreatSpeed;
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
    public float getRetreatSpeed() { return retreatSpeed; }
    public void increaseMoveSpeed(float amount) { moveSpeed+=amount; parent.setMaxMoveSpeeds(moveSpeed, moveSpeed); }
    public void increaseRetreatSpeed(float amount) { retreatSpeed+=amount; }
    public int getMaxHealth() { return maxHealth; }
    public void setHealth(int health){this.health = health;}

    public void takeDamage(int damage) {

        this.health -= damage;


        // Force the client to reckon with the health he has on the server.
        if (this.parent instanceof Player){
            int pid = ((Player) this.parent).associatedPID;
            RRGame.globals.network.connection.dispatchSyncHealth(pid, this.health);
        }


        // Only The Server Has The Authority to kill.
        // Server communicate these deaths to clients.
        if (this.health <= 0 && RRGame.globals.pid == 0) {
            this.dead = true;

            //Dispatch Kill Player Command To Clients
            if (this.parent instanceof Player){
               RRGame.globals.network.connection.dispatchKillPlayer(((Player) this.parent).associatedPID);
            }

            if (this.parent instanceof Enemy){
                RRGame.globals.network.connection.dispatchKillEnemy(( (Enemy) this.parent).id);
            }

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
