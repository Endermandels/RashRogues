package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadScreen extends ScreenAdapter implements Screen{
    RRGame game;

    public LoadScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.app.log("LoadScreen", "show");
    }

    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        game.am.update(10);
        if (game.am.isFinished()) {
            game.setScreen(new LobbyScreen(game));
        }
    }

    @Override
    public void nextScreen() {

    }

    @Override
    public void registerEntity(Entity entity) {
        return;
    }
}
