package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<ClientListener> clients; // 1. data connection to connected clients.
    private ServerSocket primarySocket;   // 2. primary connection where we listen for new clients.
    private Thread primarySocketThread;   // 3. thread on which we listen to primary connection for new clients

    /**
     * Begin hosting a server.
     * Accepted clients will break out into individual data connections on a separate thread.
     */
    public void host(){
        primarySocket = Gdx.net.newServerSocket(Network.PROTOCOL,"localhost",Network.PORT,null);
        clients = Collections.synchronizedList(new ArrayList<ClientListener>());
        System.out.println("Server listening on 127.0.0.1:" + Integer.toString(Network.PORT));//TODO: listen on other interfaces other than loopback..

        primarySocketThread = new Thread(
            new Runnable() {
                public void run(){
                    while (true){
                        try{
                            Socket client = primarySocket.accept(null);
                            clients.add(new ClientListener(client,clients.size()+1));
                            System.out.println(client.getRemoteAddress().substring(1) + " Connected.");
                            System.out.flush();

                            if (clients.size() == Network.MAX_CLIENTS){
                                System.out.println("Server full. Stopped listening for new connections.");
                                System.out.flush();
                                break;
                            }
                        }catch(GdxRuntimeException e){
                            System.out.println("Server stopped: " + e.toString());
                            System.out.flush();
                            e.printStackTrace();
                        }
                    }
                    primarySocket.dispose();
                }
            }
        );
        primarySocketThread.start();
    }

    public void processMessages() {

    }

    @Override
    public void dispatchStartGame() {
        for (ClientListener cL : clients){
           cL.dispatchStartGame();
        }
    }

    public void dispatchCreate(Entity entity){
        for (ClientListener cL : clients){
            cL.dispatchCreate(entity);
        }
    }

    public void dispatchCreate(Player player){
        dispatchCreate((Entity) player);
    }

    @Override
    public void dispatchUpdate(Entity entity) {
        for (ClientListener cL : clients ){
            cL.dispatchUpdate(entity);
        }
    }

    @Override
    public void dispatchUpdate(Player player) {
        dispatchUpdate((Entity) player);
    }


}
