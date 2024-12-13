package io.github.RashRogues;

import Networking.Network;
import Networking.Solicitor;
import UI.Button;
import UI.PlayerList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashSet;
import java.util.PriorityQueue;

public class LobbyScreen extends ScreenAdapter implements RRScreen {
    RRGame game;
    PriorityQueue<Entity> renderQueue = new PriorityQueue<>(new EntityComparator());
    HashSet<Entity> localEntities = new HashSet<>();
    private Solicitor solicitor;
    private boolean serving;
    private PlayerList playerList;

    public LobbyScreen(RRGame game) {
        // this is a bandaid fix
        RRGame.playerCam.changeWorldSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1000, 1000);
        RRGame.globals.currentScreen = this;
        this.solicitor = new Solicitor();
        this.game = game;
        this.game.network.start(Network.EndpointType.SERVER);
        this.serving = true;
        playerList = new PlayerList(5,5,300,475);
        Button start = new Button(game,RRGame.am.get(RRGame.RSC_BTN_START_GAME),Gdx.graphics.getWidth()-140,5, Button.ButtonActions.START_GAME);
    }

    public LobbyScreen(RRGame game, String ip){
        RRGame.globals.currentScreen = this;
        this.game = game;
        Network.ip = ip;
        this.game.network.start(Network.EndpointType.CLIENT);
        this.serving = false;
        playerList = new PlayerList(50,50,300,400);
        Button start = new Button(game,RRGame.am.get(RRGame.RSC_BTN_START_GAME), Gdx.graphics.getWidth()-140,5, Button.ButtonActions.START_GAME);
        start.disable();
    }

    public void update(float delta){
        if (serving){
            this.solicitor.solicit();
        }else{
        }


        for (Entity e : localEntities){
            e.update(delta);
            renderQueue.add(e);
        }

        if (game.network.type != Network.EndpointType.UNSET){
            game.network.connection.processMessages();
        }
    }

    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0, 0, 0, 1);

        /* Update and Enqueue for rendering */
        this.game.batch.begin();
        this.playerList.draw(this.game.batch);
        for (Entity e : renderQueue){
            e.draw(this.game.batch);
        }

        this.game.batch.end();
    }

    @Override
    public void nextScreen() {
        if (this.solicitor != null){
            this.solicitor.dispose();
        }

        game.setScreen(new PlayScreen(game));
    }

    @Override
    public void nextScreen(Screen screen) {
        if (this.solicitor != null){
            this.solicitor.dispose();
        }

        game.setScreen(screen);
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
        if (this.solicitor != null){
            this.solicitor.dispose();
        }
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
