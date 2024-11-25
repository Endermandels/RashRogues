package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Networking.PacketType.*;

public class Client implements Endpoint {
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    public HashMap<String, Entity> syncedEntities = new HashMap<>();
    private int pid;
    public volatile boolean listening = true;
    public LinkedHashMap<Integer, Queue<byte[]>> inputQueues;

    public Client() {
        this.inputQueues = new LinkedHashMap<>();
        try {
            this.socket = Gdx.net.newClientSocket(Network.PROTOCOL, "localhost", Network.PORT, null);
        } catch (GdxRuntimeException e) {
            System.out.println(">>! Unable to connect to server.");
            return;
        }
        System.out.println(">>> Connected to server on localhost:" + this.socket.getRemoteAddress() + Integer.toString(Network.PORT));
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();

        try {
            listen(in);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void listen(InputStream in) throws IOException, InterruptedException {
        this.listeningThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            while (listening) {
                                byte[] msg = new byte[128];
                                int read = in.read(msg, 0, 128);
                                if (read > 0) {
                                    messages.add(msg);
                                }
                            }
                        } catch (IOException e) {
                            System.out.flush();
                        }
                    }
                }
        );
        this.listeningThread.start();
    }

    /**
     * Process all queued messages that are available.
     */
    public void processMessages() {
        while (!this.messages.isEmpty()) {
            byte[] msg = this.messages.poll();
            int msgType = (int) msg[0];
            if (msgType == START_GAME.getvalue()) {
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
            }
        }

        inputQueues.forEach((id,q) -> {
            if (q.notEmpty()){
                this.handleKeys(q.removeFirst());
            }
        });

    }

    /* Handlers */

    /**
     * Accept Invite To Server
     */
    public void handleInvite(byte[] packet) {
        this.pid = (int) packet[1];
    }

    public void handleStartGame() {
        System.out.println("Server started the game.");
        RRGame.globals.currentScreen.nextScreen();
    }

    public void handleCreatePlayer(byte[] packet) {
        int new_pid = packet[1];
        int x = ((packet[2] >> 24) | (packet[3] >> 16) | (packet[4] >> 8) | (packet[5]));
        int y = ((packet[6] >> 24) | (packet[7] >> 16) | (packet[8] >> 8) | (packet[9]));
        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG), x, y, RRGame.PLAYER_SIZE);
        this.inputQueues.put((int) packet[1],new Queue<byte[]>());
        RRGame.globals.players.put(new_pid, player);
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

   /* Dispatchers */

    /**
     * Tells server about our player.
     */
    public void dispatchCreatePlayer(Player player){
       RRGame.globals.players.put(this.pid,player);
       byte[] stream = StreamMaker.createPlayer(this.pid, (int) player.getX(),(int) player.getY());
        try {
            this.out.write(stream);
            this.out.flush();
        } catch (IOException e) {
            System.out.println(">>! Unable to communicate with client.");
        }
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
     * Communicate to server which keys are pressed down.
     * @param keymask Keys pressed.
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
        try {
            this.in.close();
            this.out.close();
        } catch (IOException ignored) {
        }finally {
            socket.dispose();
            messages.clear();
        }
    }

    /**
     * What type of endpoint are we?
     * @return Endpoint type.
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }

}