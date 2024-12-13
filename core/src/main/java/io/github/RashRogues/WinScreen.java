package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class WinScreen extends ScreenAdapter implements RRScreen {

    RRGame game;
    BitmapFont font;
    float height;

    public WinScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.game = game;
        font = RRGame.am.get(RRGame.RSC_MONO_FONT_WIN);
        height = (float) Gdx.graphics.getHeight()/2 + 64f;
    }

    @Override
    public void show() {
        Gdx.app.log("WinScreen", "show");
    }

    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        game.hudBatch.begin();
        font.draw(game.hudBatch, "YOU WIN", 48f, height);
        game.hudBatch.end();
    }

    @Override
    public void nextScreen() {

    }

    @Override
    public void nextScreen(Screen screen) {

    }

    @Override
    public void registerEntity(Entity entity) {
        return;
    }

    @Override
    public void removeEntity(Entity entity) {
        return;
    }

    @Override
    public void executeCommand(String[] cmd) {
        return;
    }

    @Override
    public GUI getGUI() {
        return null;
    }

    @Override
    public Room getRoom() {
        return null;
    }

}
