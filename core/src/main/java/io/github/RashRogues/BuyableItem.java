package io.github.RashRogues;

public enum BuyableItem {
    DAGGER(1),
    THROWING_KNIFE(2),
    HEALTH_POTION(3),
    RING(4),
    CLOAK(5);
    private final int value;
    BuyableItem(int value) { this.value = value;}
    public int getvalue(){return value;}
}
