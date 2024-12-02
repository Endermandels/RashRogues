package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashSet;

public class Door extends Entity {

    private boolean locked;
    private HashSet<Player> playersAtDoor;

    Door(float x, float y) {
        super(EntityType.BACKGROUND, EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_DOOR_IMG, Texture.class),
                x, y, RRGame.DOOR_SIZE, RRGame.DOOR_SIZE, Layer.BACKGROUND, false);
        this.setBoxPercentSize(0.6f, 0.98f, hitBox);
        this.locked = true;
        this.playersAtDoor = new HashSet<>();
    }

    @Override
    public void onHit(Entity thingHit) {
        // if we hit anything other than a player, we don't care
        if (!(thingHit instanceof Player)) { return; }
        Player player = (Player) thingHit;
        // if we hit a player and we're locked, then set to unlocked
        if (player.isHoldingKey() && locked) { locked = false; } // play animation
        if (!locked) {
            playersAtDoor.add(player);
        }
        if (playersAtDoor.size() == RRGame.globals.currentNumPlayers) {
            this.removeSelf();
        }

    }

}
