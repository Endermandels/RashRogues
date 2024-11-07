package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * Clients are allowed to join one server.
 * Client must first join a server with the join() method, before sending data with the speak() method etc..
 * Failure to follow this order of operations will result in IO errors.
 */
public class Client {
    private InputStream input;
    private OutputStream output;
    private Thread listeningThread;
    private volatile boolean connectedToServer;

    /**
     * Send msg to the server. Must abide by the Server's IO_BUFFER_SIZE,
     * which is the maximum size (in bytes) that a message can be.
     * @param msg Message to send to the server.
     */
    public void speak(String msg){
        try {
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            int byteCount =  msgBytes.length;

            if (byteCount > Server.IO_BUFFER_SIZE){
                System.out.println("Warning, message too large. Some data may be lost!");
            }

            this.output.write(msgBytes,0, msgBytes.length);
            this.output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Join a server.
     * This method is prerequsite to Speak().
     * Connection will take place through the ports and protocols defined in Server.java.
     * @param hostname Hostname or IP of the server to connect to.
     */
   public void join(String hostname){
       if (this.connectedToServer){
           System.out.println("Already connected to a server. Disconnect your current connection before attempting to start to a new one.");
           return;
       }
        Socket socket = Gdx.net.newClientSocket(Server.PROTOCOL, hostname, Server.PORT, null);
        this.connectedToServer = true;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        byte[] bytes = new byte[Server.IO_BUFFER_SIZE];
        this.listeningThread = new Thread(
           new Runnable() {
               public void run() {
                   while (connectedToServer) {
                       int bytesRead = 0;
                       try {
                           bytesRead = input.read(bytes, 0, Server.IO_BUFFER_SIZE);
                           byte[] msgBytes = new byte[bytesRead];
                           System.arraycopy(bytes, 0, msgBytes, 0, bytesRead);
                           String msgString = new String(msgBytes, StandardCharsets.UTF_8);
                           System.out.println("Server sez: " + msgString);
                       } catch (IOException e) {
                           if (Thread.currentThread().isInterrupted()){
                               System.out.println("Connection to was closed.");
                               break;
                           }
                           throw new RuntimeException(e);
                       }
                   }
                   socket.dispose();
               }
           }
        );
        this.listeningThread.start();
   }


    /**
     * Leave the game.
     *
     * TODO: This needs to tell the server that we left intentionally so that it can prepare.
     */
   public void leave(){
      this.connectedToServer = false;
      this.listeningThread.interrupt();
       try {
           this.listeningThread.join();
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       }


   }
}
