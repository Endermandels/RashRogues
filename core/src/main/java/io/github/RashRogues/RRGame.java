package io.github.RashRogues;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashSet;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RRGame extends Game {
    AssetManager am;
    SpriteBatch batch;
    LaggingCamera playerCam;

    // global entity registry
    public static final HashSet<Entity> instances = new HashSet<>();

    // constants
    public static final float WORLD_WIDTH = 80;
    public static final int PLAYER_SPAWN_X = 40;
    public static final int PLAYER_SPAWN_Y = 30;
    public static final float CAMERA_SIZE = 30;
    public static final float PLAYER_SIZE = 2;

    // entity sprites (players, enemies, projectiles)
    public static final String RSC_ROGUE_IMG = "DefaultImages/rogue.png";

    // entity animations

    // item/background sprites
    public static final String RSC_ROOM1_IMG = "DefaultImages/room1.png";

    // item animations

    // sounds

    // music

    @Override
    public void create() {
        am = new AssetManager();

        am.load(RSC_ROGUE_IMG, Texture.class);

        am.load(RSC_ROOM1_IMG, Texture.class);

        batch = new SpriteBatch();

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
        am.dispose();
    }
}
