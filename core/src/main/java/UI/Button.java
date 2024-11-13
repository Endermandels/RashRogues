package UI;

import Networking.Network;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.RRGame;

public class Button extends Entity {

    public static enum ButtonStates{
        IDLE,
        HOVER,
        DISABLED
    }

    public static enum ButtonActions{
        JOIN_MULTIPLAYER,
        HOST_MULTIPLAYER,
        START_GAME,
        END_GAME
    }

    private ButtonStates state;
    private ButtonActions action;
    private RRGame game;

    public int timeoutTime = 15;
    private int timeoutElapsed = 0;
    private boolean timeoutActive = false;

    /**
     * Create a clickable button.
     * @param texture Texture to apply to button
     * @param x X position of button on screen coordinates
     * @param y Y position of button on screen coordinates
     * @param action Action to perform on-click
     */
    public Button(RRGame game, Texture texture, int x, int y, ButtonActions action){
        super(EntityType.UI,texture,x,y,128,64);
        this.setPosition(x,y);
        this.state = ButtonStates.IDLE;
        this.action = action;
        this.game = game;
    }

    public boolean mouseover(){
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float l = this.getX();
        float r = this.getX() + this.getWidth();
        float b = this.getY();
        float t = this.getY() + this.getHeight();

        if (mouseX > l && mouseX < r){
            if (mouseY > b && mouseY < t){
                return true;
            }
        }
        return false;
    }

    public void update(float delta){
        switch (state){
            case IDLE:
                this.setColor(Color.WHITE);
                if (mouseover()){
                    state = ButtonStates.HOVER;
                }
                break;

            case HOVER:
                this.setColor(Color.TAN);

                //our mouse is outside the button
                if (!mouseover()) {
                    state = ButtonStates.IDLE;
                    break;
                }

                //button is cooling down
                if (timeoutActive == true && timeoutElapsed < timeoutTime){
                    timeoutElapsed +=1;
                    break;
                }else{
                    timeoutElapsed = 0;
                    timeoutActive = false;
                }

                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                   this.activate();
                   timeoutActive = true;
                }
                break;

            case DISABLED:
                break;
        }
    }

    /**
     * Render the button unclickable
     */
    public void disable(){
        this.state = ButtonStates.DISABLED;
    }

    /**
     * Render the button clickable
     */
    public void enable(){
        this.state = ButtonStates.IDLE;
    }


    /**
     * Force execute the button's assigned on-click action.
     */
    public void force(){
        this.activate();
    }

    /**
     * Set a new action for the button to execute on-click.
     * @param action
     */
    public void set(ButtonActions action){
        this.action = action;
    }

    /**
     * Perform action
     */
    private void activate(){
       switch(action){
           case HOST_MULTIPLAYER:
               game.network.start(Network.EndpointType.SERVER);
               this.setColor(Color.DARK_GRAY);
               this.state = ButtonStates.DISABLED;
               break;

           case JOIN_MULTIPLAYER:
               game.network.start(Network.EndpointType.CLIENT);
               this.setColor(Color.DARK_GRAY);
               this.state = ButtonStates.DISABLED;
               break;

           case END_GAME:
               break;

           case START_GAME:
               break;

           default:
               System.out.println("Warning: a button with no action binding was just triggered.");
               break;
       }
    }
}
