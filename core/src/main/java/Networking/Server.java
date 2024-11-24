package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Queue;
import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<ClientListener> clients;       // 1. data connections to connected clients.
    private Queue<Integer> cleanupQueue; // 1.1 data connections to stop.
    private ServerSocket primarySocket;   // 2. primary connection where we listen for new clients.
    private Thread primarySocketThread;   // 3. thread on which we listen to primary connection for new clients
    private volatile boolean listening;
    /**
     * Begin hosting a server.
     * Accepted clients will break out into individual data connections on a separate thread.
     */
    public void host(){
        primarySocket = Gdx.net.newServerSocket(Network.PROTOCOL,"localhost",Network.PORT,null);
        clients = Collections.synchronizedList(new ArrayList<ClientListener>());
        cleanupQueue = new Queue<>();
        System.out.println("Server listening on 127.0.0.1:" + Integer.toString(Network.PORT));//TODO: listen on other interfaces other than loopback..

        primarySocketThread = new Thread(
            new Runnable() {
                public void run(){
                    listening = true;
                    while (listening){
                        try{
                            Socket client = primarySocket.accept(null);
                            clients.add(new ClientListener(client,clients.size()+1));
                            System.out.println(client.getRemoteAddress().substring(1) + " Connected.");
                            System.out.flush();

                            if (clients.size() == Network.MAX_CLIENTS){
                                System.out.println("Server full. Stopped listening for new connections.");
                                System.out.flush();
                                listening = false;
                                break;
                            }
                        }catch(GdxRuntimeException e){
                            System.out.println("Server stopped.");
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
    }

    @Override
    public void dispatchStartGame() {
        System.out.println("Starting game.");
        for (ClientListener c : clients){
            c.dispatchStartGame();
        }
    }

    @Override
    public void dispatchCreatePlayer(int x, int y) {
        System.out.println("Creating a player on the client.");
        for (ClientListener c : clients){
            System.out.println("told a handler to dispatch our player to the client");
            c.dispatchCreatePlayer(x,y);
        }
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
}
