package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Player extends Entity {

    private final float ACCELERATION = 50.0f;
    private final float FRICTION = 100.0f;

    Player(Texture texture, int x, int y) {
        super(texture, x, y);
        // can override maxX/Y velocity here
    }

    public void takeInput() {
        float xVel = 0;
        float yVel = 0;
        if  (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            xVel -= ACCELERATION;
            this.flipped = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            xVel += ACCELERATION;
            this.flipped = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            yVel -= ACCELERATION;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            yVel += ACCELERATION;
        }

        // this is to ensure speed is constant even when going diagonal
        if (xVel != 0 && yVel != 0) {
            xVel = (float) (xVel / Math.sqrt(2));
            yVel = (float) (yVel / Math.sqrt(2));
        }
        if (xVel == 0 && xVelocity != 0) {
            float sign = Math.signum(xVelocity);
            xVelocity -= sign*FRICTION;
            if (sign != Math.signum(xVelocity)) {
                xVelocity = 0;
            }
        }
        if (yVel == 0 && yVelocity != 0) {
            float sign = Math.signum(yVelocity);
            yVelocity -= sign*FRICTION;
            if (sign != Math.signum(yVelocity)) {
                yVelocity = 0;
            }
        }

        xVelocity += xVel;
        yVelocity += yVel;

        xVelocity = Math.max(-maxXVelocity, Math.min(xVelocity, maxXVelocity));
        yVelocity = Math.max(-maxYVelocity, Math.min(yVelocity, maxYVelocity));
    }
}
