package Networking;

import io.github.RashRogues.EntityType;

public class PacketStartGame implements Packet {
    public final PacketType packetType = PacketType.START_GAME;
    public PacketType getPacketType(){
        return this.packetType;
    }
}
