package Networking;

import java.io.IOException;
import java.net.*;

public class Solicitor {
    private static int BROADCAST_FREQUENCY = 5;
    private int lastBroadcast = 0;
    private DatagramSocket socket;

    public Solicitor(){
        try{
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (java.net.SocketException e){
            System.out.println("An error occurred while hosting the game.");
        }
    }

    public void solicit(){
        if (this.lastBroadcast > BROADCAST_FREQUENCY){
            byte[] msg = new byte[]{100};

            InetAddress ip = null;
            try {
                ip = InetAddress.getByName("127.0.0.1"); //TODO: dynamically figure out what the broadcast IP for the current subnet is.
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DatagramPacket p = new DatagramPacket(msg, 1, ip, Network.SOLICITATION_PORT);
            try {
                this.socket.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.lastBroadcast = 0;
        }else{
            this.lastBroadcast+=1;
        }
    }
}
