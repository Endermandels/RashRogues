package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Networking.PacketType.*;

public class Client implements Endpoint {
    private InputStream in;
    private OutputStream out;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    public HashMap<String,Entity> syncedEntities = new HashMap<>();
    private int pid;
    private volatile boolean listening = false;

    public Client() {
        this.socket = Gdx.net.newClientSocket(Network.PROTOCOL, "localhost", Network.PORT, null);
        System.out.println("Connected to server on localhost:" + Integer.toString(Network.PORT));
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        try{
            this.objectOutputStream = new ObjectOutputStream(this.out);
            listen(in);
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private void listen(InputStream in) throws IOException, InterruptedException {
        this.listeningThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            listening = true;
                            while (listening) {
                                byte[] msg = new byte[256];
                                int c = 0;
                                int b;
                                while (((byte)(b = in.read()) != -1) && (c < 256)){
                                    msg[c] = (byte) b;
                                    c++;
                                }
                                messages.add(msg);
                            }
                        } catch (IOException e) {
                            System.out.println("Listening stopped.");
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
        while (!this.messages.isEmpty()){
           byte[] msg = this.messages.poll();
           int msgType = (int) msg[0];
           if (msgType == START_GAME.getvalue()){
               handleStartGame();
           }else if (msgType == WELCOME.getvalue()){
               handleInvite(msg);
           }else if (msgType == FAREWELL.getvalue()){
               this.handleFarewell();
           }
        }
    }

    /* Handlers */

    /**
     * Accept Invite To Server
     */
    public void handleInvite(byte[] packet){
        this.pid = (int) packet[1];
        System.out.println("Server assigned a player ID: " + Integer.toString(this.pid));
    }

    /**
     * Create an object on our client
     */
    public void handleCreate(Packet p){
    }

    /**
     * Update an object on our client
     */
    public void handleUpdate(Packet p){
    }

    /**
     * Destroy an object on our client
     */
    public void handleDestroy(Packet p){
    }

    public void handleStartGame(){
        System.out.println("Server started the game.");
        RRGame.globals.currentScreen.nextScreen();
    }

    /**
     * We received a farewell message from the server.
     * The connection is closed and we can dipose of our
     * input/output streams, as well as our socket.
     */
    public void handleFarewell(){
        System.out.println("Server connection closed expectedly.");
        this.listening = false;
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.dispose();
    }

   /* Dispatchers */

    /**
     * Clients can't start games so this method is just
     * fufilling the interface.
     */
    public void dispatchStartGame() {
        return;
    }

    /**
     * Requests a new player from the server.
     * @param player
     */
    public void dispatchCreate(Player player){
    }

    /**
     * Requests a new entity from the server.
     */
    public void dispatchCreate(Entity entity) {
        return;
    }

    public void dispatchUpdate(Entity entity){}
    public void dispatchUpdate(Player entity){}

    /**
     * Sends a farewell packet to the server, informing the server
     * that we are leaving. Closes the input stream.
     */
    public void dispatchFarewell(){
        byte[] stream = StreamMaker.farewell();
        try {
            out.write(stream);
            in.close();
            System.out.println("dispatch sent to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely informs the server that we are disconnecting,
     * and then closes all streams/sockets.
     */
    public void dispose(){
        this.listening = false;
        this.dispatchFarewell();
        socket.dispose();
    }

    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }

}