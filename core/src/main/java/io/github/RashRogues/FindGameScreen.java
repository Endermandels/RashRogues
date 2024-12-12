package io.github.RashRogues;

import Networking.Network;
import Networking.Solicitee;
import UI.GameList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class FindGameScreen extends ScreenAdapter implements RRScreen {
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>(new EntityComparator());
    HashSet<Entity> localEntities = new HashSet<>();
    private Solicitee solicitee;
    private GameList list;

    public FindGameScreen(RRGame game) {
        // this is a bandaid fix
        RRGame.playerCam.changeWorldSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1000, 1000);
        RRGame.globals.currentScreen = this;
        this.solicitee = new Solicitee();
        this.list = new GameList(game, 50,50,400,400, this.solicitee);
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

        this.list.draw(this.game.batch);

        this.game.batch.end();
    }

    /**
     * Join a solicited game.
     */
    public void join(){
        try {
            game.network.start(Network.EndpointType.CLIENT);
        } catch (GdxRuntimeException e){
            System.out.println("Unable to connect.");
            game.network.reset();
        }
    }

    @Override
    public void nextScreen() {return;}

    @Override
    public void nextScreen(Screen screen) {
        if (this.solicitee != null){
            this.solicitee.dispose();
        }
        this.game.setScreen(screen);
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

    @Override
    public void executeCommand(String[] cmd) {
       return;
    }

    public void dispose(){
        this.solicitee.dispose();
    }

}
