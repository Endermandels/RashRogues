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
        sa.update();
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
        // TODO: the part above might need to be player.associatedPID, question for Cade
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
    private static final float PERCENT_DIST_FROM_LEFT_SIDE = 0.25f;
    private static final float PERCENT_DIST_FROM_BOTTOM = 0.03f;
    private static final float BASE_NUM_PIXELS_TO_BOTTOM_OF_BOMB = 12f;
    private static final int BOMB_GUI_NUM_ROWS = 1;
    private static final int BOMB_GUI_NUM_COLS = 8;

    private float xOffset = Gdx.graphics.getWidth()*PERCENT_DIST_FROM_LEFT_SIDE;
    private float yOffset = Gdx.graphics.getHeight()*PERCENT_DIST_FROM_BOTTOM;
    private float imgWidth;
    private float imgHeight;
    private TextureRegion[][] bombGUIFrames;
    private TextureRegion currentFrame = new TextureRegion(RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG, Texture.class), 0, 0, 32, 32);

    public SpecialAttack(Player player) {
        super(player, RRGame.am.get(RRGame.RSC_SMOKE_BOMB_IMG), Gdx.graphics.getWidth()*PERCENT_DIST_FROM_LEFT_SIDE, Gdx.graphics.getHeight()*PERCENT_DIST_FROM_BOTTOM,
                RRGame.SMOKE_BOMB_SIZE, RRGame.SMOKE_BOMB_SIZE, null);
        imgWidth = Gdx.graphics.getWidth() * 0.25f;
        imgHeight = imgWidth;
        Texture bombGUISheet = new Texture(RRGame.RSC_BOMB_GUI_SHEET);
        bombGUIFrames = TextureRegion.split(bombGUISheet,
                bombGUISheet.getWidth() / BOMB_GUI_NUM_COLS,
                bombGUISheet.getHeight() / BOMB_GUI_NUM_ROWS);
    }

    public void update() {
        int frame = max(0, (int) (7f * player.getAbilityTimeLeft()));
        if (frame == 7) {
            currentFrame = bombGUIFrames[0][0];
        }
        else {
            currentFrame = bombGUIFrames[0][frame+1];
        }
    }

    public void resize(int width, int height) {
        imgWidth = width * 0.25f;
        imgHeight = imgWidth;
        xOffset = width*PERCENT_DIST_FROM_LEFT_SIDE;
        // this complicated thing is only needed for this bc its so small compared to its px size of 32x32
        yOffset = height*PERCENT_DIST_FROM_BOTTOM-(imgHeight/32f * BASE_NUM_PIXELS_TO_BOTTOM_OF_BOMB);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(currentFrame, xOffset, yOffset, imgWidth, imgHeight);
    }
}

class HealthBar extends GUIElement {

    private final long FREQUENCY = 50L;
    private static final float PERCENT_DIST_FROM_LEFT_SCREEN = 0.02f;
    private static final float PERCENT_DIST_FROM_BOTTOM = 0.02f;
    private float xOffset = Gdx.graphics.getWidth()*PERCENT_DIST_FROM_LEFT_SCREEN;
    private float yOffset = Gdx.graphics.getHeight()*PERCENT_DIST_FROM_BOTTOM;
    private float barWidth = Gdx.graphics.getWidth() * 0.25f;
    private float barHeight = barWidth / 4f;

    private long lastShaked;
    private int lastHP;
    private int shakeY;
    private int bars; // Number of bars to show (from 0 to 8)

    public HealthBar(Player player) {
        super(player, RRGame.am.get(RRGame.RSC_HEALTH_BAR),  Gdx.graphics.getWidth()*PERCENT_DIST_FROM_LEFT_SCREEN, Gdx.graphics.getHeight()*PERCENT_DIST_FROM_BOTTOM,
            64f, 16f, AnimationActor.HEALTH_BAR_8);

        // The Health Bar has 9 states in total, from empty to full 8 bars
        shakeY = 0;
        lastShaked = -1L;
        lastHP = player.stats.getHealth();
        bars = max(0, (int) (8f * ((float)lastHP/(float)player.stats.getMaxHealth())));
        changeHealthBarActor();
//        System.out.println("health: " + lastHP + "/" + player.stats.getMaxHealth() + "  bars: " + bars);
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
//            System.out.println("health: " + lastHP + "/" + player.stats.getMaxHealth() + "  bars: " + bars);
            changeHealthBarActor();
            setCurrentAnimation(AnimationAction.HURT);
            shake();
        }
    }

    public void changeHealthBarActor() {
        switch (bars) {
            case 0:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_0);
                break;
            case 1:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_1);
                break;
            case 2:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_2);
                break;
            case 3:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_3);
                break;
            case 4:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_4);
                break;
            case 5:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_5);
                break;
            case 6:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_6);
                break;
            case 7:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_7);
                break;
            default:
                this.setUpAnimations(AnimationActor.HEALTH_BAR_8);
                break;
        }
    }

    public void resize(int width, int height) {
        barWidth = width * 0.25f;
        barHeight = barWidth / 4f;
        xOffset = width*PERCENT_DIST_FROM_LEFT_SCREEN;
        yOffset = height*PERCENT_DIST_FROM_BOTTOM;
    }

    public void shake() {
        shakeY = (int) (barHeight/4);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(this.getCurrentAnimationFrame(), xOffset, yOffset+shakeY, barWidth, barHeight);
    }
}
