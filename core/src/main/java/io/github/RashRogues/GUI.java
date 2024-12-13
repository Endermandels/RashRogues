package io.github.RashRogues;

import Networking.ReplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import static java.lang.Math.max;

public class GUI {

    private HealthBar hb;
    private SpecialAttack sa;
    private MerchantMenu mm;

    public GUI(Player player) {
        hb = new HealthBar(player);
        sa = new SpecialAttack(player);
        mm = new MerchantMenu(player);
    }

    public void update() {
        hb.update();
        mm.update();
    }

    public void draw(Batch batch) {
        hb.draw(batch);
        sa.draw(batch);
        mm.draw(batch);
    }

    public void resize(int width, int height) {
        hb.resize(width, height);
        sa.resize(width, height);
        mm.resize();
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

class MerchantMenu {
    private float detailPaneWidth  = 128;
    private float detailPaneHeight = 128;
    private float detailPaneX      = 0;
    private float detailPaneY      = 0;

    private float itemPaneWidth  = 256;
    private float itemPaneHeight = 128;
    private float itemPaneX      = detailPaneWidth + 8;
    private float itemPaneY      = 0;

    private float itemSelectX = 0;
    private float itemSelectY = 0;
    private float itemSelectWidth = 32;
    private float itemSelectHeight = 32;

    Texture detailPaneTexture;
    Texture itemPaneTexture;
    Texture selectionTexture;

    private BitmapFont font;

    private ShopItem[] items = {
        new ShopItem("Throwing Knife", "Increases weapon damage.", 50, RRGame.am.get(RRGame.RSC_THROWING_KNIFE_IMG)),
        new ShopItem("Ring", "Increases player speed.", 70, RRGame.am.get(RRGame.RSC_RING_IMG)),
        new ShopItem("Health Potion", "Recovers HP.", 90, RRGame.am.get(RRGame.RSC_HEALTH_POTION_IMG)),
        new ShopItem("Dagger", "Increases throwing rate.", 120, RRGame.am.get(RRGame.RSC_DAGGER_IMG)),
        new ShopItem("Cloak", "Increases player defense.", 120, RRGame.am.get(RRGame.RSC_CLOAK_IMG))
    };

    private class ShopItem {
        private String title;
        private String description;
        private int cost;
        private Texture texture;

        public ShopItem(String title, String description, int cost, Texture texture){
            this.title = title;
            this.description = description;
            this.cost = cost;
            this.texture = texture;
        }

        public String getTitle(){
            return this.title;
        }

        public String getDescription(){
            return this.description;
        }

        public int getCost(){
            return this.cost;
        }

        public Texture getTexture(){
            return this.texture;
        }
    }

    public MerchantMenu(Player player){
        detailPaneTexture = RRGame.am.get(RRGame.RSC_SHOP_DETAILED_VIEW);
        itemPaneTexture   = RRGame.am.get(RRGame.RSC_SHOP_ITEMS_VIEW);
        selectionTexture   = RRGame.am.get(RRGame.RSC_SHOP_ITEMS_SELECT);
        this.font = new BitmapFont(Gdx.files.local("Fonts/merchant.fnt"));
    }

    public void resize(){
        detailPaneWidth  = Gdx.graphics.getWidth() / 1.8f;
        detailPaneHeight = detailPaneWidth;
        detailPaneX      = 16;
        detailPaneY      = Gdx.graphics.getHeight() - detailPaneHeight - 16;
        int itemSelected = 0;

        itemPaneWidth    = Gdx.graphics.getWidth() / 3;
        itemPaneHeight   = itemPaneWidth * 2;
        itemPaneX        = Gdx.graphics.getWidth() - 16 - itemPaneWidth;
        itemPaneY        = Gdx.graphics.getHeight() - 16 - itemPaneHeight;

        itemSelectWidth  = itemPaneHeight / 4;
        itemSelectHeight = itemSelectWidth;

    }

    public void update(){

    }

    public void draw(Batch batch) {
        batch.draw(this.detailPaneTexture, detailPaneX,detailPaneY, detailPaneWidth, detailPaneHeight);
        batch.draw(this.itemPaneTexture, itemPaneX,itemPaneY, itemPaneWidth, itemPaneHeight);

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        int sel = -1;

        for (int i = 0; i < this.items.length; i++) {
            ShopItem item = items[i];
            float x = 0;
            float y = 0;

            if (i < 4) {
                x = this.itemPaneX;
                y = this.itemPaneY + this.itemSelectWidth * i;
            } else {
                x = this.itemPaneX + this.itemSelectWidth;
                y = this.itemPaneY + this.itemSelectWidth * (i - 4);
            }

            if (mx > x && mx < x + this.itemSelectWidth) {
                if (my > y && my < y + this.itemSelectWidth) {
                    batch.draw(this.selectionTexture, x, y, itemSelectWidth, itemSelectHeight);
                    sel = i;
                }
            }
            batch.draw(item.getTexture(), x, y, itemSelectWidth, itemSelectHeight);
        }

        if (sel != -1){
            this.font.setColor(Color.WHITE);
            this.font.getData().setScale(4);
            font.draw(batch, items[sel].title, this.detailPaneX + 48, this.detailPaneY + this.detailPaneHeight - 48);
            this.font.getData().setScale(3);
            font.draw(batch, items[sel].description, this.detailPaneX + 48, this.detailPaneY + this.detailPaneHeight - 84);
            font.draw(batch, Integer.toString(items[sel].cost) + " GP", this.detailPaneX + 48, this.detailPaneY + this.detailPaneHeight - 116);
        }
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
