package io.github.RashRogues;

import Networking.Endpoint;
import Networking.Network;

import java.util.HashMap;
import java.util.HashSet;

public class Globals {
    public static int PAROLE_TIME = 3;
    public static Network network;
    public RRScreen currentScreen;
    public HashMap<Integer, Player> players = new HashMap<>();
    public HashMap<Integer,Entity> entities = new HashMap<>();
    public HashSet<Player> playersSet = new HashSet<>();
    public HashSet<Integer> clientSet = new HashSet<>();
    public int currentNumPlayers = 0;
    public int pid = -1;
    private int entityIncrementor = 0;

    public void registerEntity(Entity e, boolean networked){
        if (networked){
            entities.put(entityIncrementor, e);
            e.id = entityIncrementor;
            entityIncrementor+=1;

        }
        RRGame.globals.currentScreen.registerEntity(e);
    }

    public void deregisterEntity(Entity e){
        if (this.pid == 0){
            Globals.network.connection.dispatchDestroyEntity(e.id);
        }
        RRGame.globals.currentScreen.removeEntity(e);
    }

    public Entity getRegisteredEntity(int id){
        if (this.entities.containsKey(id)){
            return this.entities.get(id);
        }
        return null;
    }

    public void addPlayer(int pid, Player player){
        RRGame.globals.players.put(pid,player);
        RRGame.globals.playersSet.add(player);
    }

    public void addClient(int pid){
        RRGame.globals.clientSet.add(pid);
    }

    public void removePlayer(int pid){
        Player p = RRGame.globals.players.get(pid);
        RRGame.globals.players.remove(pid);
        RRGame.globals.playersSet.remove(p);
    }

    public void removeClient(int pid){
        RRGame.globals.clientSet.remove(pid);
    }
}
