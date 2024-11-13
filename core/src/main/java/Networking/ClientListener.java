package Networking;

import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientListener implements Endpoint {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<Packet> messages = new ConcurrentLinkedQueue<>();
    private int pid = 0; //we are the server. PID 0

    public ClientListener(Socket socket, int pid) {
        try {
            this.socket = socket;

            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();

            objectInputStream = null;
            objectOutputStream = new ObjectOutputStream(out);

            dispatchWelcome(pid);
            listen(in);
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
                            objectInputStream = new ObjectInputStream(in);
                            while (true) {
                                messages.add((Packet) objectInputStream.readObject());
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
     * Process all queued messages that are available.
     */
    public void processMessages() {
        while (!this.messages.isEmpty()){
            Packet p = this.messages.poll();
            switch (p.getPacketType()){
                case UPDATE:
                    handleUpdate(p);
                    break;
                case CREATE:
                    handleCreate(p);
                    break;
                case DESTROY:
                    handleDestroy(p);
                    break;
                case WELCOME:
                    break;
            }
        }
    }

    /* Dispatchers */

    /**
     * 1. Initialize a new client with their playerID.
     *
     * @param pid Player ID to assign to new player.
     */
    public void dispatchWelcome(int pid) {
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
    public void dispatchCreate(Entity entity){
        try{
            EntityType type = entity.getType();
            String uid = Integer.toString(this.pid) + '_' + entity.toString();
            String texture = ((FileTextureData) entity.getTexture().getTextureData()).getFileHandle().path();
            float x = entity.getX();
            float y = entity.getY();
            objectOutputStream.writeObject(new PacketCreate(type,uid,texture,(int) x, (int) y));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /* Handlers */
    /**
     * Create an object on our client
     */
    public void handleCreate(Packet p){
        PacketCreate create = (PacketCreate) p;
        //register up!
    }

    /**
     * Update an object on our client
     */
    public void handleUpdate(Packet p){

    }

    /**
     * Destroy an object on our client
     */
    public void handleDestroy(Packet p){

    }
}