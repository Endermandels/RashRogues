package io.github.RashRogues;

public class BomberBomb extends Bomb {
    /**
     * Create a bomber bomb.
     * This bomber bomb will be synced.
     * @param x
     * @param y
     * @param xDirection
     * @param yDirection
     * @param distance
     * @param speed
     */
    BomberBomb(float x, float y, float xDirection, float yDirection, float distance, float speed, int damage) {
        super(EntityAlignment.ENEMY, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG), x, y, RRGame.BOMBER_BOMB_SIZE,
                RRGame.BOMBER_BOMB_SIZE, xDirection, yDirection, distance, RRGame.BOMBER_BOMB_FUSE_DURATION,
                new BomberExplosion(x, y, damage), speed, AnimationActor.BOMBER_BOMB);
        this.setBoxPercentSize(0.5f, 0.5f, hitBox);
    }
}
