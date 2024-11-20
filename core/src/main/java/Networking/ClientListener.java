package Networking;

import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Networking.PacketType.*;

public class ClientListener implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    private int pid = 0; //we are the server. PID 0
    private int client_pid;
    private volatile boolean listening;

    public ClientListener(Socket socket, int pid) {
        try {
            this.socket = socket;

            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();

            objectInputStream = null;
            objectOutputStream = new ObjectOutputStream(out);

            this.client_pid = pid;
            this.dispatchWelcome(this.client_pid);
            this.listen(in);
        } catch (IOException | InterruptedException e) {
            System.out.println("Catastrophic communication error! Exiting!");
            e.printStackTrace();
            System.exit(1);
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
                        listening = true;
                        while (listening) {
                            byte[] msg = new byte[256];
                            int c = 0;
                            int b;
                            System.out.println("BYTES:");
                            while (((byte)(b = in.read()) != '*') && (c < 256)){
                                System.out.println(b);
                                msg[c] = (byte) b;
                                c++;
                            }
                            System.out.println("ENDBYTES");
                            System.out.flush();
                            messages.add(msg);
                        }
                    } catch (IOException ex) {
                        System.out.println("Listening to client #" + Integer.toString(client_pid) + " stopped.");
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
    public void processMessages(){
        while (!this.messages.isEmpty()){
            byte[] msg = this.messages.poll();
            int msgType = (int) msg[0];
            System.out.println(msg[0]);
            if (msgType == START_GAME.getvalue()){
                continue;
            }else if (msgType == WELCOME.getvalue()){
                continue;
            }else if (msgType == FAREWELL.getvalue()){
                this.handleFarewell();
            }
        }
    }

    /* Dispatchers */
    /**
     * Officially welcome the client to the game, supplying them with a player ID.
     */
    public void dispatchWelcome(int pid) {
        try {
            byte[] stream = StreamMaker.welcome(pid);
            out.write(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatchCreate(Entity entity){
        return;
    }

    public void dispatchCreate(Player player){
        return;
    }

    public void dispatchStartGame() {
        try{
            byte[] stream = StreamMaker.startGame();
            out.write(stream);
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void dispatchUpdate(Entity entity){
        return;
    }

    public void dispatchUpdate(Player player){
        return;
    }

    /**
     * Sends a farewell packet to the client, informing them
     * that we are leaving. Closes the input stream.
     */
    public void dispatchFarewell(){
        byte[] stream = StreamMaker.farewell();
        try {
            out.write(stream);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Handlers */
    /**
     * Create an object on our client
     */
    public void handleCreate(Packet p){
//        PacketCreate create = (PacketCreate) p;
        //register up!
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

    /**
     * After receiving a farewell packet, we are clear to close our streams,
     * and dispose of our socket.
     */
    public void handleFarewell(){
        this.listening = false;
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.dispose();
        System.out.println("Connection to client " + Integer.toString(this.client_pid) + " closed.");
    }

    public void dispose(){
        this.listening = false;
        dispatchFarewell();
        socket.dispose();
    }

    public Network.EndpointType getType() {
        return Network.EndpointType.SERVER;
    }
}