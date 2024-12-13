package Networking;

import io.github.RashRogues.BuyableItem;
import io.github.RashRogues.Player;

public interface Endpoint {
    /**
     * Read and apply all new messages from the network.
     */
    public void processMessages();
    public void dispatchStartGame();
    public void dispatchCreatePlayer(Player player);
    public void dispatchKeys(byte[] keyMask, long keys);
    public void dispatchHeartbeat();
    public void forward(byte[] packet);
    public void dispose();
    public void dispatchDestroyEntity(int eid);
    public void dispatchDestroyEntity2(int pid, long frame);
    public void dispatchDestroyProjectile(int pid, long number);
    public void dispatchSeed(long randomSeed);
    public void dispatchKillPlayer(int pid);
    public void dispatchCommand(String[] cmd);
    void dispatchKeyPickup(int pid);
    void dispatchEnterMerchant(int pid);
    void dispatchLeaveMerchant(int pid);
    void dispatchUpgrade(int pid, BuyableItem item);
}