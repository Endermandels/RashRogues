package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Key extends Entity {

    Key(float x, float y) {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_KEY_IMG, Texture.class),
                x, y, RRGame.KEY_SIZE, RRGame.KEY_SIZE, Layer.BACKGROUND);
        this.setBoxPercentSize(1f, 1f, hitBox);
    }

    @Override
    public void onHit(Entity thingHit) {
        // if we hit anything other than a player, we don't care
        if (!(thingHit instanceof Player)) { return; }
        // otherwise kill ourselves, the player will grab us
        this.removeSelf();
    }
}
