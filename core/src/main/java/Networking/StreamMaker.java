package Networking;

import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.nio.ByteBuffer;

public class StreamMaker {
   private static ByteBuffer longbuffer = ByteBuffer.allocate(Long.BYTES);
   private static ByteBuffer intbuffer = ByteBuffer.allocate(Integer.BYTES);


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

   public static byte[] destroyPlayer(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.DESTROY_PLAYER.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   public  static byte[] destroyEntity(int eid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.DESTROY.getvalue();

      byte[] eidBytes = intToBytes(eid);

      stream[1] = eidBytes[0];
      stream[2] = eidBytes[1];
      stream[3] = eidBytes[2];
      stream[4] = eidBytes[3];

      return stream;
   }

   /**
    * Destroys entity belonging to player 'pid', created on frame 'frame'
    * @param pid Player whom this entity belongs to.
    * @param frame Frame on which this entity was created (frame is relative to endpoint pid originated from).
    * @return
    */
   public static byte[] destroyEntity2(int pid, long frame){
      byte[] stream = new byte[128];

      stream[0] = (byte) PacketType.DESTROY2.getvalue();
      stream[1] = (byte) (pid); //truncate pid to a byte, cause we'll only have at max 4 players.
      stream[2] = (byte) (frame >> 56);
      stream[3] = (byte) (frame >> 48);
      stream[4] = (byte) (frame >> 40);
      stream[5] = (byte) (frame >> 32);
      stream[6] = (byte) (frame >> 24);
      stream[7] = (byte) (frame >> 16);
      stream[8] = (byte) (frame >> 8);
      stream[9] = (byte) (frame);

      return stream;
   }

   /**
    * Destroys the number'th projectile belonging to player 'pid'.
    * @param pid
    * @param number
    * @return
    */
   public static byte[] destroyProjectile(int pid, long number){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.DESTROY3.getvalue();
      stream[1] = (byte) (pid); //truncate pid to a byte, cause we'll only have at max 4 players.
      stream[2] = (byte) (number >> 56);
      stream[3] = (byte) (number >> 48);
      stream[4] = (byte) (number >> 40);
      stream[5] = (byte) (number >> 32);
      stream[6] = (byte) (number >> 24);
      stream[7] = (byte) (number >> 16);
      stream[8] = (byte) (number >> 8);
      stream[9] = (byte) (number);
      return stream;
   }

   public static byte[] killPlayer(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.KILL_PLAYER.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   public static byte[] updatePlayer(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.UPDATE_PLAYER.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   public static byte[] keys(int pid, long frame, byte[] keymask){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.KEYS.getvalue();
      stream[1] = (byte) pid;
      stream[2] = (byte) (frame >> 56);
      stream[3] = (byte) (frame >> 48);
      stream[4] = (byte) (frame >> 40);
      stream[5] = (byte) (frame >> 32);
      stream[6] = (byte) (frame >> 24);
      stream[7] = (byte) (frame >> 16);
      stream[8] = (byte) (frame >> 8);
      stream[9] = (byte) (frame);
      System.arraycopy(keymask, 0, stream, 10, keymask.length);
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

   public static byte[] notifyClientUpdate(int pid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.CLIENT_UPDATE.getvalue();
      stream[1] = (byte) pid;
      return stream;
   }

   /**
    * The current list of clients.
    * byte 1: how many clients we are sharing
    * byte 2-N: client PIDs
    */
   public static byte[] getClients(){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.CLIENT_SHARE.getvalue();
      stream[1] = (byte) RRGame.globals.clientSet.size();
      int i = 2;
      for (int c : RRGame.globals.clientSet){
         stream[i] = (byte) c;
         i++;
      }
      return stream;
   }

   /**
    * Specify the target for an NPC
    * @param pid Player whom NPC should target
    * @param eid ID of NPC
    * @return
    */
   public static byte[] target(int pid, int eid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.SET_TARGET.getvalue();
      stream[1] = (byte) pid;
      byte[] eidBytes = intToBytes(eid);
      for (int i = 0; i < eidBytes.length; i++){
         stream[2+i] = eidBytes[i];
      }
      return stream;
   }

   /**
    * Specify the seed to use for random operations.
    * @param seed
    * @return
    */
   public static byte[] seed(long seed){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.RANDOM_SEED.getvalue();
      byte[] seedBytes = longToBytes(seed);
      stream[1] = seedBytes[0];
      stream[2] = seedBytes[1];
      stream[3] = seedBytes[2];
      stream[4] = seedBytes[3];
      stream[5] = seedBytes[4];
      stream[6] = seedBytes[5];
      stream[7] = seedBytes[6];
      stream[8] = seedBytes[7];
      return stream;
   }

   public static byte[] intToBytes(int i){
      intbuffer.clear();
      intbuffer.putInt(i);
      return intbuffer.array();
   }

   public static int bytesToInt(byte[] bytes){
      intbuffer.clear();
      intbuffer.put(bytes, 0, bytes.length);
      intbuffer.flip();
      return intbuffer.getInt();
   }

   public static byte[] longToBytes(long l){
      longbuffer.clear();
      longbuffer.putLong(0, l);
      return longbuffer.array();
   }

   public static long bytesToLong(byte[] bytes){
      longbuffer.clear();
      longbuffer.put(bytes, 0, bytes.length);
      longbuffer.flip();
      return longbuffer.getLong();
   }


}