package Networking;

import io.github.RashRogues.Player;

import java.nio.ByteBuffer;

public class StreamMaker {
   public static byte[] farewell(){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.FAREWELL.getvalue();
      stream[1] = '*';
      return stream;
   }

   public static byte[] welcome(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.WELCOME.getvalue();
      stream[1] = (byte) pid;
      stream[2] = '*';
      return stream;
   }

   public static byte[] startGame(){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.START_GAME.getvalue();
      stream[1] = '*';
      return stream;
   }

   public static byte[] createPlayer(int pid, int x, int y) {
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.CREATE_PLAYER.getvalue();
      stream[1] = (byte) pid;
      stream[2] = (byte) (x >> 24);
      stream[3] = (byte) (x >> 16);
      stream[4] = (byte) (x >> 8);
      stream[5] = (byte) (x);
      stream[6] = (byte) (y >> 24);
      stream[7] = (byte) (y >> 16);
      stream[8] = (byte) (y >> 8);
      stream[9] = (byte) (y);

      return stream;
   }

   public static byte[] updatePlayer(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.UPDATE_PLAYER.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   public static byte[] keys(int pid, byte[] keymask){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.KEYS.getvalue();
      stream[1] = (byte) pid;
      for (int i = 0; i < keymask.length; i++){
         stream[i+2] = keymask[i];
      }
      return stream;
   }

   public static byte[] playerPosition(Player player, int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.UPDATE_PLAYER_POSITION.getvalue();
      stream[1] = (byte) pid;

      ByteBuffer bufferX = ByteBuffer.allocate(4);
      ByteBuffer bufferY = ByteBuffer.allocate(4);

      bufferX.putFloat(player.getX());
      bufferY.putFloat(player.getY());

      byte[] arrayX = bufferX.array();
      byte[] arrayY = bufferY.array();

      stream[2] = arrayX[0];
      stream[3] = arrayX[1];
      stream[4] = arrayX[2];
      stream[5] = arrayX[3];

      stream[6] = arrayY[0];
      stream[7] = arrayY[1];
      stream[8] = arrayY[2];
      stream[9] = arrayY[3];

      return stream;
   }

   public static byte[] heartbeat(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.HEARTBEAT.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   public static byte[] newClientNotification(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.NEW_CLIENT.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

}