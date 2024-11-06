package io.github.RashRogues;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    public static final int PORT = 5999;
    public static final int IO_BUFFER_SIZE = 128;

    private ServerSocket connectionSocket;
    private List<Socket> clientSockets;

    public void Host(Socket s, int port){
        connectionSocket = Gdx.net.newServerSocket(Net.Protocol.TCP,"localhost",PORT,null);
        clientSockets = Collections.synchronizedList(new ArrayList<Socket>());
        new Thread(
                new Runnable() {
                    public void run(){
                        Socket client = connectionSocket.accept(null);
                        clientSockets.add(client);
                        System.out.println("A Client connected.");
                        System.out.flush();
                    }
                }
        ).start();
    }
}


