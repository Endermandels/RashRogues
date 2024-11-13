package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.PriorityQueue;

public class LobbyScreen extends ScreenAdapter implements Screen{
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>();

    public LobbyScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.game = game;
    }

    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
    }

    @Override
    public void nextScreen() {
        game.setScreen(new PlayScreen(game));
    }

    @Override
    public void registerEntity(Entity entity) {
        return;
    }
}
