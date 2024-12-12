package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static java.lang.Math.max;

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

    public void resize(int width, int height) {
        hb.resize(width, height);
    }
}

class HealthBar extends Entity{

    private final long FREQUENCY = 50L;
    private final float X = 10f;
    private final float Y = 10f;
    private float barWidth = Gdx.graphics.getWidth() * 0.25f;
    private float barHeight = barWidth / 4f;
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
        TEXTURE_WIDTH, TEXTURE_HEIGHT, Layer.FOREGROUND, AnimationActor.HEALTH_BAR_8,
                ReplicationType.CLIENTSIDE, player.pid, -1);

        // The Health Bar has 9 states in total, from empty to full 8 bars
        shakeY = 0;
        lastShaked = -1L;
        this.player = player;
        lastHP = player.stats.getHealth();
        bars = 8;
    }

    public void update() {
        long time = System.currentTimeMillis();
        if (shakeY != 0 && time > lastShaked + FREQUENCY) {
            shakeY = (Math.abs(shakeY) - 2) * -Integer.signum(shakeY);
            lastShaked = time;
        }
        if (player.stats.getHealth() != lastHP) {
            lastHP = player.stats.getHealth();
            bars = max(0, (int) (8f * ((float)lastHP/(float)player.stats.getMaxHealth())));
            System.out.println("health: " + lastHP + "/" + player.stats.getMaxHealth() + "  bars: " + bars);
            switch (bars) {
                case 0:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_0);
                    break;
                case 1:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_1);
                    break;
                case 2:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_2);
                    break;
                case 3:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_3);
                    break;
                case 4:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_4);
                    break;
                case 5:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_5);
                    break;
                case 6:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_6);
                    break;
                case 7:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_7);
                    break;
                default:
                    this.setCurrentAnimation(AnimationAction.HURT, AnimationActor.HEALTH_BAR_8);
                    break;
            }
            shake();
        }
    }

    public void resize(int width, int height) {
        barWidth = Gdx.graphics.getWidth() * 0.25f;
        barHeight = barWidth / 4f;
    }

    public void shake() {
        shakeY = (int) (barHeight/4);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(this.getCurrentAnimationFrame(), X, Y+shakeY, barWidth, barHeight);
    }
}
