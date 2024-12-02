package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The server class is the central control hub for multiplayer games.
 * Use the host() method to start listening for new connections.
 * The server tracks all connections, but does not directly handle any IO between server & client beyond accepting
 * the connection. Those are handled between the ClientListener class (reads and writes to a single client on a
 * dedicated socket) and the Client class (reads and writes to the server on a dedicated socket).
 *
 * For network settings, such as port & protocol configuration, see the Network class.
 */
public class Server implements Endpoint{
    private List<ClientListener> clients;       // data connections to connected clients.
    private Queue<Integer> cleanupQueue;        // data connections to stop.
    private ServerSocket primarySocket;         // primary connection where we listen for new clients.
    private Thread primarySocketThread;         // thread on which we listen to primary connection for new clients
    private volatile boolean listening;         // Are we listening for new connections?

    private LinkedHashMap<Integer,Integer> heartbeatStatus; //track how many frames have passed since last heartbeat

    /**
     * Begin hosting a server.
     * Accepted clients will break out into individual data connections on a separate thread.
     */
    public void host(){
        Server server = this; //add server to this block scope, so we can use it inside the thread.
        primarySocket = Gdx.net.newServerSocket(Network.PROTOCOL,"localhost",Network.PORT,null);
        clients = Collections.synchronizedList(new ArrayList<ClientListener>());
        cleanupQueue = new Queue<>();
        heartbeatStatus = new LinkedHashMap();
        System.out.println(">>> Server listening on 127.0.0.1:" + Integer.toString(Network.PORT));
        RRGame.globals.addClient(0);
        RRGame.globals.pid = 0;

        primarySocketThread = new Thread(
            new Runnable() {
                public void run(){
                    listening = true;
                    while (listening){
                        try{
                            Socket client = primarySocket.accept(null);
                            clients.add(new ClientListener(server, client,clients.size()+1));
                            System.out.println(">>> " + client.getRemoteAddress().substring(1) + " Connected.");
                            System.out.flush();

                            if (clients.size() == Network.MAX_CLIENTS){
                                System.out.println(">>> Game full. No longer accepting clients.");
                                System.out.flush();
                                listening = false;
                                break;
                            }
                        }catch(GdxRuntimeException e){
                            System.out.println(">>! Server stopped.");
                        }
                    }
                }
            }
        );
        primarySocketThread.start();
    }

    /**
     * Instructs all connections to send farwell messages and close.
     * Effectively ends the multiplayer game.
     */
    public void dispatchFarewell(){
        for (ClientListener c : clients){
            c.dispatchFarewell();
        }
        clients.clear();
    }

    @Override
    public void processMessages() {

        //process messages from existing connections.
        for (int i = 0; i < clients.size(); i++){
            if (clients.get(i).listening) {
                clients.get(i).processMessages();
            }else{
                cleanupQueue.addLast(i);
            }
        }

        //Cleanup closed connections.
        while (!cleanupQueue.isEmpty()){
            int toRemove = cleanupQueue.removeFirst();
            clients.remove(toRemove);
        }

        //Read Heartbeats
        this.heartbeatStatus.forEach((client, frames) -> {
            if (frames > Network.HEARTBEAT_THRESHOLD){
                System.out.println(">>! Client #" + Integer.toString(client) + " has gone silent!");
            }
            this.heartbeatStatus.put(client,frames+1);
        });
    }

    @Override
    public void dispatchStartGame() {
        System.out.println(">>> Starting game.");
        for (ClientListener c : clients){
            c.dispatchStartGame();
        }

    public void heartbeat(int clientPID){
        this.heartbeatStatus.put(clientPID,0);
    }

    @Override
    public void dispatchCreatePlayer(Player player) {
        for (ClientListener c : clients){
            c.dispatchCreatePlayer(player);
        }
    }

    public void relay(byte[] packet, int notMe){
        for (ClientListener c : clients){
            if (c.client_pid != notMe){
                c.forward(packet);
            }
        }
    }

    public void forward(byte[] packet, int notMe){
        return;
    }

    @Override
    public void dispose() {
        this.listening = false;
        this.primarySocket.dispose();
        this.dispatchFarewell();
    }

    @Override
    public void dispatchKeys(byte[] keymask) {
        for (ClientListener c : clients){
            c.dispatchKeys(keymask);
        }
    }

    @Override
    public void dispatchHeartbeat() {
        return;
    }

    @Override
    public void forward(byte[] packet) {
        return;
    }
}
