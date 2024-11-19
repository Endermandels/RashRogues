package Networking;

public class PacketWelcome implements Packet {
    public final PacketType packetType = PacketType.WELCOME;
    public final int pid;

    /**
     * Welcome a player to the game.
     * @param assignedPlayerID Player ID to assign to the new player. (equal to their index in the connected clients list)
     */
    public PacketWelcome(int assignedPlayerID){
        this.pid = assignedPlayerID;
    }

    public PacketType getPacketType(){
        return this.packetType;
    }
}
