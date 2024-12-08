package io.github.RashRogues;

import Networking.Endpoint;
import Networking.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Globals {
    public long frame = 0;

    public static Network network;
    public RRScreen currentScreen;

    public HashSet<Player> playersSet = new HashSet<>();
    public HashSet<Integer> clientSet = new HashSet<>();
    public HashMap<Integer, Player> players = new HashMap<>();

    //Entities that are deterministic can be associated with foreign entities simply by creation order.
    public HashMap<Integer,Entity> deterministicReplicatedEntities = new HashMap<>();
    public HashSet<Entity> deterministicReplicatedEntitiesSet  = new HashSet<>();

    // Entities that are not deterministic but must be tied to a frame.
    private final HashMap<Integer, HashMap<Long,Entity>> nondeterministicReplicatedEntities = new HashMap<>();

    public int currentNumPlayers = 0;
    public int pid = -1;


    public void setPID(int pid){
       this.pid = pid;
        this.nondeterministicReplicatedEntities.put(pid,new HashMap<>());
    }

    /**
     * Add an Entity to the game.
     * Entities that are registered will be automatically placed in the current screen.
     * If the networked parameter is set to true, the entity will be assigned a replication ID,
     * and will be matched with an entity on a foreign endpoint.
     * If no screen is currently active, the entity will not be created.
     * @param e Entity to register
     * @param deterministic Auto-associate with another entity on a different endpoint based on creation order?
     */
    public void registerEntity(Entity e, boolean deterministic, int pid, long frame){

        //Cannot register entities if there is no screen.
        if (currentScreen == null){
           return;
        }

        // This entity needs to be indexed to a certain 'frame'. It originated the from player with a pid of 'pid'.
        if (pid != -1) {
            System.out.println("we registered " + e.toString() + " from " + Integer.toString(pid) + " with frame id of : " + Long.toString(frame) );
            this.nondeterministicReplicatedEntities.get(pid).put(frame,e);
            e.pid = pid;

        // This entity is matched with another entity based on creation order.
        } else if (deterministic){
            deterministicReplicatedEntities.put(this.deterministicReplicatedEntitiesSet.size(), e);
            e.id = deterministicReplicatedEntitiesSet.size();
            deterministicReplicatedEntitiesSet.add(e);
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

        //This is a deterministic replicated entity
        if (e.id != -1){

            //we are the server. Tell client to eliminate this entity.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity(e.id);
            }

            //stop tracking this entity.
            this.removeReplicatedEntity(e);
        }

        //This is a nondeterministic replicated entity
        if (e.pid != -1){
            this.nondeterministicReplicatedEntities.get(e.pid).remove(e.frame);

            //We are the server. Tell client to eliminate this entity.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity2(e.pid, e.frame);
            }
        }

        //Remove from screen.
        RRGame.globals.currentScreen.removeEntity(e);
    }

    /**
     * Get all entites that are replicated on 2 or more endpoints.
     * @return ArrayList of Entities
     */
    public ArrayList<Entity> getdeterministicReplicatedEntities(){
        ArrayList entities = new ArrayList();
        for (Entity e : deterministicReplicatedEntitiesSet){
            entities.add(e);
        }
        return entities;
    }

    /**
     * Get an entity that was registered as nondeterministic.
     * @param pid
     * @param frame
     * @return
     */
    public Entity findNondeterministicEntity(int pid, long frame){
       if (this.nondeterministicReplicatedEntities.containsKey(pid)){
           return this.nondeterministicReplicatedEntities.get(pid).get(frame);
       }
       return null;
    }

    /**
     * Get the entity object associated with a replicated entity id.
     * @param id ID of entity
     * @return Entity associated with id
     */
    public Entity getReplicatedEntity(int id){
        if (this.deterministicReplicatedEntities.containsKey(id)){
            return this.deterministicReplicatedEntities.get(id);
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
        if (this.deterministicReplicatedEntities.containsKey(e.id)){
            this.deterministicReplicatedEntities.remove(e.id);
        }
        if (this.deterministicReplicatedEntitiesSet.contains(e)){
            this.deterministicReplicatedEntitiesSet.remove(e);
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
        this.nondeterministicReplicatedEntities.put(pid,new HashMap<>());
    }

    /**
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
