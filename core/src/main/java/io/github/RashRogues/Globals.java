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
        clientSet.add(pid);
    }
}
