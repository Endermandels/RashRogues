package io.github.RashRogues;

public enum EntityType {
    PLAYER(0),
    SWORDSMAN(1),
    PROJECTILE(2),
    ARCHER(3),
    BOMBER(4),
    UI(5),
    BACKGROUND(6),
    BOMB(7);

    private final int value;
    EntityType(int value){this.value=value;}
    public int getvalue(){return this.value;}

}
