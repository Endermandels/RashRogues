package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.BuyableItem;
import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;
import io.github.RashRogues.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static Networking.PacketType.*;

public class Client implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Thread listeningThread;
    private Thread speakingThread;
    private int pid;

    private Queue<byte[]> deferQueue = new Queue<>();
    private HashMap<Integer,Integer> tgtPktDeferCount = new HashMap<>();
    private int tgtPktDeferMax = 3;

    public ConcurrentLinkedQueue<byte[]> incomingMessages = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<byte[]> outgoingMessages = new ConcurrentLinkedQueue<>();
    public volatile boolean listening = true;
    public volatile boolean speaking = true;
    public LinkedHashMap<Integer, Queue<byte[]>> inputQueues;
    public HashMap<String, Entity> syncedEntities = new HashMap<>();
    public int lastHeartbeat = 0;

    /**
     * Client-Server connection, over which gameplay data can be communicated.
     */
    public Client() throws GdxRuntimeException {
        this.inputQueues = new LinkedHashMap<>();
        this.socket = Gdx.net.newClientSocket(Network.PROTOCOL, Network.ip, Network.PORT, null);
        System.out.println(">>> Connected to server on:" + this.socket.getRemoteAddress());

        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();

        try {
            listen(in);
            speak(out);
        } catch (IOException | InterruptedException e) {
            System.out.println(">>! Connection with server closed.");
        }
    }

    /**
     * Listen for and handle incomingmessages from the server.
     * Creates a listening thread seperate from the game thread.
     * Interacts with gamestate via incomingMessages queue.
     *
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
                                int read = in.read(msg, 0, 128);
                                if (read > 0) {
                                    incomingMessages.add(msg);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Listening thread stopped.");
                            System.out.flush();
                            listening = false;
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
     *
     * @param out
     * @throws IOException
     * @throws InterruptedException
     */
    private void speak(OutputStream out) throws IOException, InterruptedException {
        this.speakingThread = new Thread(
                new Runnable() {
                    public void run() {
                        while (speaking) {
                            while (!outgoingMessages.isEmpty()) {
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
     * <p>
     * Each message type has a specific handler method.
     * Most Messages will be executed as soon as they are encountered.
     * Input is an exception to this, as it needs to be buffered make
     * sure we don't cluster multiple player inputs on one frame.
     */
    public void processMessages() {

        // Check deferred packets from last frame.
        while (!this.deferQueue.isEmpty()){
            this.incomingMessages.add(deferQueue.removeFirst());
        }

        //READ MESSAGES FROM LISTENER THREAD
        while (!this.incomingMessages.isEmpty()) {
            byte[] msg = this.incomingMessages.poll();
            int msgType = (int) msg[0];

            if (msgType == HEARTBEAT.getvalue()) {
                this.handleHeartbeat();
            } else if (msgType == START_GAME.getvalue()) {
                handleStartGame();
            } else if (msgType == WELCOME.getvalue()) {
                handleInvite(msg);
            } else if (msgType == FAREWELL.getvalue()) {
                this.handleFarewell();
            } else if (msgType == CREATE_PLAYER.getvalue()) {
                this.handleCreatePlayer(msg);
            } else if (msgType == KEYS.getvalue()) {
                this.inputQueues.get((int) msg[1]).addLast(msg);
            } else if (msgType == UPDATE_PLAYER_POSITION.getvalue()) {
                this.handleUpdatePlayerPosition(msg);
            } else if (msgType == CLIENT_SHARE.getvalue()){
                this.handleClientShare(msg);
            } else if (msgType == CLIENT_UPDATE.getvalue()){
                this.handleClientUpdate(msg);
            } else if (msgType == DESTROY_PLAYER.getvalue()){
                this.handleDestroyPlayer(msg);
            } else if (msgType == DESTROY.getvalue()){
                this.handleDestroyEntity(msg);
            } else if (msgType == DESTROY2.getvalue()){
                this.handleDestroyEntity2(msg);
            } else if (msgType == RANDOM_SEED.getvalue()){
                this.handleSeed(msg);
            } else if (msgType == DESTROY3.getvalue()){
                this.handleDestroyProjectile(msg);
            } else if (msgType == KILL_PLAYER.getvalue()){
                this.handleKillPlayer(msg);
            } else if (msgType == COMMAND.getvalue()){
                this.handleCommand(msg);
            } else if (msgType == PICKUP_KEY.getvalue()){
                this.handlePickupKey(msg);
            } else if (msgType == MERCHANT.getvalue()){
                this.handleMerchant(msg);
            } else if (msgType == UPGRADE.getvalue()){
                this.handleUpgrade(msg);
            } else if (msgType == SET_TARGET.getvalue()){
                this.handleSetTarget(msg);
            } else if (msgType == DROP_KEY.getvalue()){
                this.handleDropKey(msg);
            }
        }

        //HANDLE 0 or 1 INPUT FRAMES FROM EACH PLAYER
        inputQueues.forEach((id, q) -> {
            if (q.notEmpty()) {
                this.handleKeys(q.removeFirst());
            }
        });

        //SEND OUR HEARTBEAT TO THE SERVER
        this.dispatchHeartbeat();

        if (this.lastHeartbeat > Network.HEARTBEAT_THRESHOLD) {
            System.out.println(">>! Warning! Server went dark!");
        }

        this.lastHeartbeat += 1;
    }


    /* Dispatchers */

    /**
     * Tells server about our player.
     */
    public void dispatchCreatePlayer(Player player) {
        this.outgoingMessages.add(StreamMaker.createPlayer(this.pid, (int) player.getX(), (int) player.getY()));
    }

    @Override
    public void dispatchDestroyPlayer(int pid) {
       return;
    }

    /**
     * Clients can't start games so this method is just
     * fufilling the interface.
     */
    public void dispatchStartGame() {
        return;
    }

    /**
     * Sends a farewell packet to the server, informing the server
     * that we are leaving. Closes the input stream.
     */
    public void dispatchFarewell() {
        this.outgoingMessages.add(StreamMaker.farewell());
        this.listening = false;
        this.dispose();
    }

    /**
     * Sends a seed for random events to the server.
     * Not implemented as server is the only one who
     * gets to create seeds.
     * @param seed
     */
    public void dispatchSeed(long seed){
        return;
    }

    @Override
    public void dispatchKillPlayer(int pid) {
       return;
    }

    @Override
    public void dispatchTarget(int eid, int pid) {
       return;
    }

    @Override
    public void dispatchCommand(String[] cmd) {
       return;
    }

    @Override
    public void dispatchKeyPickup(int pid, int keyID) {
       return;
    }

    @Override
    public void dispatchKeyDrop(float x, float y) {
        return;
    }

    @Override
    public void dispatchEnterMerchant(int pid) {
        this.outgoingMessages.add(StreamMaker.merchant(pid,true));
    }

    @Override
    public void dispatchLeaveMerchant(int pid) {
        this.outgoingMessages.add(StreamMaker.merchant(pid,false));
    }

    @Override
    public void dispatchUpgrade(int pid, BuyableItem item) {
       this.outgoingMessages.add(StreamMaker.upgrade(pid,item));
    }


    /**
     * Communicate to server which keys are pressed down.
     *
     * @param keymask Keys pressed.
     */
    public void dispatchKeys(byte[] keymask, long frame, float x, float y) {
        this.outgoingMessages.add(StreamMaker.keys(pid, frame, keymask, x, y));
    }

    /**
     * Communicate to the server that we are still alive.
     */
    public void dispatchHeartbeat() {
        this.outgoingMessages.add(StreamMaker.heartbeat(pid));
    }


    /* Handlers */

    /**
     * We received an OK to join the game.
     */
    public void handleInvite(byte[] packet) {
        this.pid = (int) packet[1];
        RRGame.globals.addClient(this.pid);
        RRGame.globals.setPID(this.pid);
    }

    /**
     * Server told us to pickup a specific key from the world.
     * @param packet
     */
    public void handlePickupKey(byte[] packet){
        int playerWhoPickedUpKey = packet[1];
        byte[] keyIDBytes = new byte[4];
        System.arraycopy(packet,2,keyIDBytes,0,4);
        int keyID = StreamMaker.bytesToInt(keyIDBytes);

        Entity key = RRGame.globals.getKey(keyID);
        Player p = RRGame.globals.players.get(playerWhoPickedUpKey);

        if (key == null || p == null){
            return;
        }

        RRGame.globals.deregisterEntity(key);
        p.setHoldingKey(true);
    }

    public void handleDropKey(byte[] packet){
        byte[] xBytes = new byte[4];
        byte[] yBytes = new byte[4];

        xBytes[0] = packet[1];
        xBytes[1] = packet[2];
        xBytes[2] = packet[3];
        xBytes[3] = packet[4];

        yBytes[0] = packet[5];
        yBytes[1] = packet[6];
        yBytes[2] = packet[7];
        yBytes[3] = packet[8];

        float x = StreamMaker.bytesToFloat(xBytes);
        float y = StreamMaker.bytesToFloat(yBytes);
        new Key(x,y);
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
     * We received the instruction to kill a player.
     * @param packet
     */
    public void handleKillPlayer(byte[] packet){
        int pidToKill = packet[1];
        Player player = RRGame.globals.players.get(pidToKill);
        if (player != null){
            player.stats.kill();
        }else{
            System.out.println("Warning! Player doesn't exist!");
        }
    }

    /**
     * We received the instruction to destroy a player entity.
     * @param packet
     */
    public void handleDestroyPlayer(byte[] packet){
        int pid = packet[1];
        Player p = RRGame.globals.players.get(pid);
        RRGame.globals.removePlayer(pid);
        RRGame.globals.deregisterEntity(p);
    }

    public void handleSetTarget(byte[] packet){
        int pid = packet[1];
        byte[] eidBytes = new byte[4];
        System.arraycopy(packet,2,eidBytes,0,4);
        int eid = StreamMaker.bytesToInt(eidBytes);
        Entity e = RRGame.globals.deterministicReplicatedEntities.get(eid);
        Player p = RRGame.globals.players.get(pid);

        if (e != null && e instanceof Enemy){
            ((Enemy) e).setTarget(p);

        //try again next frame.
        } else {
            if (tgtPktDeferCount.containsKey(eid) == false) {
                tgtPktDeferCount.put(eid, 0);
            }

            if (tgtPktDeferCount.get(eid) >= tgtPktDeferMax){
                return;
            }

            tgtPktDeferCount.put(eid, tgtPktDeferCount.get(eid) + 1);
            this.deferQueue.addLast(packet);
        }
    }

    /**
     * We receieved a command from the server. Enact it.
     * @param packet
     */
    public void handleCommand(byte[] packet){
        ArrayList<String> commands = new ArrayList<>();
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 1; i < packet.length; i++){
            //encountered cmdlet delimiter - add cmdlet to list and clear current cmdlet.
            if (packet[i] == '#'){
                commands.add(strBuilder.toString());
                strBuilder = new StringBuilder();
            //encountered a char - add to current cmdlet
            }else{
                strBuilder.append((char) packet[i]);
            }
        }

        //convert arraylist to array.
        String[] finalCommand = new String[commands.size()];
        for (int i = 0; i < commands.size(); i++){
            finalCommand[i] = commands.get(i);
        }

        RRGame.globals.executeCommandOnCurrentScreen(finalCommand);
    }

    /**
     * The server shared a list of all clients with us.
     */
    public void handleClientShare(byte[] packet) {
        int clients = packet[1];
        for (int i = 0; i < clients; i++){
            RRGame.globals.addClient(packet[2+i]);
        }
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

    /**
     * We received info that a new client joined the game.
     */
    public void handleClientUpdate(byte[] packet){
        int client = packet[1];
        RRGame.globals.addClient(client);
    }

    /**
     * We received a message from the server indicating that it is still alive.
     */
    public void handleHeartbeat(){
        this.lastHeartbeat = 0;
    }

    /**
     * We recieved a message to start the game.
     */
    public void handleStartGame() {
        System.out.println("Server started the game.");
        RRGame.globals.currentScreen.nextScreen();
    }

    /**
     * We received a seed from the server to base
     * all random events off.
     * @param packet
     */
    public void handleSeed(byte[] packet){
        byte[] seedBytes = new byte[8];
        System.arraycopy(packet,1, seedBytes, 0, 8);
        long seed = StreamMaker.bytesToLong(seedBytes);
        RRGame.globals.setRandomSeed(seed);
    }

    /**
     * We received a message to create a player locally.
     * @param packet Player Data
     */
    public void handleCreatePlayer(byte[] packet) {
        int new_pid = packet[1];

        byte[] xIntBytes = new byte[4];
        byte[] yIntBytes = new byte[4];
        System.arraycopy(packet,2,xIntBytes,0,4);
        System.arraycopy(packet,6,yIntBytes,0,4);
        int x = StreamMaker.bytesToInt(xIntBytes);
        int y = StreamMaker.bytesToInt(yIntBytes);

        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG), x, y, RRGame.PLAYER_SIZE, new_pid);
        this.inputQueues.put((int) packet[1],new Queue<byte[]>());
        RRGame.globals.addPlayer(new_pid,player);
    }

    public void handleDestroyProjectile(byte[] packet){
        int pid = packet[1];
        byte[] numberBytes = new byte[8];
        System.arraycopy(packet,2,numberBytes,0,8);
        long number = StreamMaker.bytesToLong(numberBytes);
        RRGame.globals.deregisterEntity(RRGame.globals.findNondeterministicProjectile(pid,number));
    }

    public void handleDestroyEntity(byte[] packet){
        byte[] eidBytes = new byte[4];
        System.arraycopy(packet,1,eidBytes,0,4);
        int eid = StreamMaker.bytesToInt(eidBytes);
        RRGame.globals.deregisterEntity(RRGame.globals.getReplicatedEntity(eid));
    }

    /**
     * Server instructs us to destroy entity that belongs to a certain pid, and was created on a certain frame.
     * @param packet
     */

    public void handleDestroyEntity2(byte[] packet){
        int pid = (int) packet[1];
        byte[] longBytes = new byte[8];
        System.arraycopy(packet,2, longBytes,0,8);
        long frame = StreamMaker.bytesToLong(longBytes);
        Entity e = RRGame.globals.findNondeterministicEntity(pid,frame);
        RRGame.globals.deregisterEntity(e);
    }

    public void dispatchDestroyProjectile(int pid, long number){
        return;
    }

    /**
     * We received a farewell message from the server.
     * The connection is closed and we can dipose of our
     * input/output streams, as well as our socket.
     */
    public void handleFarewell() {
        System.out.println("Server connection closed.");
        this.dispose();
    }

    public void handleUpdatePlayerPosition(byte[] packet) {
        int pid = packet[1];
        Player p = RRGame.globals.players.get(pid);
        if (p == null){
            return;
        }
        float x = ByteBuffer.wrap(new byte[]{packet[2], packet[3], packet[4], packet[5]}).getFloat();
        float y = ByteBuffer.wrap(new byte[]{packet[6], packet[7], packet[8], packet[9]}).getFloat();
        p.setPosition(x,y);
    }

    public void handleKeys(byte[] packet){
        int pid = (int) packet[1];
        Player p = RRGame.globals.players.get(pid);

        if (p == null){
            return;
        }

        byte[] longBytes = new byte[8];
        byte[] xBytes = new byte[4];
        byte[] yBytes = new byte[4];
        System.arraycopy(packet,2, longBytes,0,8);
        System.arraycopy(packet,64, xBytes,0,4);
        System.arraycopy(packet,68, yBytes,0,4);
        long frame = StreamMaker.bytesToLong(longBytes);
        float x = StreamMaker.bytesToFloat(xBytes);
        float y = StreamMaker.bytesToFloat(yBytes);

        p.setPosition(x,y);

        if (packet[10] == 1){
            p.moveUp();
        }
        if (packet[11] == 1){
            p.moveDown();
        }
        if (packet[12] == 1){
            p.moveRight();
        }
        if (packet[13] == 1){
            p.moveLeft();
        }
        if (packet[14] == 1){
            p.dash();
        }

        if (packet[15] == 1){
            p.useConsumable(pid, frame);
        }

        if (packet[16] == 1){
            p.useAbility(pid, frame);
        }
    }

    @Override
    public void forward(byte[] packet) {
        return;
    }

    /**
     * Safely informs the server that we are disconnecting,
     * and then closes all streams/sockets.
     */
    public void dispose(){
        if (this.listening){
            this.dispatchFarewell();
        }
        this.listening = false;
        this.speaking  = false;
        try {
            this.in.close();
            this.out.close();
        } catch (IOException ignored) {
        }finally {
            socket.dispose();
            this.incomingMessages.clear();
        }
    }

    @Override
    public void dispatchDestroyEntity(int eid) {
        return;
    }

    @Override
    public void dispatchDestroyEntity2(int pid, long frame) {
       return;
    }

    @Override
    public void dispatchDestroyEntity3(int eid, long number) {
       return;
    }

    /**
     * Are we a server or a client?
     * @return Endpoint type.
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }
}