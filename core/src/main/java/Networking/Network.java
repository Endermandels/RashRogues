package Networking;

import com.badlogic.gdx.Net;
import io.github.RashRogues.RRGame;

public class Network {
   public static enum EndpointType {
      UNSET,
      CLIENT,
      SERVER
   }

   /* See readme for instructions on how to set HOST_IP and BROADCAST_IP */

   /* Set Me! */
   public static final String HOST_IP = "127.0.0.1";
   //public static final String HOST_IP = "192.168.5.24"

   /* Set Me! */
   public static final String BROADCAST_IP = "127.0.0.1";
   //public static final String HOST_IP = "192.168.5.255"


   public static final int PORT = 5999;
   public static final int SOLICITATION_PORT = 5998;
   public static final int SOLICITATION_FREQUENCY = 120;
   public static final byte[] SOLICITATION_KEY = {51,25,64,12};
   public static final int IO_BUFFER_SIZE = 256;
   public static final int MAX_CLIENTS = 4;
   public static final int HEARTBEAT_THRESHOLD = 60;
   public static final Net.Protocol PROTOCOL = Net.Protocol.TCP;
   public Endpoint connection;
   public EndpointType type = EndpointType.UNSET;
   public static String ip = "";

   public void start(EndpointType type){
      if (this.type != EndpointType.UNSET){
         return;
      }
      this.type = type;
      switch (type){
         case CLIENT:
            this.connection = new Client();
            break;
         case SERVER:
            this.connection = new Server();
            ((Server) this.connection).host();
            break;
      }
   }

   public void reset(){
      this.type = EndpointType.UNSET;
      if (this.connection != null){
         this.connection.dispose();
      }
   }

   public void dispose(){
      if (this.type != EndpointType.UNSET){
         this.connection.dispose();
      }
   }
}
