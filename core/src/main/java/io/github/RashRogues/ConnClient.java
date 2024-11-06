package io.github.RashRogues;

import com.badlogic.gdx.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Representative of a single Server->Client connection
 * Used to send data to the client, as well as listen for incoming data.
 */
public class ConnClient {
    public Socket socket;
    private InputStream input;
    private OutputStream output;

    public ConnClient(Socket socket){
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.listen();
        this.speak("Hi Client!");
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
            this.output.write(msgBytes,0,Server.IO_BUFFER_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
