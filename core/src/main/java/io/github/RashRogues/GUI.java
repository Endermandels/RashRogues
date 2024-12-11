package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GUI {

    private HealthBar hb;

    public GUI(Player player) {
        hb = new HealthBar(player);
    }

    public void update() {
        hb.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
    }
}

class HealthBar extends Entity{

    private final long FREQUENCY = 50L;
    private final float X = 10f;
    private final float Y = 10f;
    private final float SCALE = 2f;
    private static final float TEXTURE_WIDTH = 64;
    private static final float TEXTURE_HEIGHT = 16;

    private long lastShaked;
    private int lastHP;
    private int shakeY;
    private int bars; // Number of bars to show (from 0 to 8)

    private Player player;
    private TextureRegion image;

    public HealthBar(Player player) {
        super(EntityAlignment.UI, RRGame.am.get(RRGame.RSC_HEALTH_BAR), 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT, Layer.FOREGROUND, ReplicationType.CLIENTSIDE, player.pid, -1);

        // The Health Bar has 9 states in total, from empty to full 8 bars
        shakeY = 0;
        lastShaked = -1L;
        this.player = player;
        lastHP = player.stats.getHealth();
        bars = 8;

        // TODO: change to animation
        Texture texture = RRGame.am.get(RRGame.RSC_HEALTH_BAR);
        // For some reason, I couldn't use the TEXTURE_WIDTH/HEIGHT variables here
        image = new TextureRegion(texture, 0, 0, 64, 16);
    }

    public void update() {
        long time = System.currentTimeMillis();
        if (shakeY != 0 && time > lastShaked + FREQUENCY) {
            shakeY = (Math.abs(shakeY) - 2) * -Integer.signum(shakeY);
            lastShaked = time;
        }
        if (player.stats.getHealth() != lastHP) {
            // TODO: start animation
            lastHP = player.stats.getHealth();
            bars = (int) (8f * ((float)lastHP/(float)player.stats.getMaxHealth()));
            shake();
        }
    }

    public void shake() {
        shakeY = 10;
    }

    @Override
    public void draw(Batch batch) {
        // TODO: add animation
        batch.draw(image, X, Y+shakeY, TEXTURE_WIDTH*SCALE, TEXTURE_HEIGHT*SCALE);
    }
}
