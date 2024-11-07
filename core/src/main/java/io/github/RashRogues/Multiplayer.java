package io.github.RashRogues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Holds relevant information to multiplayer gameplay.
 * All game instances must register as a server or client.
 */
public class Multiplayer {
   public Server server;
   public Client client;
   private ArrayList<Entity> networkEntities;
   private ArrayList<Player> networkPlayers;

   public static enum ClientType{
       SERVER,
       CLIENT
   }

   public static enum MessageType{
       CREATE_ENTITY,
       CREATE_PLAYER,
       UPDATE_ENTITY,
       RAW_INPUT
   }

    public ClientType clientType;
     /**
     * Register as a server or client.
     * @param type Server or Client?
     */
   public void setup(ClientType type){
       this.networkPlayers = new ArrayList<>();
      switch(type){
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

//   public int registerEntity(Entity entity){
//       int nid = this.networkEntities.size();
//       this.networkEntities.add(entity);
//       this.server.broadcastCreateEntity(entity);
//       return nid;
//   }

   public int registerPlayer(Player player) {
       int nid = this.networkPlayers.size();
       this.networkPlayers.add(player);
       this.server.broadcastCreatePlayer(player);
       return nid;
   }

   public ArrayList<Player> getNetworkPlayers(){
        return this.networkPlayers;
   }
}
