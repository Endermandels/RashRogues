package io.github.RashRogues;

public enum Layer {
    BACKGROUND(0),
    PLAYER(1),
    ENEMY(2),
    KEY(3),
    COIN(4),
    PROJECTILE(5),
    FOREGROUND(6);

    Layer(int value){
        this.value = value;
    }

    private final int value;

    public int getValue(){
        return value;
    }
}
