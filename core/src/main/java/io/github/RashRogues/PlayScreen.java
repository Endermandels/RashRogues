package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class PlayScreen extends ScreenAdapter {

    private RRGame game;
    private Player player;

    public PlayScreen(RRGame game) {
        this.game = game;
        this.player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), 50, 50);
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {

        // update room/objects

        // update player(s)
        player.takeInput();
        player.update(delta);

        // update enemies

        // update projectiles

        // update misc

    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.batch.begin();

        // draw room/objects

        // draw player(s)
        player.draw(game.batch);

        // draw enemies

        // draw projectiles

        // draw misc

        game.batch.end();
    }
}
