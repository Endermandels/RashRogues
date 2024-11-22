package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
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
    private Socket socket;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    public HashMap<String,Entity> syncedEntities = new HashMap<>();
    private int pid;
    public volatile boolean listening = true;

    public Client() {
        try {
            this.socket = Gdx.net.newClientSocket(Network.PROTOCOL, "localhost", Network.PORT, null);
        }catch(GdxRuntimeException e){
            System.out.println("unable to connect to server !");
            return;
        }
        System.out.println("Connected to server on localhost:" + Integer.toString(Network.PORT));
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        try{
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
                            while (listening) {
                                byte[] msg = new byte[128];
                                int read = in.read(msg,0,128);
                                if (read > 0) {
                                    messages.add(msg);
                                }
                                //DEBUG
                                //for (int i = 0; i < 128; i++){
                                //    System.out.print(msg[i] + "-");
                                //}
                                //System.out.println("|");
                                //System.out.flush();
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
           }else if (msgType == CREATE_PLAYER.getvalue()){
               this.handleCreatePlayer(msg);
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

    public void handleStartGame(){
        System.out.println("Server started the game.");
        RRGame.globals.currentScreen.nextScreen();
    }

    public void handleCreatePlayer(byte[] packet){
        int x = ((packet[2] >> 24) | (packet[3] >> 16) | (packet[4] >> 8) | (packet[5]));
        int y = ((packet[6] >> 24) | (packet[7] >> 16) | (packet[8] >> 8) | (packet[9]));
        new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y, RRGame.PLAYER_SIZE);
    }

    /**
     * We received a farewell message from the server.
     * The connection is closed and we can dipose of our
     * input/output streams, as well as our socket.
     */
    public void handleFarewell(){
        System.out.println("Server connection closed.");
        this.dispose();
    }

   /* Dispatchers */

    /**
     * Tells server about our player.
     */
    public void dispatchCreatePlayer(int x, int y){
       byte[] stream = StreamMaker.createPlayer(this.pid, x, y);
        try {
            this.out.write(stream);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        this.listening = false;
        this.dispose();
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
        } catch (IOException e) {
        }finally {
            socket.dispose();
            messages.clear();
        }
    }

    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }

}