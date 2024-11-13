package io.github.RashRogues;

import java.util.Comparator;

public class EntityComparator implements Comparator<Entity> {
   public int compare(Entity e1, Entity e2){
        return Integer.compare(e1.layer.getValue(),e2.layer.getValue());
   }
}
