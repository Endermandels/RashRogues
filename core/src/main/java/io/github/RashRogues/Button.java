package io.github.RashRogues;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    public PlayScreen theScreen; //todo: do not import this shit, nor use it.

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
    Button(PlayScreen screen, Texture texture, int x, int y, ButtonActions action){
       super(texture);
       this.setPosition(x,y);
       this.state = ButtonStates.IDLE;
       this.action = action;
       this.theScreen = screen;
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
                if (mouseover()){
                    state = ButtonStates.HOVER;
                }
                break;

            case HOVER:

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
               RRGame.mp.setup(Multiplayer.ClientType.SERVER);
               RRGame.mp.registerPlayer(theScreen.player);//TODO: fix this shit
               break;

           case JOIN_MULTIPLAYER:
               RRGame.mp.setup(Multiplayer.ClientType.CLIENT);
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
