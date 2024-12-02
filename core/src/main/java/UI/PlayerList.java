package UI;

import Networking.Network;
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

public class PlayerList extends Entity {
    private HashSet<String> players;
    private static Texture itemTex = RRGame.am.get(RRGame.RSC_GAME_LIST_ITEM);
    private BitmapFont font = new BitmapFont();

    public PlayerList(float x, float y, int width, int height){
        super(EntityType.UI, EntityAlignment.BACKGROUND, RRGame.am.get(RRGame.RSC_GAME_LIST),x,y,width,height, Layer.BACKGROUND);
        this.players         = new HashSet<>();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void draw(Batch batch){
        super.draw(batch);

        int index = 0;
        for (Integer pid : RRGame.globals.clientSet){
            int itemHeight = (int) (this.getHeight() / Network.MAX_CLIENTS);
            int X = (int) this.getX();
            int Y = (int) this.getY() + (int) this.getHeight() - (itemHeight*(index+1));
            batch.draw(itemTex,X,Y,this.getWidth(), itemHeight);
            if (pid == RRGame.globals.pid){
                font.setColor(0.6f,0.0f,0.0f,1.0f);
            }else{
                font.setColor(0f,0f,0f,1.0f);
            }
            font.draw(batch,"Player " + Integer.toString(pid),X+8,Y+(itemHeight)-16);
            index++;
        }
    }
}
