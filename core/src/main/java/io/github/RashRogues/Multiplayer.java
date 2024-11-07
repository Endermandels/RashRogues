package io.github.RashRogues;

/**
 * Holds relevant information to multiplayer gameplay.
 * All game instances must register as a server or client.
 */
public class Multiplayer {
   public Server server;
   public Client client;
   public static enum ClientType{
       SERVER,
       CLIENT
   }
   public ClientType clientType;

    /**
     * Register as a server or client.
     * @param type Server or Client?
     */
   public void register(ClientType type){
      switch(clientType){
          case CLIENT:
              client = new Client();
              client.join("localhost");
              break;
          case SERVER:
              server = new Server();
              server.host();
              break;
      }
      this.clientType = type;
   }
}
