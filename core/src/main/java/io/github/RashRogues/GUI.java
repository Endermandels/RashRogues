package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import static java.lang.Math.max;

public class GUI {

    private HealthBar hb;
    private SpecialAttack sa;
    private CoinCount cc;
    private HealthPotionCount hpc;

    public GUI(Player player) {
        hb = new HealthBar(player, 10f, 10f);
        sa = new SpecialAttack(player, 150f, -50f);
        cc = new CoinCount(player, -40f, Gdx.graphics.getHeight() - 130f);
        hpc = new HealthPotionCount(player, -40f, Gdx.graphics.getHeight() - 190f);
    }

    public void update() {
        hb.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
        sa.draw(batch);
        cc.draw(batch);
        hpc.draw(batch);
    }

    public void resize(int width, int height) {
        hb.resize(width, height);
        sa.resize(width, height);
        cc.resize(width, height);
        hpc.resize(width, height);
    }
}

class GUIElement extends Entity {
    protected float x, y;

    protected Player player;

    public GUIElement(Player player, Texture texture, float x, float y,
                      float width, float height, AnimationActor animationActor) {
        super(EntityAlignment.UI, texture, x, y,
            width, height, Layer.FOREGROUND, animationActor,
            ReplicationType.CLIENTSIDE, player.pid, -1);
        this.x = x;
        this.y = y;
        this.player = player;
    }
}


class HealthPotionCount extends GUIElement {
    private float imgWidth;
    private float imgHeight;
    private BitmapFont font;

    public HealthPotionCount(Player player, float x, float y) {
        super(player, RRGame.am.get(RRGame.RSC_HEALTH_POTION_IMG), x, y,
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
        batch.draw(getTexture(), x, y, imgWidth, imgHeight);
        font.draw(batch, Integer.toString(player.getHealthPotionCount()), x+imgWidth/2+20, y+imgHeight/2+10);
    }
}

class CoinCount extends GUIElement {
    private float imgWidth;
    private float imgHeight;
    private BitmapFont font;

    public CoinCount(Player player, float x, float y) {
        super(player, RRGame.am.get(RRGame.RSC_COIN_IMG), x, y,
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
        batch.draw(getTexture(), x, y, imgWidth, imgHeight);
        font.draw(batch, Integer.toString(player.getNumCoins()), x+imgWidth/2+20, y+imgHeight/2+10);
    }
}

class SpecialAttack extends GUIElement {
    private float imgWidth;
    private float imgHeight;

    public SpecialAttack(Player player, float x, float y) {
        super(player, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG), x, y,
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
        batch.draw(getTexture(), x, y, imgWidth, imgHeight);
    }
}

class HealthBar extends GUIElement {

    private final long FREQUENCY = 50L;
    private float barWidth = Gdx.graphics.getWidth() * 0.25f;
    private float barHeight = barWidth / 4f;

    private long lastShaked;
    private int lastHP;
    private int shakeY;
    private int bars; // Number of bars to show (from 0 to 8)

    public HealthBar(Player player, float x, float y) {
        super(player, RRGame.am.get(RRGame.RSC_HEALTH_BAR), x, y,
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
        batch.draw(this.getCurrentAnimationFrame(), x, y+shakeY, barWidth, barHeight);
    }
}
