package Networking;

import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.RashRogues.*;
import java.util.ArrayList;
import static io.github.RashRogues.RRGame.RSC_NET_VIEWER;

public class NetViewer extends Entity {

    private String text;

    public NetViewer() {
        super(EntityAlignment.BACKGROUND, RRGame.am.get(RSC_NET_VIEWER), 0, 0, 5, 5, Layer.FOREGROUND, ReplicationType.CLIENTSIDE, -1, -1);
    }

    public void outputToConsole(){
        this.text = getInfo();
        System.out.println(this.text);
    }

    public void enable(){
      super.setAlpha(1);
    }

    public void disable(){
        super.setAlpha(0);
    }

    public void attach(LaggingCamera cam){
    }

    public String getInfo(){
//       ArrayList<Entity> entities = RRGame.globals.getReplicatedEntities();
//       StringBuilder str = new StringBuilder();
//       str.append("Networked Entities: \n");
//       str.append("---------------\n");
//       for (Entity e : entities){
//           str.append(e.toString());
//           str.append("\n");
//       }
//       str.append("---------------\n");
//       return str.toString();
        return "";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void draw(Batch batch){
    }
}