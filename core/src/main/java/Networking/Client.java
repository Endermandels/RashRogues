package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.Entity;
import io.github.RashRogues.PlayScreen;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.nio.ByteBuffer;
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


    /**
     * Communicate to server which keys are pressed down.
     *
     * @param keymask Keys pressed.
     */
    public void dispatchKeys(byte[] keymask, long frame) {
        this.outgoingMessages.add(StreamMaker.keys(pid, frame, keymask));
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
     * We received the instruction to kill a player.
     * @param packet
     */
    public void handleKillPlayer(byte[] packet){
        int pidToKill = packet[1];
        Entity playerEntity = (Entity) RRGame.globals.players.get(pidToKill);
        RRGame.globals.deregisterEntity(playerEntity);
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

    public void handleDestroyPlayer(byte[] packet){
        int pid = packet[1];
        Player p = RRGame.globals.players.get(pid);
        RRGame.globals.removePlayer(pid);
        RRGame.globals.removeClient(pid);
        RRGame.globals.deregisterEntity(p);
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

    public void handleDestroyEntity2(byte[] packet){
        int pid = (int) packet[1];

        byte[] longBytes = new byte[8];
        System.arraycopy(packet,2, longBytes,0,8);
        long frame = StreamMaker.bytesToLong(longBytes);

        System.out.println("Server says to destroy an entity. PID: " + Integer.toString(pid) + " FRAME ID: " + Long.toString(frame));

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
        System.out.println("SERVER TOLD US TO UPDATE THE PLAYER WITH PID: " + Integer.toString(pid));
        Player p = RRGame.globals.players.get(pid);
        System.out.println("Player is: ");
        System.out.println(p);
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
        System.arraycopy(packet,2, longBytes,0,8);
        long frame = StreamMaker.bytesToLong(longBytes);

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

        if (packet[17] == 1){
            p.attack(pid, frame);
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

    /**
     * Are we a server or a client?
     * @return Endpoint type.
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }
}