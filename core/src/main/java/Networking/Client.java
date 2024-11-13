package Networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import io.github.RashRogues.Entity;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Endpoint {
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private Thread listeningThread;
    public ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();

    public Client(){
        this.socket = Gdx.net.newClientSocket(Network.PROTOCOL,"localhost",Network.PORT,null);
        System.out.println("Connected to server on localhost:" + Integer.toString(Network.PORT));
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        try {
            listen(in);
        } catch (IOException | InterruptedException e) {
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
                            messages.add((Message) objStream.readObject());
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

    public Network.EndpointType getType() {
        return Network.EndpointType.CLIENT;
    }

    public void shareEntityCreation() {

    }

    public void shareEntityDesctruction(){

    }

    public void shareEntityUpdate() {

    }

//    private void handleMessageEntityCreate(RRGame game, Message m){
//        EntityMsg eMessage = (EntityMsg) m;
//        switch (eMessage.entityType){
//            case PLAYER:
//                Player p = new Player(game.am.get(eMessage.tex), (int) eMessage.x, (int) eMessage.y,64f,64f,true);
//                RRGame.globals.registerNetworkEntity(p, eMessage.uid);
//                break;
//        }
//    }

//    private void handleMessagePlayerInit(Message m){
//        RRGame.globals.setPlayerID(((GameMsg) m).plyNum);
//    }

    public void processMessages(){

    }

//    public void processMessages(RRGame game) {
//        while (this.messages.isEmpty() == false){
//            Message m = this.messages.remove();
//            switch (m.type()){
//                case INIT_PLY:
//                    handleMessagePlayerInit(m);
//                    break;
//                case CREAT_ENT:
//                    System.out.println("Creating a new thingamajig");
//                    handleMessageEntityCreate(game, m);
//                    break;
//                case UPDTE_ENT:
//                    handleMessageEntityUpdate(m);
//                    break;
//            }
//        }
//    }
}
