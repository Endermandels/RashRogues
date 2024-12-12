package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.graphics.Texture;

public class Key extends Entity {

    public Key(float x, float y) {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class),
                x, y, RRGame.KEY_SIZE, RRGame.KEY_SIZE, Layer.BACKGROUND, null,
                ReplicationType.KEY, -1, -1);
        this.setBoxPercentSize(1f, 1f, hitBox);
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
