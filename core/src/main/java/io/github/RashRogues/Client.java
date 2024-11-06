package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;


public class Client {
    private Socket socket;
    private InputStream input;
    private OutputStream output;

    public Client(){
        join("localhost");
        speak("Hello Server!");
    }

    public void speak(String msg){
        try {
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            int byteCount =  msgBytes.length;

            if (byteCount > Server.IO_BUFFER_SIZE){
                System.out.println("Warning, message too large. Some data may be lost!");
            }

            this.output.write(msgBytes,0, Server.IO_BUFFER_SIZE);
            this.output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


   public void join(String hostname){
     this.socket = Gdx.net.newClientSocket(Net.Protocol.TCP,hostname,Server.PORT,null);
     this.input = this.socket.getInputStream();
     this.output = this.socket.getOutputStream();
     byte[] bytes = new byte[Server.IO_BUFFER_SIZE];
       new Thread(
           new Runnable() {
               public void run() {
                   int bytesRead = 0;
                   try {
                       bytesRead = input.read(bytes,0, Server.IO_BUFFER_SIZE);
                       byte[] msgBytes = new byte[bytesRead];
                       System.arraycopy(bytes,0,msgBytes,0,bytesRead);
                       String msgString = new String(msgBytes, StandardCharsets.UTF_8);
                       System.out.println("Server sez: " + msgString);
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
               }
           }
       );

   }



}
