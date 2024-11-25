package Networking;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Networking.PacketType.*;

/**
 * Each ClientListener is dedicated to speaking/listening to a remote client.
 * Most methods within the ClientListener are meant to be called directly by the Server class.
 */
public class ClientListener implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Server server;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    public int pid = 0; //we are the server. PID 0
    public int client_pid;
    public volatile boolean listening = true;
    public LinkedHashMap<Integer, Queue<byte[]>> inputQueues;

    /**
     * Dedicated
     * @param socket Active socket to communicate on.
     * @param pid Player ID to associate with this ClientListener
     */
    public ClientListener(Server server, Socket socket, int pid) {
        this.server = server;
        this.socket     = socket;
        this.in         = socket.getInputStream();
        this.out        = socket.getOutputStream();
        this.client_pid = pid;
        this.inputQueues = new LinkedHashMap<>();

        try {
            this.dispatchWelcome(this.client_pid);
            this.listen(in);
        } catch (IOException | InterruptedException e) {
            System.out.println(">>! Connection with client #" + Integer.toString(this.client_pid) + " closed.");
        }
    }

    /**
     * Listen for and handle messages from client.
     * listen() determines the lifetime of the connection; when this function concludes the socket/streams will close.
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
                                    messages.add(msg);
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
     * Process Messages from the client.

     * Each message type has a specific handler method.
     * Most Messages will be executed as soon as they are encountered.
     * Input is an exception to this, as it needs to be buffered to
     * run only once a frame.
     */
    public void processMessages() {
        try {
            while ( !this.messages.isEmpty() ) {
                byte[] msg = this.messages.poll();
                int msgType = (int) msg[0];

                //FAREWELL
                if ( msgType == FAREWELL.getvalue() ) {
                    this.handleFarewell();
                }

                //CREATE PLAYER
                else if ( msgType == CREATE_PLAYER.getvalue() ) {
                    this.handleCreatePlayer(msg);
                }

                //KEYSTROKES
                else if ( msgType == KEYS.getvalue() ) {
                    this.inputQueues.get((int) msg[1]).addLast(msg);
                }
            }

            /*
            We can only handle one input  per frame, otherwise shit gets messy.
            if we didn't queue these we'd be running multiple inputs in a single frame on the server,
            which is not reflective of how they were executed on the client.

            We also need a seperate queue for each player, hence the linked hash map of queues.
            */
            inputQueues.forEach((id,q) -> {
                if (q.notEmpty()){
                    this.handleKeys(q.removeFirst());
                }
            });

        } catch(Exception e) {
            System.out.println(">>! Malformed Network Traffic Detected!");
        }

    }

    /**
     * Officially welcome the client to the game, supplying them with a player ID.
     */
    public void dispatchWelcome(int pid) {
        try {
            byte[] stream = StreamMaker.welcome(pid);
            out.write(stream);
            out.flush();
        } catch (IOException e) {
            System.out.println(">>! Unable to communicate with client.");
        }
    }

    /**
     * Communicate keystrokes to client.
     * @param keymask Keystroke Bytemap
     */
    public void dispatchKeys(byte[] keymask){
        byte[] stream = StreamMaker.keys(pid, keymask);
        try {
            out.write(stream);
            out.flush();
        } catch (IOException e) {
            System.out.println(">>! Unable to communicate with client.");
        }
    }

    @Override
    public void forward(byte[] packet) {
        try {
            out.write(packet);
            out.flush();
        } catch (IOException e){
            System.out.println(">>! Unable to communicate with client.");
        }
    }

    /**
     * Communicate to client that the game has started.
     */
    public void dispatchStartGame() {
        try{
            byte[] stream = StreamMaker.startGame();
            out.write(stream);
            out.flush();
        } catch (IOException e){
            System.out.println(">>! Unable to communicate with client.");
        }
    }

    /**
     * Communicate to the client that the server is shutting down.
     */
    public void dispatchFarewell(){
        byte[] stream = StreamMaker.farewell();
        try {
            out.write(stream);
            out.flush();
        } catch (IOException e) {
            System.out.println(">>! Unable to communicate with client.");
        }
        this.listening = false;
        this.dispose();
    }

    /**
     * Communicate to the client to create the server's player
     */
    public void dispatchCreatePlayer(Player player){
        RRGame.globals.players.put(this.pid,player);
        System.out.println("SERVER PID: " + Integer.toString(this.pid));
        byte[] stream = StreamMaker.createPlayer(0, (int) player.getX(), (int) player.getY());
        try {
            this.out.write(stream);
            this.out.flush();
        } catch (IOException e) {
            System.out.println(">>! Unable to communicate with client.");
        }
    }

    /**
     * Communicate to the client to create a player.
     * Used to relay information between clients.
     */
    public void dispatchCreatePlayer(byte[] createPlayerPacket, int pid){
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
        int x = ((packet[2] >> 24) | (packet[3] >> 16) | (packet[4] >> 8) | (packet[5]));
        int y = ((packet[6] >> 24) | (packet[7] >> 16) | (packet[8] >> 8) | (packet[9]));
        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y, RRGame.PLAYER_SIZE);
        RRGame.globals.players.put(new_pid,player);
    }

    /**
     * Client Requests to execute keystrokes on a player.
     * @param packet Input Data
     */
    public void handleKeys(byte[] packet){
        this.server.relay(packet,this.client_pid);
        Player p = RRGame.globals.players.get((int) packet[1]);
        if (packet[2] == 1) {
            p.moveUp();
        }
        if (packet[3] == 1) {
            p.moveDown();
        }
        if (packet[4] == 1) {
            p.moveRight();
        }
        if (packet[5] == 1) {
            p.moveLeft();
        }
        if (packet[6] == 1) {
            p.dash();
        }
        if (packet[7] == 1) {
            p.useAbility();
        }
        if (packet[8] == 1) {
            p.useConsumable();
        }
    }

    /**
     * Client Requests to leave the game
     */
    public void handleFarewell(){
        this.dispose();
        System.out.println(">>> Client #" + Integer.toString(this.client_pid) + " left the game.");
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
            messages.clear();
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