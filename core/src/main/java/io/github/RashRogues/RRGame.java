package io.github.RashRogues;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RRGame extends Game {
    AssetManager am;
    SpriteBatch batch;
    public static Multiplayer mp = new Multiplayer();
    public static final String RSC_ROGUE_IMG = "DefaultImages/rogue.png";
    public static final String JOIN_IMG = "join.png";
    public static final String HOST_IMG = "host.png";

    @Override
    public void create() {
        am = new AssetManager();

        am.load(RSC_ROGUE_IMG, Texture.class);
        am.load(JOIN_IMG, Texture.class);
        am.load(HOST_IMG, Texture.class);

        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}
