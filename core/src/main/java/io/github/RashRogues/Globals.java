package io.github.RashRogues;

import java.util.HashMap;
import java.util.HashSet;

public class Globals {
    public RRScreen currentScreen;
    public HashMap<Integer, Player> players = new HashMap<>();
    public HashSet<Player> playersSet = new HashSet<>();
    public HashSet<Integer> clientSet = new HashSet<>();
    public int currentNumPlayers = 0;
    public int pid = -1;

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
