package io.github.RashRogues;

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

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RRGame extends Game {
    AssetManager am;
    SpriteBatch batch;
    SpriteBatch hudBatch;
    ShapeRenderer shapeRenderer;
    LaggingCamera playerCam;

    // constants
    public static final float WORLD_WIDTH = 80;
    public static final int PLAYER_SPAWN_X = 40;
    public static final int PLAYER_SPAWN_Y = 30;
    public static final float CAMERA_SIZE = 30;
    public static final float PLAYER_SIZE = 2;

    public static final String RSC_MONO_FONT_FILE = "JetBrainsMono-Regular.ttf";
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

    @Override
    public void create() {
        am = new AssetManager();

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

        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        float h = Gdx.graphics.getHeight();
        float w = Gdx.graphics.getWidth();
        playerCam = new LaggingCamera(CAMERA_SIZE, CAMERA_SIZE * (h/w));
        playerCam.position.set(playerCam.viewportWidth / 2f, playerCam.viewportHeight / 2f, 0);
        playerCam.update();
        setScreen(new LoadScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        playerCam.viewportWidth = CAMERA_SIZE;
        playerCam.viewportHeight = CAMERA_SIZE * ((float) height/width);
        playerCam.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        am.dispose();
    }
}
