package io.github.RashRogues;

public enum Layer {
    BACKGROUND(0),
    PLAYER(1),
    ENEMY(2),
    PROJECTILE(3),
    FOREGROUND(4);

    Layer(int value){
        this.value = value;
    }

    private final int value;

    public int getValue(){
        return value;
    }
}
