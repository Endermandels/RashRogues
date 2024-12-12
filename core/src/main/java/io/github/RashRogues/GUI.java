package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static java.lang.Math.max;

public class GUI {

    private HealthBar hb;
    private SpecialAttack sa;
    private CoinCount cc;

    public GUI(Player player) {
        hb = new HealthBar(player);
        sa = new SpecialAttack(player);
        cc = new CoinCount(player);
    }

    public void update() {
        hb.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
        sa.draw(batch);
        cc.draw(batch);
    }

    public void resize(int width, int height) {
        hb.resize(width, height);
        sa.resize(width, height);
        cc.resize(width, height);
    }
}

class GUIElement extends Entity {
    protected Player player;

    public GUIElement(Player player, Texture texture, float x, float y,
                      float width, float height, AnimationActor animationActor) {
        super(EntityAlignment.UI, texture, x, y,
            width, height, Layer.FOREGROUND, animationActor,
            ReplicationType.CLIENTSIDE, player.pid, -1);
        this.player = player;
    }
}

class CoinCount extends GUIElement {
    private static final float X = -48f;
    private static final float Y = Gdx.graphics.getHeight() - 130f;
    private float imgWidth;
    private float imgHeight;
    private BitmapFont font;

    public CoinCount(Player player) {
        super(player, RRGame.am.get(RRGame.RSC_COIN_IMG), X, Y,
                RRGame.KEY_SIZE, RRGame.KEY_SIZE, null);
        imgWidth = Gdx.graphics.getWidth() * 0.2f;
        imgHeight = imgWidth;
        font = RRGame.am.get(RRGame.RSC_MONO_FONT_LARGE);
        font.setColor(0,0,0,1);
    }

    public void resize(int width, int height) {
        imgWidth = Gdx.graphics.getWidth() * 0.2f;
        imgHeight = imgWidth;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(getTexture(), X, Y, imgWidth, imgHeight);
        font.draw(batch, Integer.toString(player.getNumCoins()), X+imgWidth/2+20, Y+imgHeight/2+10);
    }
}

class SpecialAttack extends GUIElement {
    private static final float X = 150f;
    private static final float Y = -50f;
    private float imgWidth;
    private float imgHeight;

    public SpecialAttack(Player player) {
        super(player, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG), X, Y,
                RRGame.SMOKE_BOMB_SIZE, RRGame.SMOKE_BOMB_SIZE, null);
        imgWidth = Gdx.graphics.getWidth() * 0.25f;
        imgHeight = imgWidth;
    }

    public void update() {
        // Do animation with: player.getAbilityTimeLeft();
    }

    public void resize(int width, int height) {
        imgWidth = Gdx.graphics.getWidth() * 0.25f;
        imgHeight = imgWidth;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(getTexture(), X, Y, imgWidth, imgHeight);
    }
}

class HealthBar extends GUIElement {

    private final long FREQUENCY = 50L;
    private static final float X = 10f;
    private static final float Y = 10f;
    private float barWidth = Gdx.graphics.getWidth() * 0.25f;
    private float barHeight = barWidth / 4f;

    private long lastShaked;
    private int lastHP;
    private int shakeY;
    private int bars; // Number of bars to show (from 0 to 8)

    public HealthBar(Player player) {
        super(player, RRGame.am.get(RRGame.RSC_HEALTH_BAR), X, Y,
            64f, 16f, AnimationActor.HEALTH_BAR_8);

        // The Health Bar has 9 states in total, from empty to full 8 bars
        shakeY = 0;
        lastShaked = -1L;
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
