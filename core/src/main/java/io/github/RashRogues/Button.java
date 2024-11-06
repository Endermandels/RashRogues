package io.github.RashRogues;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Button extends Sprite {

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

    /**
     * Create a clickable button.
     * @param texture Texture to apply to button
     * @param x X position of button on screen coordinates
     * @param y Y position of button on screen coordinates
     * @param action Action to perform on-click
     */
    Button(Texture texture, int x, int y, ButtonActions action){
       super(texture);
       this.setPosition(x,y);
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
               break;

           case JOIN_MULTIPLAYER:
               break;

           case END_GAME:
               break;

           case START_GAME:
               break;

           default:
               System.out.println("Warning! No action assigned to button");

       }





    }




}
