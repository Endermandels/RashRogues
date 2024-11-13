package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class LobbyScreen extends ScreenAdapter implements Screen{
    RRGame game;

    public LobbyScreen(RRGame game) {
        this.game = game;
    }

    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
    }

    @Override
    public void nextScreen() {
        game.setScreen(new PlayScreen(game));
    }
}
