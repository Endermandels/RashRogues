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
    public void dispatchCreatePlayer(Player player);
    public void dispatchKeys(byte[] keyMask);
    public void dispatchHeartbeat();
    public void forward(byte[] packet);
    public void dispose();
    public void dispatchDestroyEntity(int eid);
}