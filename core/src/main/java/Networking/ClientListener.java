package Networking;

import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;

public class ClientListener implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Thread listeningThread;

    public ClientListener(Socket socket, int pid) {
        try {
            this.socket = socket;

            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();

            objectInputStream = null;
            objectOutputStream = new ObjectOutputStream(out);

            listen(in);
            welcome(pid);
        } catch (IOException | InterruptedException e) {
            System.out.println("Catastrophic communication error! Exiting!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Endpoint type of current endpoint.
     *
     * @return Endpoint Type
     */
    public Network.EndpointType getType() {
        return Network.EndpointType.SERVER;
    }

    /**
     * Listen for and handle messages from client.
     * listen() determines the lifetime of the connection; when this function concludes the socket/streams will close.
     */
    private void listen(InputStream in) throws IOException, InterruptedException {
        this.listeningThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            ObjectInputStream objInput = null;
                            objInput = new ObjectInputStream(in);
                            while (true) {
                                Packet m = (Packet) objInput.readObject();
                                System.out.println(m);
                                System.out.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        this.listeningThread.start();
    }

    /**
     * 1. Initialize a new client with their playerID.
     *
     * @param pid Player ID to assign to new player.
     */
    public void welcome(int pid) {
        try {
            objectOutputStream.writeObject(new PacketWelcome(pid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 2. Create a new entity on the client
     *
     */
    public void create(Entity entity){
        try{
            EntityType type = EntityType.PLAYER;
            String uid = "123456";
            String texture = "tex";
            float x = entity.getX();
            float y = entity.getY();
            objectOutputStream.writeObject(new PacketCreate(type,uid,texture,(int) x, (int) y));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void processMessages() {

    }

}