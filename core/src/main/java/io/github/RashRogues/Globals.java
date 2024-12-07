package io.github.RashRogues;

import Networking.Endpoint;
import Networking.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Globals {
    public static int PAROLE_TIME = 3;
    public static Network network;
    public RRScreen currentScreen;
    public HashMap<Integer, Player> players = new HashMap<>();
    public HashMap<Integer,Entity> replicatedEntities = new HashMap<>();
    public HashSet<Entity> replicatedEntitiesSet  = new HashSet<>();
    public HashSet<Player> playersSet = new HashSet<>();
    public HashSet<Integer> clientSet = new HashSet<>();
    public int currentNumPlayers = 0;
    public int pid = -1;
    private int entityIncrementor = 0;
    /**
     * Add an Entity to the game.
     * Entities that are registered will be automatically placed in the current screen.
     * If the networked parameter is set to true, the entity will be assigned a replication ID,
     * and will be matched with an entity on a foreign endpoint.
     * If no screen is currently active, the entity will not be created.
     * @param e Entity to register
     * @param networked Auto-associate with another entity on a different endpoint?
     */
    public void registerEntity(Entity e, boolean networked){
        if (currentScreen == null){
           return;
        }

        System.currentTimeMillis();

        if (networked){
            replicatedEntities.put(entityIncrementor, e);
            replicatedEntitiesSet.add(e);
            e.id = entityIncrementor;
            entityIncrementor+=1;
        }
        System.out.println("Registered : " + e.toString() + " with pid of " + Integer.toString(e.id));
        RRGame.globals.currentScreen.registerEntity(e);
    }

    /**
     * Remove an entity from the game.
     * @param e Entity
     */
    public void deregisterEntity(Entity e){

        if (e == null){
            return;
        }

        //This is a networked entity
        if (e.id != -1){

            //we are the server. Tell client to eliminate this entity.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity(e.id);
            }

            //stop tracking this entity.
            this.removeReplicatedEntity(e);
        }

        //remove from screen.
        RRGame.globals.currentScreen.removeEntity(e);
    }

    /**
     * Get all entites that are replicated on 2 or more endpoints.
     * @return ArrayList of Entities
     */
    public ArrayList<Entity> getReplicatedEntities(){
        ArrayList entities = new ArrayList();
        for (Entity e : replicatedEntitiesSet){
            entities.add(e);
        }
        return entities;
    }

    /**
     * Get the entity object associated with a replicated entity id.
     * @param id ID of entity
     * @return Entity associated with id
     */
    public Entity getReplicatedEntity(int id){
        if (this.replicatedEntities.containsKey(id)){
            return this.replicatedEntities.get(id);
        }
        return null;
    }

    /**
     * Stop tracking a replicated entity.
     * @param e Entity to clear out.
     */
    public void removeReplicatedEntity(Entity e){
        if (e.id == -1){
            return;
        }
        if (this.replicatedEntities.containsKey(e.id)){
            this.replicatedEntities.remove(e.id);
        }
        if (this.replicatedEntitiesSet.contains(e)){
            this.replicatedEntitiesSet.remove(e);
        }
    }

    /**
     * Keep track of an additional player.
     * @param pid
     * @param player
     */
    public void addPlayer(int pid, Player player){
        RRGame.globals.players.put(pid,player);
        RRGame.globals.playersSet.add(player);
    }

    /**
     * Keep track of an additional client.
     * @param pid
     */
    public void addClient(int pid){
        RRGame.globals.clientSet.add(pid);
    }

    /**
     * Stop tracking a player.
     * @param pid
     */
    public void removePlayer(int pid){
        Player p = RRGame.globals.players.get(pid);
        RRGame.globals.players.remove(pid);
        RRGame.globals.playersSet.remove(p);
    }

    /**
     * Stop tracking a specific client.
     * @param pid
     */
    public void removeClient(int pid){
        if (RRGame.globals.clientSet.contains(pid)){
            RRGame.globals.clientSet.remove(pid);
        }
    }

    public void timeStampEntity(Entity e, int pid, long timestamp){


    }
}
