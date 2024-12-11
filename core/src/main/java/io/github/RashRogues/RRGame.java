package io.github.RashRogues;

import Networking.Network;
import Networking.StreamMaker;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.HashSet;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RRGame extends Game {
    public static AssetManager am = new AssetManager();
    SpriteBatch batch;
    SpriteBatch hudBatch;
    ShapeRenderer shapeRenderer;
    LaggingCamera playerCam;

    public Network network;
    public HashSet<Entity> globalEntities;

    public static Globals globals = new Globals();

    // constants
    public static final float WORLD_WIDTH = 80;
    public static final int PLAYER_SPAWN_X = 40;
    public static final int PLAYER_SPAWN_Y = 30;
    public static final float CAMERA_SIZE = 30;
    public static final float PLAYER_SIZE = 2;
    public static final float STANDARD_ENEMY_SIZE = 5;
    public static final float DOOR_SIZE = 10;
    public static final float KEY_SIZE = 4;
    public static final float ARROW_SIZE = 2;
    public static final float THROWING_KNIFE_SIZE = 2;
    public static final float BOMB_EXPLOSION_DURATION = 1;
    public static final float SMOKE_BOMB_SIZE = 4;
    public static final float SMOKE_BOMB_EXPLOSION_SIZE = 10;
    public static final float SMOKE_BOMB_EXPLOSION_DURATION = 8;
    public static final float STANDARD_PROJECTILE_SPEED = 20;
    public static final float STANDARD_PROJECTILE_DISTANCE = 40;
    public static final int HEALTH_POTION_HEAL_AMOUNT = 50;

    public static final String RSC_MONO_FONT_FILE = "Fonts/JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";

    // entity sprites (players, enemies, projectiles)
    public static final String RSC_ROGUE_IMG = "DefaultImages/rogue.png";
    public static final String RSC_ARCHER_IMG = "DefaultImages/archer.png";
    public static final String RSC_BOMBER_IMG = "DefaultImages/bomber.png";
    public static final String RSC_SWORDSMAN_IMG = "DefaultImages/swordsman.png";
    public static final String RSC_ARROW_IMG = "DefaultImages/arrow.png";
    public static final String RSC_THROWING_KNIFE_IMG = "DefaultImages/throwing_knife.png";
    public static final String RSC_SMOKE_BOMB_IMG = "DefaultImages/bomb.png";
    public static final String RSC_SMOKE_BOMB_EXPLOSION_IMG = "DefaultImages/explosion.png";

    // entity animations

    // item/background sprites
    public static final String RSC_ROOM1_IMG = "DefaultImages/room1.png";
    public static final String RSC_ROOM2_IMG = "DefaultImages/room2.png";
    public static final String RSC_KEY_IMG = "DefaultImages/key.png";
    public static final String RSC_DOOR_IMG = "DefaultImages/door.png";

    //debug tools
    public static final String RSC_NET_VIEWER = "Menu/net_viewer.png";

    // item animations

    // sounds

    // music

    // ui
    public static final String RSC_BTN_HOST = "Buttons/host.png";
    public static final String RSC_BTN_JOIN = "Buttons/join.png";
    public static final String RSC_BTN_START_GAME = "Buttons/play.png";
    public static final String RSC_GAME_LIST = "Menu/game_list_background.png";
    public static final String RSC_GAME_LIST_ITEM = "Menu/game_list_item.png";
    public static final String RSC_HEALTH_BAR = "GUI/health_bar.png";

    @Override
    public void create() {
        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_MONO_FONT_FILE;
        myFont.fontParameters.size = 14;
        am.load(RSC_MONO_FONT, BitmapFont.class, myFont);

        am.load(RSC_ROGUE_IMG, Texture.class);
        am.load(RSC_ARCHER_IMG, Texture.class);
        am.load(RSC_BOMBER_IMG, Texture.class);
        am.load(RSC_SWORDSMAN_IMG, Texture.class);
        am.load(RSC_ARROW_IMG, Texture.class);
        am.load(RSC_THROWING_KNIFE_IMG, Texture.class);
        am.load(RSC_SMOKE_BOMB_IMG, Texture.class);
        am.load(RSC_SMOKE_BOMB_EXPLOSION_IMG, Texture.class);

        am.load(RSC_ROOM1_IMG, Texture.class);
        am.load(RSC_ROOM2_IMG, Texture.class);
        am.load(RSC_KEY_IMG, Texture.class);
        am.load(RSC_DOOR_IMG, Texture.class);

        am.load(RSC_NET_VIEWER, Texture.class);

        am.load(RSC_BTN_HOST, Texture.class);
        am.load(RSC_BTN_JOIN, Texture.class);
        am.load(RSC_BTN_START_GAME, Texture.class);
        am.load(RSC_GAME_LIST, Texture.class);
        am.load(RSC_GAME_LIST_ITEM, Texture.class);
        am.load(RSC_HEALTH_BAR, Texture.class);

        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Globals.network = new Network();
        network = Globals.network;

        globalEntities = new HashSet<>();

        float h = Gdx.graphics.getHeight();
        float w = Gdx.graphics.getWidth();
        playerCam = new LaggingCamera(CAMERA_SIZE, CAMERA_SIZE * (h/w));
        playerCam.center();
        playerCam.update();
        setScreen(new LoadScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        playerCam.viewportWidth = CAMERA_SIZE;
        playerCam.viewportHeight = CAMERA_SIZE * ((float) height/width);
        playerCam.update();
        this.screen.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        am.dispose();
        this.network.dispose();
        RRGame.globals.currentScreen.dispose();
    }
}
