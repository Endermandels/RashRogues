package Networking;

import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Queue;
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
    private Thread listeningThread;
    public ConcurrentLinkedQueue<byte[]> messages = new ConcurrentLinkedQueue<>();
    private int pid = 0; //we are the server. PID 0
    private int client_pid;
    public volatile boolean listening = true;
    public Queue<byte[]> inputQueue = new Queue<byte[]>();

    public ClientListener(Socket socket, int pid) {
        try {
            this.socket = socket;

            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
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
                        while (listening) {
                                byte[] msg = new byte[128];
                                int read = in.read(msg,0,128);
                                if (read > 0) {
                                    messages.add(msg);
                                }
                                // DEBUG
                                //for (int i = 0; i < 128; i++){
                                //  System.out.print(msg[i] + "-");
                                //}
                                //System.out.flush();
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
            if (msgType == UPDATE_PLAYER.getvalue()){
                this.handleUpdatePlayer(msg);
            }else if (msgType == START_GAME.getvalue()){
                continue;
            }else if (msgType == WELCOME.getvalue()){
                continue;
            }else if (msgType == FAREWELL.getvalue()){
                this.handleFarewell();
            }else if (msgType == CREATE_PLAYER.getvalue()){
                this.handleCreatePlayer(msg);
            }else if (msgType == KEYS.getvalue()){
                inputQueue.addLast(msg);
                if (inputQueue.notEmpty()){
                    handleKeys(inputQueue.removeFirst());
                }
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
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Sends a farewell packet to the client, informing them
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
     * Tells server about our player.
     */
    public void dispatchCreatePlayer(int x, int y){
        byte[] stream = StreamMaker.createPlayer(0, x, y);
        try {
            this.out.write(stream);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatchReckonPlayerPosition(){
        Player clientP = RRGame.globals.players.get(this.client_pid);
        byte[] stream = StreamMaker.reckonPlayerPosition(clientP.getX(), clientP.getY());
        try {
            this.out.write(stream);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Handlers */

    public void handleCreatePlayer(byte[] packet){
        int new_pid = packet[1];
        int x = ((packet[2] >> 24) | (packet[3] >> 16) | (packet[4] >> 8) | (packet[5]));
        int y = ((packet[6] >> 24) | (packet[7] >> 16) | (packet[8] >> 8) | (packet[9]));
        Player player = new Player(RRGame.am.get(RRGame.RSC_ROGUE_IMG),x,y, RRGame.PLAYER_SIZE);
        RRGame.globals.players.put(new_pid,player);
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

    public void handleUpdatePlayer(byte [] packet){
        return;
    }

    /**
     * After receiving a farewell packet, we are clear to close our streams,
     * and dispose of our socket.
     */
    public void handleFarewell(){
        this.dispose();
        System.out.println("Client #" + Integer.toString(this.client_pid) + " left the game.");
    }

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
     * Communicate to server which keys are pressed down.
     * @param keymask Keys pressed.
     */
    public void dispatchKeys(byte[] keymask){
        byte[] stream = StreamMaker.keys(pid, keymask);
        try {
            out.write(stream);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Network.EndpointType getType() {
        return Network.EndpointType.SERVER;
    }
}