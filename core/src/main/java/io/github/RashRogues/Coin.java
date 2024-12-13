package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.graphics.Texture;

public class Coin extends Entity {

    public int value;

    Coin(float x, float y, int value) {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_COIN_IMG, Texture.class),
                x, y, RRGame.KEY_SIZE, RRGame.KEY_SIZE, Layer.COIN, null,
                ReplicationType.CLIENTSIDE, -1, -1);
        this.setBoxPercentSize(0.4f, 0.4f, hitBox);
        hitBox.disableLength = 0f;
        this.value = value;
    }

    @Override
    public void onHit(Entity thingHit) {
        // if we hit anything other than a player, we don't care
        if (!(thingHit instanceof Player)) { return; }
        // otherwise kill ourselves, the player will grab us
        if (((Player) thingHit).associatedPID == RRGame.globals.pid){
            this.removeSelf();
        }
    }
}
