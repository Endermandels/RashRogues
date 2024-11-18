package io.github.RashRogues;

import Networking.Network;
import UI.Button;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class LobbyScreen extends ScreenAdapter implements RRScreen {
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>(new EntityComparator());
    HashSet<Entity> localEntities = new HashSet<>();

    public LobbyScreen(RRGame game) {
        RRGame.globals.currentScreen = this;
        this.game = game;
        Button host = new Button(game,game.am.get(RRGame.RSC_BTN_HOST),128,256, Button.ButtonActions.HOST_MULTIPLAYER);
        Button join = new Button(game,game.am.get(RRGame.RSC_BTN_JOIN),256,256, Button.ButtonActions.JOIN_MULTIPLAYER);
        Button start = new Button(game,game.am.get(RRGame.RSC_BTN_START_GAME),192,128, Button.ButtonActions.START_GAME);

        host.addDependent(start);
        join.addDependent(start);
        host.addExclusive(join);
        join.addExclusive(host);
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
    public void nextScreen() {
        game.setScreen(new PlayScreen(game));
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
}
