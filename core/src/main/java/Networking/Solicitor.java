package Networking;

import java.io.IOException;
import java.net.*;

public class Solicitor {
    private int lastBroadcast = 0;
    private DatagramSocket socket;

    public Solicitor() {
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (java.net.SocketException e) {
            System.out.println("An error occurred while hosting the game.");
        }
    }

    public void solicit() {
        if (this.lastBroadcast > Network.SOLICITATION_FREQUENCY) {
            InetAddress ip = null;
            try {
                ip = InetAddress.getByName("192.168.11.255"); //TODO: dynamically figure out what the broadcast IP for the current subnet is.
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DatagramPacket p = new DatagramPacket(Network.SOLICITATION_KEY.clone(), Network.SOLICITATION_KEY.length, ip, Network.SOLICITATION_PORT);

            try {
                this.socket.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.lastBroadcast = 0;
        } else {
            this.lastBroadcast += 1;
        }
    }

    public void dispose() {
        this.socket.close();
    }
}
