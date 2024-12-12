package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;


public class Chest extends Entity {

    private boolean open;
    private final float AMOUNT_NEEDED_TO_OPEN = 50f;
    private float currentAmount;
    private final int BASE_MAX_AMOUNT_OF_COIN = 20;
    private final int MAX_DROP_DISTANCE = 11;

    private Sound openSFX;

    Chest(float x, float y) {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_CHEST_IMG, Texture.class),
                x, y, RRGame.CHEST_SIZE, RRGame.CHEST_SIZE, Layer.BACKGROUND, AnimationActor.CHEST,
                ReplicationType.ENTITY_NUMBER,-1,-1);
        this.setBoxPercentSize(0.48f, 0.41f, hitBox);
        hitBox.disableLength = 0.5f;
        this.open = false;
        this.currentAmount = 0;
        this.toggleAnimations(false);
        openSFX = RRGame.am.get(RRGame.RSC_DOOR_OPEN_SFX);
    }

    @Override
    public void onHit(Entity thingHit) {
        // if we hit anything other than a player, we don't care
        if (!(thingHit instanceof Player)) { return; }
        Player player = (Player) thingHit;
        // if we hit a player, unlock a portion of the chest equal to their dexterity
        currentAmount += player.stats.getDexterity();
        // if we're over the amount needed, open the chest!
        if (currentAmount >= AMOUNT_NEEDED_TO_OPEN && !open) {
            open = true;
            this.toggleAnimations(true);
            setCurrentAnimation(AnimationAction.OPEN);
            openSFX.play(0.2f);
            int maxCoinDropped = (int) player.stats.getDexterity() + BASE_MAX_AMOUNT_OF_COIN;
            int numCoin = RRGame.globals.getRandomInteger(maxCoinDropped);
            for (int ii = 0; ii < numCoin; ii++) {
                float x = RRGame.globals.getRandomInteger(MAX_DROP_DISTANCE) - 5;
                float y = RRGame.globals.getRandomInteger(MAX_DROP_DISTANCE) - 5;
                new Coin(getX() + x, getY() + y);
            }
        }
    }
}
