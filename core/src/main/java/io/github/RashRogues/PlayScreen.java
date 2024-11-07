package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class PlayScreen extends ScreenAdapter {

    private RRGame game;
    public Player player; //TODO: switch this back to private
    private Button joinbtn;
    private Button hostbtn;

    public PlayScreen(RRGame game) {
        this.game = game;
        this.player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), 50, 50);

        //TODO: don't give these playscreen in the future.
        this.joinbtn = new Button(this,game.am.get(RRGame.JOIN_IMG),256,64, Button.ButtonActions.JOIN_MULTIPLAYER);
        this.hostbtn = new Button(this,game.am.get(RRGame.HOST_IMG),128,64,Button.ButtonActions.HOST_MULTIPLAYER);
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
        joinbtn.update(delta);
        hostbtn.update(delta);
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
        hostbtn.draw(game.batch);
        joinbtn.draw(game.batch);
        // draw enemies

        // draw projectiles

        // draw misc

        game.batch.end();
    }
}
