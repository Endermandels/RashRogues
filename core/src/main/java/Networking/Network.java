package Networking;

import com.badlogic.gdx.Net;

public class Network {
   public static enum MsgType{
      UPDTE_ENT,
      CREAT_ENT,
      INIT_PLY
   }

   public static enum EndpointType {
      UNSET,
      CLIENT,
      SERVER
   }

   public static final int PORT = 5999;
   public static final int IO_BUFFER_SIZE = 256;
   public static final int MAX_CLIENTS = 4;
   public static final Net.Protocol PROTOCOL = Net.Protocol.TCP;
   public Endpoint connection;
   public EndpointType type = EndpointType.UNSET;

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
}
