package UI;

import Networking.Solicitee;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.github.RashRogues.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameList extends Entity {
    private static int STALE_GAME_FRAMES = 240;
    private final int GAMES_SHOWN  = 4;
    private Solicitee solicitee;
    private HashSet<String> games;
    private HashMap<String,Integer> gamesAge;
    private boolean timeout = false;
    private int timeoutClock = 0;
    private int timeoutTotal = 5;
    private int startIndex  = 0;
    private static Texture itemTex = RRGame.am.get(RRGame.RSC_GAME_LIST_ITEM);
    private BitmapFont font = new BitmapFont();
    private RRGame game;

    public GameList(RRGame game, float x, float y, int width, int height, Solicitee solicitee){
        super(EntityType.UI, EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_GAME_LIST),x,y,width,height, Layer.BACKGROUND, false);
        this.game = game;
        this.games          = new HashSet<>();
        this.gamesAge       = new HashMap<>();
        this.solicitee      = solicitee;
        this.solicitee.listen();
    }

    public void decrement(){
       if (this.startIndex > 0){
           this.startIndex--;
       }
       timeout = true;
       timeoutClock = timeoutTotal;
    }

    public void increment(){
        if (this.startIndex < this.games.size()-1){
            this.startIndex++;
        }
        timeout = true;
        timeoutClock = timeoutTotal;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (Gdx.input.isKeyPressed(Input.Keys.I) && !timeout){
            this.increment();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.J) && !timeout){
            this.decrement();
        }

        if (timeout){
            if (timeoutClock <= 0){
                timeout = false;
            }else{
                timeoutClock--;
            }
        }

        // Add-to/Update list of games.
        ArrayList<String> updates = this.solicitee.fetch();
        for (String s : updates){
            this.games.add(s);
            this.gamesAge.put(s,0);
        }

        ArrayList<String> staleGames = new ArrayList<>();

        // Age list of games
        for (String s : games){
            if (this.gamesAge.get(s) > STALE_GAME_FRAMES){
                staleGames.add(s);
            }
            this.gamesAge.put(s,this.gamesAge.get(s)+1);
        }

        // Remove games that no longer exist from the list.
        for (String s : staleGames){
            this.games.remove(s);
        }
    }

    public void draw(Batch batch){
        super.draw(batch);

        ArrayList<String> gameList = new ArrayList<>();

        for (String s : games){
            gameList.add(s);
        }

        for (int i = 0; i < GAMES_SHOWN; i++){
            if (startIndex+i >= 0 && startIndex+i < gameList.size()){
                int itemHeight = (int) (this.getHeight() / this.GAMES_SHOWN);
                int X = (int) this.getX();
                int Y = (int) this.getY() + (int) this.getHeight() - (itemHeight*(i+1));
                int mouseX = Gdx.input.getX();
                int mouseY = Gdx.graphics.getHeight()-Gdx.input.getY();
                if (mouseX > X && mouseX < this.getX() + this.getWidth() && mouseY > Y && mouseY < Y+itemHeight){
                    batch.setColor(0.9f,0.9f,0.9f,1.0f);
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                        RRGame.globals.currentScreen.nextScreen(new LobbyScreen(this.game, gameList.get(startIndex+i)));
                    }
                }
                batch.draw(itemTex,X,Y,this.getWidth(), itemHeight);
                batch.setColor(1f,1f,1f,1f);
                font.setColor(1,0,0,1);
                font.draw(batch,gameList.get(startIndex+i),X+8,Y+(itemHeight)-16);
            }
        }
    }
}
