package Networking;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.BuyableItem;
import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Networking.PacketType.*;

public class ClientListener implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Server server;
    private Thread listeningThread;
    private Thread speakingThread;
    private int pid = 0;

    public ConcurrentLinkedQueue<byte[]> incomingMessages = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<byte[]> outgoingMessages = new ConcurrentLinkedQueue<>();
    public volatile boolean listening = true;
    public volatile boolean speaking  = true;
    public LinkedHashMap<Integer, Queue<byte[]>> inputQueues;
    public int client_pid;

    /**
     * Server-Client connection, over which gameplay data can be communicated.
     * @param server The master server object.
     * @param socket Active socket to communicate on.
     * @param pid Player ID to associate with this ClientListener.
     */
    public ClientListener(Server server, Socket socket, int pid) {
        this.server = server;
        this.socket     = socket;
        this.in         = socket.getInputStream();
        this.out        = socket.getOutputStream();
        this.client_pid = pid;
        this.inputQueues = new LinkedHashMap<>();

        try {
            /* Send Welcome Packet To New Client */
            this.dispatchWelcome(this.client_pid);

            /* Send Our Seed To The New Client */
            this.dispatchSeed(RRGame.globals.getRandomSeed());
            this.listen(in);
            this.speak(out);

        } catch (IOException | InterruptedException e) {
            System.out.println(">>! Connection with client #" + Integer.toString(this.client_pid) + " closed.");
        }
    }

    /**
     * Listen for and handle incomingmessages from the client.
     * Creates a listening thread seperate from the game thread.
     * Interacts with gamestate via incomingMessages queue.
     * @param in
     * @throws IOException
     * @throws InterruptedException
     */
    private void listen(InputStream in) throws IOException, InterruptedException {
        this.listeningThread = new Thread(
            new Runnable() {
                public void run() {
                    try {
                        while (listening) {
                                byte[] msg = new byte[128];
                                int read = in.read(msg,0,128);
                                if (read > 0) {
                                    incomingMessages.add(msg);
                                }
                        }
                    } catch (IOException ex) {
                        System.out.println(">>! Listener Thread Exited.");
                        System.out.flush();
                    }
                }
            }
        );
        this.listeningThread.start();
    }

    /**
     * Write messages to the server as fast as possible.
     * Creates a speaking thread seperate from the main game thread.
     * Communicates with game state via outgoingMessages queue.
     * @param out
     * @throws IOException
     * @throws InterruptedException
     */
    private void speak(OutputStream out) throws IOException, InterruptedException {
        this.speakingThread = new Thread(
                new Runnable() {
                    public void run() {
                        while (speaking) {
                            while (!outgoingMessages.isEmpty()){
                                byte[] msg = outgoingMessages.poll();
                                try {
                                    out.write(msg);
                                    out.flush();
                                } catch (IOException e) {
                                    System.out.println("Speaking thread stopped.");
                                    System.out.flush();
                                    speaking = false;
                                }
                            }
                        }
                    }
                }
        );
        this.speakingThread.start();
    }

    /**
     * Process Messages from the client.

     * Each message type has a specific handler method.
     * Most Messages will be executed as soon as they are encountered.
     * Input is an exception to this, as it needs to be buffered to make
     * sure we don't cluster multiple player inputs on one frame.
     */
    public void processMessages() {
        try {
            //READ MESSAGES FROM THE LISTENER THREAD
            while ( !this.incomingMessages.isEmpty() ) {
                byte[] msg = this.incomingMessages.poll();
                int msgType = (int) msg[0];

                if (msgType == HEARTBEAT.getvalue()){
                    this.handleHeartBeat();
                } else if ( msgType == FAREWELL.getvalue() ) {
                    this.handleFarewell();
                } else if ( msgType == CREATE_PLAYER.getvalue() ) {
                    this.handleCreatePlayer(msg);
                } else if ( msgType == KEYS.getvalue() ) {
                    this.inputQueues.get((int) msg[1]).addLast(msg);
                } else if (msgType == MERCHANT.getvalue()){
                    this.handleMerchant(msg);
                } else if (msgType == UPGRADE.getvalue()){
                    this.handleUpgrade(msg);
                }
            }

            //HANDLE 0 or 1 INPUT FRAMES FROM EACH PLAYER
            inputQueues.forEach((id,q) -> {
                if (q.notEmpty()){
                    this.handleKeys(q.removeFirst());
                }
            });

            //SEND OUR HEARTBEAT TO THE CLIENT
            this.dispatchHeartbeat();

        } catch(Exception e) {
            System.out.println(">>! Malformed Network Traffic Detected!");
            System.out.println(e);
        }

    }

    /**
     * Officially welcome the client to the game, supplying them with a player ID.
     */
    public void dispatchWelcome(int client_pid) {
        this.outgoingMessages.add(StreamMaker.welcome(client_pid));
        this.outgoingMessages.add(StreamMaker.getClients());
        this.server.relay(StreamMaker.notifyClientUpdate(client_pid), client_pid);
        RRGame.globals.addClient(client_pid);
    }

    /**
     * Communicate keystrokes to client.
     * @param keymask Keystroke Bytemap
     */
    public void dispatchKeys(byte[] keymask, long frame, float x, float y, float mx, float my){
        this.outgoingMessages.add(StreamMaker.keys(pid, frame, keymask, x, y, mx, my));
    }

    public void dispatchKillEnemy(int eid){
       this.outgoingMessages.add(StreamMaker.killEnemy(eid));
    }

    /**
     * Communicate to the client that we are still alive!
     */
    @Override
    public void dispatchHeartbeat() {
        this.outgoingMessages.add(StreamMaker.heartbeat(pid));
    }

    @Override
    public void forward(byte[] packet) {
        this.outgoingMessages.add(packet);
    }

    /**
     * Communicate to client that the game has started.
     */
    public void dispatchStartGame() {
        this.outgoingMessages.add(StreamMaker.startGame());
    }

    public void dispatchUpgrade(int pid, BuyableItem item){
        this.outgoingMessages.add(StreamMaker.upgrade(pid,item));
    }

    /**
     * Communicate to the client that the server is shutting down.
     */
    public void dispatchFarewell(){
        this.outgoingMessages.add(StreamMaker.farewell());
        this.listening = false;
        this.dispose();
    }


    public void dispatchDestroyProjectile(int pid, long number){
        this.outgoingMessages.add(StreamMaker.destroyProjectile(pid,number));
    }

    /**
     * Tell client that 'pid' picked up a key.
     * @param pid
     */
    public void dispatchKeyPickup(int pid, int keyID){
        this.outgoingMessages.add(StreamMaker.pickupKey(pid, keyID));
    }

    public void dispatchCoinDrop(float x, float y, int level){
        this.outgoingMessages.add(StreamMaker.dropCoins(x,y,level));
    }

    public void dispatchKeyDrop(float x, float y){
        this.outgoingMessages.add(StreamMaker.dropKey(x, y));
    }

    @Override
    public void dispatchEnterMerchant(int pid) {
       this.outgoingMessages.add(StreamMaker.merchant(pid,true));
    }

    @Override
    public void dispatchLeaveMerchant(int pid) {
        this.outgoingMessages.add(StreamMaker.merchant(pid,false));
    }

    /**
     * Communicate to the client to create the server's player
     */
    public void dispatchCreatePlayer(Player player){
        this.outgoingMessages.add(StreamMaker.createPlayer(0, (int) player.getX(), (int) player.getY()));
    }

    /**
     * Communicate to the client a random seed to be used for all random activities.
     * @param seed
     */
    public void dispatchSeed(long seed){
        this.outgoingMessages.add(StreamMaker.seed(seed));
    }

    /**
     * Kill the player
     * @param pid
     */
    @Override
    public void dispatchKillPlayer(int pid) {
       this.outgoingMessages.add(StreamMaker.killPlayer(pid));
    }

    @Override
    public void dispatchCommand(String[] cmd) {
       this.outgoingMessages.add(StreamMaker.command(cmd));
    }

    /**
     * Remove the player's entity.
     * @param pid
     */
    public void dispatchDestroyPlayer(int pid){
        this.outgoingMessages.add(StreamMaker.destroyPlayer(pid));
    }

    /**
     * Communicate to the client which player the npc with eid should target.
     * @param eid
     * @param pid
     */
    @Override
    public void dispatchTarget(int eid, int pid) {
       this.outgoingMessages.add(StreamMaker.target(pid,eid));
    }

    public void dispatchSyncHealth(int pid, int hp){
        this.outgoingMessages.add(StreamMaker.syncHealth(pid,hp));
    }

    /**
     * Communicate to the client to destroy an entity.
     */
    public void dispatchDestroyEntity(int eid) {
        this.outgoingMessages.add(StreamMaker.destroyEntity(eid));
    }

    @Override
    public void dispatchDestroyEntity2(int pid, long frame) {
        this.outgoingMessages.add(StreamMaker.destroyEntity2(pid,frame));
    }

    @Override
    public void dispatchDestroyEntity3(int eid, long number) {
       this.outgoingMessages.add(StreamMaker.destroyEntity3(eid,number));
    }

    /**
     * Communicate to the client to create a player.
     * Used to relay information between clients.
     */
    public void dispatchCreatePlayer(byte[] createPlayerPacket, int pid){
        return;
    }

    /* Handlers */

    /**
     * Client Requests to create a player.
     * We need to relay this to the other clients, too.
     * @param packet Player Data
     */
    public void handleCreatePlayer(byte[] packet){
        this.server.relay(packet,(int) packet[1]);
        this.inputQueues.put((int) packet[1],new Queue<byte[]>());
        int new_pid = packet[1];

        byte[] xIntBytes = new byte[4];
        byte[] yIntBytes = new byte[4];
        System.arraycopy(packet,2,xIntBytes,0,4);
        System.arraycopy(packet,6,yIntBytes,0,4);
        int x = StreamMaker.bytesToInt(xIntBytes);
        int y = StreamMaker.bytesToInt(yIntBytes);

        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y, RRGame.PLAYER_SIZE, new_pid);
        RRGame.globals.addPlayer(new_pid,player);
    }

    public void handleUpgrade(byte[] packet){
        int pid = packet[1];
        int upgrade = packet[2];
        Player p = RRGame.globals.players.get(pid);
        if (p == null){
            System.out.println(">>! Null Player!");
            return;
        }
        if (upgrade == BuyableItem.CLOAK.getvalue()){
            p.stats.increaseMoveSpeed(5);
        } else if (upgrade == BuyableItem.DAGGER.getvalue()){
            p.stats.increaseAttackSpeed(1);
        } else if (upgrade == BuyableItem.HEALTH_POTION.getvalue()){
            p.healthPotionsHeld+=1;
        } else if (upgrade == BuyableItem.RING.getvalue()){
            p.stats.increaseHealth(25);
        }
    }

    public void handleMerchant(byte[] packet){
        int pid = packet[1];
        boolean enterOrLeave = false;
        if ((int) packet[2] == 1){
            enterOrLeave = true;
        }
        Player p = RRGame.globals.players.get(pid);
        if (enterOrLeave){
            p.startShopping();
        }else{
            p.stopShopping();
        }
    }


    /**
     * Client Requests to execute keystrokes on a player.
     * @param packet Input Data
     */
    public void handleKeys(byte[] packet){
        this.server.relay(packet,this.client_pid);

        int pid = packet[1];
        Player p = RRGame.globals.players.get(pid);
        if (p == null){
            return;
        }

        byte[] longBytes = new byte[8];
        byte[] xBytes = new byte[4];
        byte[] yBytes = new byte[4];
        byte[] mxBytes = new byte[4];
        byte[] myBytes = new byte[4];

        System.arraycopy(packet,2, longBytes,0,8);
        System.arraycopy(packet,64, xBytes,0,4);
        System.arraycopy(packet,68, yBytes,0,4);
        System.arraycopy(packet,72, mxBytes,0,4);
        System.arraycopy(packet,76, myBytes,0,4);

        long frame = StreamMaker.bytesToLong(longBytes);
        float x = StreamMaker.bytesToFloat(xBytes);
        float y = StreamMaker.bytesToFloat(yBytes);
        float mx = StreamMaker.bytesToFloat(mxBytes);
        float my = StreamMaker.bytesToFloat(myBytes);

        p.mouseLocation.x = mx;
        p.mouseLocation.y = my;

        System.out.println("Got mouse location of x as " + mx + " | y=" + my);

        p.setPosition(x,y);

        if (packet[10] == 1) {
            p.moveUp();
        }
        if (packet[11] == 1) {
            p.moveDown();
        }
        if (packet[12] == 1) {
            p.moveRight();
        }
        if (packet[13] == 1) {
            p.moveLeft();
        }
        if (packet[14] == 1) {
            p.dash();
        }
        if (packet[15] == 1) {
            p.useConsumable(pid,frame);
        }
        if (packet[16] == 1) {
            p.useAbility(pid,frame);
        }
    }

    /**
     * Client Requests to leave the game
     */
    public void handleFarewell(){
        this.server.relay(StreamMaker.destroyPlayer(this.client_pid), this.client_pid);
        Player p = RRGame.globals.players.get(client_pid);
        RRGame.globals.removePlayer(client_pid);
        RRGame.globals.removeClient(client_pid);
        RRGame.globals.deregisterEntity(p);
        this.dispose();
        System.out.println(">>> Client #" + Integer.toString(this.client_pid) + " left the game.");
    }

    /**
     * Client sent a message indicating that they are still alive.
     */
    public void handleHeartBeat(){
        this.server.heartbeat(this.client_pid);
    }

    /**
     * Safely destroy streams and sockets.
     */
    public void dispose(){
        if (this.listening){
            this.dispatchFarewell();
        }
        this.listening = false;
        try {
            out.close();
            in.close();
        } catch (IOException e) {
        }finally {
            socket.dispose();
            incomingMessages.clear();
        }
    }

    /**
     * Are we a server or a client?
     * @return Network type.
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.SERVER;
    }
}