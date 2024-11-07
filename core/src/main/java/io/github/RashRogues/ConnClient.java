package io.github.RashRogues;

import com.badlogic.gdx.net.Socket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Representative of a single Server->Client connection
 * Used to send data to the client, as well as listen for incoming data.
 */
public class ConnClient {
    public Socket socket;
    private InputStream input;
    private OutputStream output;
    private ObjectInputStream oInput;
    private ObjectOutputStream oOutput;


    public ConnClient(Socket socket){
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        try {
            this.oOutput = new ObjectOutputStream(output);
            this.oInput = new ObjectInputStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.listen();
        this.introduce();
    }

    /**
     * Start listening to the input stream of the socket.
     * The client will send data to the server here.
     */
    private void listen(){
        InputStream in = this.input;
        byte[] bytes = new byte[128];

        new Thread(
            new Runnable() {
                public void run() {
                    try {
                        int bytesRead = in.read(bytes,0, Server.IO_BUFFER_SIZE);
                        if (bytesRead > 0){
                            byte[] msgBytes = new byte[bytesRead];
                            System.arraycopy(bytes,0,msgBytes,0,bytesRead);
                            String msgString = new String(msgBytes, StandardCharsets.UTF_8);
                            System.out.println("Client sez: " + msgString);
                            System.out.flush();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    /**
     * Write data to the client here.
     */
    private void speak(String msg){
        try {
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            int byteCount  = msgBytes.length;
            if (byteCount > Server.IO_BUFFER_SIZE){
                System.out.println("Warning: Message too big for buffer. Some data may be lost!");
            }
            this.output.write(msgBytes,0, msgBytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * It is necessary to send new players all of the client side controlled data when they join the game,
     * as they may have missed it.
     */
    public void introduce(){
        ArrayList<Player> networkPlayers = RRGame.mp.getNetworkPlayers();
        for (int i = 0; i < networkPlayers.size(); i++){
            Player p = networkPlayers.get(i);
            this.requestCreatePlayer(p.getNetworkID(),i,"DefaultImages/rogue.png",p.getX(),p.getY());
        }
    }


    /**
     * Request client to create a new player.
     * @param texImg Local path to the texture that should be applied to the player.
     * @param nid Network ID of local player instance, so as to keep players synchronized.
     * @param pid Whom this player should be under the control of.
     * @param x X position of new player.
     * @param y Y position of new player.
     */
    public void requestCreatePlayer(int nid, int pid, String texImg, float x, float y){
        try {
            this.oOutput.writeObject(new ReqCreatePlayer(nid,texImg,x,y));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
