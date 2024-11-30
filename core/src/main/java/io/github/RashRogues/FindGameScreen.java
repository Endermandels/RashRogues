package io.github.RashRogues;

import Networking.Network;
import UI.Button;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class FindGameScreen extends ScreenAdapter implements RRScreen {
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>(new EntityComparator());
    HashSet<Entity> localEntities = new HashSet<>();

    public FindGameScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.game = game;
    }

    public void render(float delta) {
        if (game.network.type != Network.EndpointType.UNSET){
            game.network.connection.processMessages();
        }
        ScreenUtils.clear(0, 0, 0, 1);

        /* Update and Enqueue for rendering */
        this.game.batch.begin();
        for (Entity e : localEntities){
            e.update(delta);
            renderQueue.add(e);
        }

        /* Dequeue and Render */
        while (!renderQueue.isEmpty()){
            renderQueue.poll().draw(game.batch);
        }

        this.game.batch.end();
    }

    @Override
    public void nextScreen() {return;}

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
}
