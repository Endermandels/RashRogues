package io.github.RashRogues;

import com.badlogic.gdx.Screen;

public interface RRScreen {
    /**
     * Goes to the next screen.
     */
    public void nextScreen();

    /**
     * Goes to a screen.
     * @param screen
     */
    public void nextScreen(Screen screen);

    /**
     * Registers an entity to be rendered and updated on this screen.
     */
    public void registerEntity(Entity entity);

    /**
     * Removes an entity from this screen.
     */
    public void removeEntity(Entity entity);

    void dispose();
}
