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
    public void dispatchCreatePlayer(int x, int y);
    public void dispose();
    public void dispatchKeys(byte[] keyMask);
}