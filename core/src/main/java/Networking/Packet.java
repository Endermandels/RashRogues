package Networking;

import java.io.Serializable;

public interface Packet extends Serializable {
  public PacketType getPacketType();
}