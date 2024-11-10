package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;

public class Enemy extends Entity {

    protected EnemyStats stats;

   Enemy(Texture texture, int x, int y, float width, float height, Layer layer){
        super(texture,x,y,width,height,layer);
    }

   Enemy(Texture texture, int x, int y, float width, float height) {
        this(texture,x,y,width,height,Layer.ENEMY);
    }

    Enemy(Texture texture, int x, int y, float size) {
        this(texture,x,y,size,size,Layer.ENEMY);
    }

    protected void levelUpEnemy() {
        stats.health += 3;
        stats.damage += 3;
        stats.attackSpeed += 0.2f;
        stats.moveSpeed += 0.5f;
    }
}
