package Networking;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Solicitee {
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<String> broadcasts;
    private Thread listener;
    private volatile boolean searching = true;

    public Solicitee(){
        this.broadcasts = new ConcurrentLinkedQueue<>();
        try{
            try {
                this.socket = new DatagramSocket(Network.SOLICITATION_PORT,InetAddress.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (java.net.SocketException e){
            System.out.println("An error occurred while hosting the game.");
            System.out.println(e.toString());
        }
    }

    public boolean validate(byte[] message){
        if (message.length != Network.SOLICITATION_KEY.length){
            return false;
        }

        for (int i = 0; i < message.length; i++) {
            if (message[i] != Network.SOLICITATION_KEY[i]) {
                return false;
            }
        }

        return true;
    }

    public void listen(){
        this.listener = new Thread(
            new Runnable() {
                public void run() {
                    while (searching){
                        byte[] rcv_msg = new byte[Network.SOLICITATION_KEY.length];
                        DatagramPacket p = new DatagramPacket(rcv_msg,Network.SOLICITATION_KEY.length);
                        try {
                            socket.receive(p);
                        } catch (IOException e) {
                            break;
                        }
                        boolean valid = validate(rcv_msg);
                        if (valid){
                            broadcasts.add(p.getAddress().toString().substring(1));
                        }
                    }
                }
            });
        this.listener.start();
    }

    public ArrayList<String> fetch(){
        ArrayList<String> messages = new ArrayList<>();
        while (!this.broadcasts.isEmpty()){
            messages.add(this.broadcasts.poll());
        }
        return messages;
    }

    public void dispose(){
        this.searching = false;
        this.socket.close();
        this.listener.interrupt();
        try {
            this.listener.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
