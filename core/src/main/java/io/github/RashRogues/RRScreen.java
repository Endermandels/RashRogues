package io.github.RashRogues;

public interface RRScreen {
    /**
     * Goes to the next screen.
     */
    public void nextScreen();

    /**
     * Registers an entity to be rendered and updated on this screen.
     */
    public void registerEntity(Entity entity);

    /**
     * Removes an entity from this screen.
     */
    public void removeEntity(Entity entity);
}
