package io.github.RashRogues;

import Networking.Network;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
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

    public static final String RSC_MONO_FONT_FILE = "Fonts/JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";

    // entity sprites (players, enemies, projectiles)
    public static final String RSC_ROGUE_IMG = "DefaultImages/rogue.png";
    public static final String RSC_SWORDSMAN_IMG = "DefaultImages/swordsman.png";

    // entity animations

    // item/background sprites
    public static final String RSC_ROOM1_IMG = "DefaultImages/room1.png";

    // item animations

    // sounds

    // music

    // ui
    public static final String RSC_BTN_HOST = "Buttons/host.png";
    public static final String RSC_BTN_JOIN = "Buttons/join.png";
    public static final String RSC_BTN_START_GAME = "Buttons/play.png";

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
        am.load(RSC_SWORDSMAN_IMG, Texture.class);

        am.load(RSC_ROOM1_IMG, Texture.class);

        am.load(RSC_BTN_HOST, Texture.class);
        am.load(RSC_BTN_JOIN, Texture.class);
        am.load(RSC_BTN_START_GAME, Texture.class);

        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        network = new Network();

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
    }
}
