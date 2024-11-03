package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Entity {

    Player(Texture texture, int x, int y) {
        super(texture, x, y);
        // can override maxX/Y velocity here
    }

    public void takeInput() {
        float xVel = 0;
        float yVel = 0;
        if  (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xVel -= maxXVelocity;
            this.flipped = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xVel += maxXVelocity;
            this.flipped = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            yVel -= maxYVelocity;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            yVel += maxYVelocity;
        }

        // this is to ensure speed is constant even when going diagonal
        if (xVel != 0 && yVel != 0) {
            xVel = (float) (xVel / Math.sqrt(2));
            yVel = (float) (yVel / Math.sqrt(2));
        }

        setXVelocity(xVel);
        setYVelocity(yVel);
    }

}
