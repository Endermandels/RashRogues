package io.github.RashRogues;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servers can host multiple clients.
 * Note that the Server should execute the Host() method before attempting any other operations.
 *
 * After executing Host(), the server will always be listening for new connections,
 * up until the point that the maximum number of clients is fufilled.
 *
 * New connections that are made are then moved to a seperate thread,
 * where commmunication is estabished between the server and client.
 *
 * speakOne(Client) sends a message to a single client.
 * speakAll() sends message to all clients.
 *
 */
public class Server {
    public static enum ServerStatus{
        DEAD,
        ALIVE_ACCEPTING_CLIENTS,
        ALIVE_REFUSING_CLIENTS,
        ALIVE_IN_GAME
    }

    public static final int PORT = 5999;
    public static final int IO_BUFFER_SIZE = 128;
    public static final Net.Protocol PROTOCOL = Net.Protocol.TCP;
    public static final int MAX_CLIENTS = 4;
    public volatile boolean acceptingClients;

    private Thread newConnectionsThread;    // Thread on which to listen for new connections.
    private ServerSocket newConnectionsSocket;  // Socket on which to listen for new connections.
    private List<Socket> clientSockets;     // Sockets on which to talk to clients.

    /**
     * Listen for new client connections.
     * Stop listening if we reach the maximum number of clients.
     */
    public void host(){
        this.newConnectionsSocket = Gdx.net.newServerSocket(Server.PROTOCOL,"localhost",Server.PORT,null);
        this.clientSockets = Collections.synchronizedList(new ArrayList<Socket>());
        this.newConnectionsThread = new Thread(
                new Runnable() {
                    public void run(){
                        while (acceptingClients) {
                            try{
                                Socket client = newConnectionsSocket.accept(null);
                                clientSockets.add(client);
                                new ConnClient(client);
                                System.out.println("A Client connected.");
                                System.out.flush();
                                acceptingClients = (clientSockets.size() == Server.MAX_CLIENTS);
                            /*
                                Note: Because accept() blocks, the only way to purposefully get out of listening
                                for new clients reliably is to send the thread an interrupt, which throws an exception.
                                If, when we run into an exception, we see the isInteruppted() flag, then we know it was
                                a purposeful interrupt, and not an actual issue. Otherwise print our stack trace like usual.
                             */
                            } catch (GdxRuntimeException e){
                                if (Thread.currentThread().isInterrupted()){
                                    System.out.println("Stopped listening for clients!");
                                    break;
                                }
                                e.printStackTrace();
                            }
                            newConnectionsSocket.dispose();
                        }
                    }
                }
        );
        this.acceptingClients = true;
        this.newConnectionsThread.start();
    }

    /**
     * Stop accepting new connections.
     */
    public void lock(){
        try {
            this.acceptingClients = false;
            this.newConnectionsThread.interrupt();
            this.newConnectionsThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}


