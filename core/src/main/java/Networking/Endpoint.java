package Networking;

import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

public interface Endpoint {
    /**
     * Read and apply all new messages from the network.
     */
    public void processMessages();
    public void dispatchStartGame();
    public void dispatchCreate(Entity entity);
    public void dispatchCreate(Player player);
    public void dispatchUpdate(Entity entity);
    public void dispatchUpdate(Player entity);
    public void dispose();
}