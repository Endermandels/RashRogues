package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.graphics.Texture;

public class Coin extends Entity {

    Coin(float x, float y) {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_COIN_IMG, Texture.class),
                x, y, RRGame.KEY_SIZE, RRGame.KEY_SIZE, Layer.COIN, null,
                ReplicationType.ENTITY_NUMBER, -1, -1);
        this.setBoxPercentSize(0.25f, 0.25f, hitBox);
        hitBox.disableLength = 0f;
    }

    @Override
    public void onHit(Entity thingHit) {
        // if we hit anything other than a player, we don't care
        if (!(thingHit instanceof Player)) { return; }
        // otherwise kill ourselves, the player will grab us
        this.removeSelf();
    }
}
