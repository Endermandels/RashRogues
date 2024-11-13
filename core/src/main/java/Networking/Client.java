package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.EntityType;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Endpoint {
    private InputStream in;
    private OutputStream out;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<Packet> messages = new ConcurrentLinkedQueue<>();
    private int pid;

    public Client() {
        this.socket = Gdx.net.newClientSocket(Network.PROTOCOL, "localhost", Network.PORT, null);
        System.out.println("Connected to server on localhost:" + Integer.toString(Network.PORT));
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        try{
            this.objectOutputStream = new ObjectOutputStream(this.out);
            listen(in);
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private void listen(InputStream in) throws IOException, InterruptedException {
        this.listeningThread = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            ObjectInputStream objStream = null;
                            objStream = new ObjectInputStream(in); //blocks until data comes through.
                            while (true) {
                                messages.add((Packet) objStream.readObject());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Disconnected.");
                        System.out.flush();
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
                   handleInvite(p);
                   break;
           }
        }

    }


   /* Handlers */

    /**
     * Accept Invite To Server
     */
    public void handleInvite(Packet p){
        PacketWelcome invite = (PacketWelcome) p;
        this.pid = invite.pid;
    }

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


   /* Dispatchers */

    /**
     * 1. Create a new entity on the client
     */
    public void dispatchCreate(Entity entity) {
        try {
            EntityType type = entity.getType();
            String uid = Integer.toString(this.pid) + '_' + entity.toString();
            String texture = ((FileTextureData) entity.getTexture().getTextureData()).getFileHandle().path();
            float x = entity.getX();
            float y = entity.getY();
            this.objectOutputStream.writeObject(new PacketCreate(type, uid, texture, (int) x, (int) y));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }
}