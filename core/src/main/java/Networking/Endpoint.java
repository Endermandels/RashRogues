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
    public void dispatchDestroyPlayer(int pid);
    public void dispatchKeys(byte[] keyMask, long keys, float x, float y);
    public void dispatchHeartbeat();
    public void forward(byte[] packet);
    public void dispose();
    public void dispatchDestroyEntity(int eid);
    public void dispatchDestroyEntity2(int pid, long frame);
    public void dispatchDestroyEntity3(int eid, long number);
    public void dispatchDestroyProjectile(int pid, long number);
    void dispatchSeed(long randomSeed);
    void dispatchKillPlayer(int pid);
    void dispatchTarget(int eid, int pid);
    public void dispatchCommand(String[] cmd);
    void dispatchEnterMerchant(int pid);
    void dispatchLeaveMerchant(int pid);
    void dispatchUpgrade(int pid, BuyableItem item);
    void dispatchKeyPickup(int pid, int keyID);
    void dispatchKeyDrop(float x, float y);
    void dispatchCoinDrop(float x, float y, int level);
}