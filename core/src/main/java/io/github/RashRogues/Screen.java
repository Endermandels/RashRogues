package io.github.RashRogues;

public interface Screen {
    /**
     * Goes to the next screen.
     */
    public void nextScreen();

    /**
     * Registers an entity to be rendered and updated on this screen.
     */
    public void registerEntity(Entity entity);
}
