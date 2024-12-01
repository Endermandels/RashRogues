package io.github.RashRogues;

import Networking.Network;
import Networking.Solicitor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class HostGameScreen extends ScreenAdapter implements RRScreen {
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>(new EntityComparator());
    HashSet<Entity> localEntities = new HashSet<>();
    private Solicitor solicitor;

    public HostGameScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.solicitor = new Solicitor();
        this.game = game;
    }

    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        /* Update and Enqueue for rendering */
        this.game.batch.begin();
        for (Entity e : localEntities){
            e.update(delta);
            renderQueue.add(e);
        }

        this.solicitor.solicit();

        this.game.batch.end();
    }

    @Override
    public void nextScreen() {return;}

    @Override
    public void nextScreen(Screen screen) {

    }

    @Override
    public void registerEntity(Entity entity) {
        this.localEntities.add(entity);
        return;
    }

    @Override
    public void removeEntity(Entity entity) {
        this.localEntities.remove(entity);
        return;
    }

    public void dispose(){
        this.solicitor.dispose();
    }
}
