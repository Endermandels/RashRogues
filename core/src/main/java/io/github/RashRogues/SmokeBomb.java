package io.github.RashRogues;

public class SmokeBomb extends Bomb {

    /**
     * Create a smokebomb.
     * This smokebomb will be synced.
     * @param x
     * @param y
     * @param xDirection
     * @param yDirection
     * @param distance
     * @param speed
     */
    SmokeBomb(float x, float y, float xDirection, float yDirection, float distance, float speed) {
        super(EntityAlignment.PLAYER, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG), x, y, RRGame.SMOKE_BOMB_SIZE,
                RRGame.SMOKE_BOMB_SIZE, xDirection, yDirection, distance, 2f,
                new SmokeBombExplosion(x,y), speed);
        this.setBoxPercentSize(0.5f, 0.5f, hitBox);
        this.hitBox.setEffect(Effect.SMOKE);
    }
}
