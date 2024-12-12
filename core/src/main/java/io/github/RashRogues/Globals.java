package io.github.RashRogues;

import Networking.Endpoint;
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
    private final HashMap<Integer,HashMap<Long, Entity>> nondeterministicReplicatedEnemyProjectiles = new HashMap<>();

    private final HashMap<Integer,Long> projectileNumber                                        = new HashMap<>();
    private final HashMap<Integer,Long> enemyProjectileNumber                                   = new HashMap<>();

    public void setupRandomNumberGenerator(){
        seed = new Random().nextLong();
        random = new Random(seed);
    }

    public void setPID(int pid){
       this.pid = pid;
        this.nondeterministicReplicatedEntities.put(pid,new HashMap<>());
        this.nondeterministicReplicatedProjectiles.put(pid, new HashMap<>());
        this.nondeterministicReplicatedEnemyProjectiles.put(pid, new HashMap<>());
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

        // This entity needs to be indexed to a certain enemy, and then the count of projectiles that enemy has produced.
        // in this scenario, creator is the id of the Entity, and number is the number of projectiles that entity has produced in the past.
        if (type == ReplicationType.ENEMY_PROJECTILE_NUMBER) {
            this.nondeterministicReplicatedEnemyProjectiles.get(creatorID).put(this.enemyProjectileNumber.get(creatorID),e);
            e.number = this.enemyProjectileNumber.get(creatorID);
            e.pid    = creatorID;
            this.enemyProjectileNumber.put(creatorID, this.enemyProjectileNumber.get(creatorID)+1);

        // This entity needs to be indexed to a certain 'frame', and originator.
        } else if (type == ReplicationType.FRAME_NUMBER) {
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
            this.enemyProjectileNumber.put(e.id,0l);
            this.nondeterministicReplicatedEnemyProjectiles.put(e.id, new HashMap<>());
            this.eid++;
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

        if (e.replicationType == ReplicationType.ENEMY_PROJECTILE_NUMBER){
            // Tell client to destroy this projectile.
            if (this.pid == 0){
                Globals.network.connection.dispatchDestroyEntity3(e.pid, e.number);
            }
        }

         if (e.replicationType == ReplicationType.PLAYER){
            Player p = (Player) e;
            this.players.remove(p.associatedPID);
            this.playersSet.remove(p);
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

    public long getEnemyProjectileNumber(int eid){
        if (this.enemyProjectileNumber.containsKey(eid)){
            return this.enemyProjectileNumber.get(eid);
        }
        return 0;
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
     * Get a projectile that was registered as nondeterministic, and was fired by entity 'eid'
     * @param eid
     * @param number
     * @return
     */
    public Entity findNondeterministicEnemyProjectile(int eid, long number){
        if (this.nondeterministicReplicatedEnemyProjectiles.containsKey(eid)){
            return this.nondeterministicReplicatedEnemyProjectiles.get(eid).get(number);
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
        this.nondeterministicReplicatedEnemyProjectiles.put(pid,new HashMap<>());
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
     * Returns a number between 0 and bound
     * @param bound Upper bound
     * @return int
     */
    public int getRandomInteger(int bound){
       if (this.random != null){
           return random.nextInt(bound);
       }
       return 0;
    }

    /**
     * Returns a random number generator.
     * This number generator is synced to all clients.
     * @return Random
     */
    public Random getRandom(){
        return this.random;
    }

}
