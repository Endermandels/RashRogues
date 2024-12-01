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
        RRGame.globals.addClient(0);

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
            } else if (msgType == NEW_CLIENT.getvalue()){
                this.handleClientJoinNotification(msg);
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
        RRGame.globals.addPlayer(this.pid, player);
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
     * Communicate to server which keys are pressed down.
     *
     * @param keymask Keys pressed.
     */
    public void dispatchKeys(byte[] keymask) {
        this.outgoingMessages.add(StreamMaker.keys(pid, keymask));
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
        System.out.println("invite from " + Integer.toString(this.pid));
        RRGame.globals.addClient(this.pid);
        RRGame.globals.pid = this.pid;
    }

    /**
     * We received info that a new client joined the game.
     */
    public void handleClientJoinNotification(byte[] packet) {
        int client_pid = packet[1];
        RRGame.globals.addClient(client_pid);
    }

    /**
     * We received info that a client left the game.
     */
    public void handleClientLeftNotification(byte[] packet) {
        int client_pid = packet[1];



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
     * We received a message to create a player locally.
     * @param packet Player Data
     */
    public void handleCreatePlayer(byte[] packet) {
        int new_pid = packet[1];
        int x = ((packet[2] >> 24) | (packet[3] >> 16) | (packet[4] >> 8) | (packet[5]));
        int y = ((packet[6] >> 24) | (packet[7] >> 16) | (packet[8] >> 8) | (packet[9]));
        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG), x, y, RRGame.PLAYER_SIZE);
        this.inputQueues.put((int) packet[1],new Queue<byte[]>());
        RRGame.globals.addPlayer(new_pid,player);
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
        Player p = RRGame.globals.players.get((int) packet[1]);
        if (packet[2] == 1){
            p.moveUp();
        }
        if (packet[3] == 1){
            p.moveDown();
        }
        if (packet[4] == 1){
            p.moveRight();
        }
        if (packet[5] == 1){
            p.moveLeft();
        }
        if (packet[6] == 1){
            p.dash();
        }
        if (packet[7] == 1){
            p.useAbility();
        }
        if (packet[8] == 1){
            p.useConsumable();
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

    /**
     * Are we a server or a client?
     * @return Endpoint type.
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }
}