package UI;

import Networking.Solicitee;
import io.github.RashRogues.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GameList extends Entity {
    private HashSet<String> games;
    private HashMap<String, Integer> gamesAge;
    private Solicitee solicitee;
    private static int STALE_GAME_FRAMES = 60;

    public GameList(float x, float y, int width, int height){
        super(EntityType.UI, EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_GAME_LIST),x,y,width,height, Layer.BACKGROUND);
        this.solicitee  = new Solicitee();
        this.solicitee.listen();
        this.games      = new HashSet<>();
        this.gamesAge   = new HashMap<>();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Add-to/Update list of games.
        ArrayList<String> updates = this.solicitee.fetch();
        for (String s : updates){
            this.games.add(s);
            this.gamesAge.put(s,0);
        }

        System.out.println(this.games.size());

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
}
