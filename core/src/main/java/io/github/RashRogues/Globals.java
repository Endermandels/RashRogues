package io.github.RashRogues;

import Networking.Network;
import Networking.ReplicationType;

import java.util.*;

public class Globals {
    public long frame = 0;
    public int eid = 5; //starts at 5 so as not to overlap with player id values.

    public int pid = -1;
    public int currentNumPlayers = 0;

    public static Network network;
    public RRScreen currentScreen;

    private Random random;
    private long seed;

    public HashSet<Player> playersSet = new HashSet<>();
    public HashSet<Integer> clientSet = new HashSet<>();
    public HashMap<Integer, Player> players = new HashMap<>();

    //Entities that are deterministic can be associated with foreign entities simply by creation order.
    public HashMap<Integer,Entity> deterministicReplicatedEntities = new HashMap<>();
    public HashSet<Entity> deterministicReplicatedEntitiesSet  = new HashSet<>();

    // Entities that are not deterministic but must be tied to a frame.
    private final HashMap<Integer, HashMap<Long,Entity>> nondeterministicReplicatedEntities     = new HashMap<>();
    private final HashMap<Integer, HashMap<Long,Entity>> nondeterministicReplicatedProjectiles  = new HashMap<>();

    private final HashMap<Integer,Long> projectileNumber                                        = new HashMap<>();

    // These items can only be purchased one time per player.
    public final HashSet<BuyableItem> nonRepurchasableItems = new HashSet<>(Arrays.asList(BuyableItem.CLOAK,BuyableItem.DAGGER, BuyableItem.RING));

    private final HashMap<Integer,Entity> keys = new HashMap<>();
    private int keyCount = 0;

    public void setupRandomNumberGenerator(){
        seed = new Random().nextLong();
        random = new Random(seed);
    }

    public void setPID(int pid){
       this.pid = pid;
        this.nondeterministicReplicatedEntities.put(pid,new HashMap<>());
        this.nondeterministicReplicatedProjectiles.put(pid, new HashMap<>());
        this.projectileNumber.put(pid,0l);
    }

    /**
     * Add an Entity to the game.
     * Entities that are registered will be automatically placed in the current screen.
     * If the networked parameter is set to true, the entity will be assigned a replication ID,
     * and will be matched with an entity on a foreign endpoint.
     * If no screen is currently active, the entity will not be created.
     * @param e Entity to register
     * @param creatorID Who created this?
     * @param number Which number is this? (frame #, projectile #)
     */
    public void registerEntity(Entity e, ReplicationType type, int creatorID, long number) {

        //Cannot register entities if there is no screen.
        if (currentScreen == null) {
            return;
        }

        // This entity needs to be indexed to a certain 'frame', and originator.
         if (type == ReplicationType.FRAME_NUMBER) {
            this.nondeterministicReplicatedEntities.get(creatorID).put(number, e);
            e.pid = creatorID;
            e.number = number;

        // This entity needs to be indexed to projectile creation number, and originator.
        } else if (type == ReplicationType.PLAYER_PROJECTILE_NUMBER){
            this.nondeterministicReplicatedProjectiles.get(creatorID).put(number,e);
            e.pid = creatorID;
            e.number = number;
            this.projectileNumber.put(creatorID,this.projectileNumber.get(creatorID)+1);

        // This entity is matched with another entity based on creation order.
        } else if (type == ReplicationType.ENTITY_NUMBER){
            deterministicReplicatedEntities.put(this.eid, e);
            e.id = this.eid;
            deterministicReplicatedEntitiesSet.add(e);
            this.eid++;
        } else if (type == ReplicationType.KEY){
            keys.put(this.keyCount,e);
            e.id = keyCount;
            keyCount++;
        }
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

         if (e.replicationType == ReplicationType.PLAYER){
            Player p = (Player) e;
            this.players.remove(p.associatedPID);
            this.playersSet.remove(p);

            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyPlayer(p.associatedPID);
            }

        }

        //This is a deterministic replicated entity
        if (e.replicationType == ReplicationType.ENTITY_NUMBER){

            // We are the server. Tell client to eliminate this entity.
            // If this entity is client-side-only, don't bother dispatching removal
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity(e.id);
            }
            //stop tracking this entity.
            this.removeReplicatedEntity(e);
        }

        //This is a nondeterministic replicated entity
        if (e.replicationType == ReplicationType.FRAME_NUMBER){
            this.nondeterministicReplicatedEntities.get(e.pid).remove(e.number);
            //We are the server. Tell client to eliminate this entity.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity2(e.pid, e.number);
            }
        }

        //This is a nondeterministic replicated projectile
        if (e.replicationType == ReplicationType.PLAYER_PROJECTILE_NUMBER){
            this.nondeterministicReplicatedProjectiles.get(e.pid).remove(e.number);
            //We are the server. Tell client to eliminate this entity.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyProjectile(e.pid, e.number);
            }
        }

        //Remove from screen.
        RRGame.globals.currentScreen.removeEntity(e);
    }

    public long getProjectileNumber(int pid){
        if (this.projectileNumber.containsKey(pid)) {
            return this.projectileNumber.get(pid);
        }
        return 0;
    }

    /**
     * Get the entity of a registered Key.
     * @param keyID
     * @return
     */
    public Entity getKey(int keyID){
        return this.keys.get(keyID);
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
     * Get a projectile that was registered as nondeterministic.
     * @param pid
     * @param number
     * @return
     */
    public Entity findNondeterministicProjectile(int pid, long number){
        if (this.nondeterministicReplicatedProjectiles.containsKey(pid)){
            return this.nondeterministicReplicatedProjectiles.get(pid).get(number);
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
        this.nondeterministicReplicatedProjectiles.put(pid,new HashMap<>());
        this.projectileNumber.put(pid,0l);
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

    public void executeCommandOnCurrentScreen(String[] cmd){
        if (this.currentScreen != null){
            this.currentScreen.executeCommand(cmd);
        }
    }

    /**
     * Set the seed for all random numbers.
     * @param seed
     */
    public void setRandomSeed(long seed){
        this.seed = seed;
        this.random = new Random(this.seed);
    }

    /**
     * Get the currently set seed, used for random number generation.
     * @return seed
     */
    public long getRandomSeed(){
        return this.seed;
    }


    /**
     * Returns a random number generator.
     * This number generator is synced to all clients.
     * @return Random
     */
    public Random getRandom(){
        Random newRandom = new Random(this.random.nextLong());
        return newRandom;
    }

}
