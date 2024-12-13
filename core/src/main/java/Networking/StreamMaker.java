package Networking;

import io.github.RashRogues.BuyableItem;
import io.github.RashRogues.Player;
import io.github.RashRogues.RRGame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StreamMaker {
   private static ByteBuffer longbuffer;
   private static ByteBuffer intbuffer;
   private static ByteBuffer floatbuffer;

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
    * Destroy projectile # 'number' from entity 'eid'
    * @param eid
    * @param number
    * @return
    */
   public static byte[] destroyEntity3(int eid, long number){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.DESTROY4.getvalue();

      byte[] eidBytes = intToBytes(eid);
      stream[1] = eidBytes[0];
      stream[2] = eidBytes[1];
      stream[3] = eidBytes[2];
      stream[4] = eidBytes[3];

      byte[] numberBytes = longToBytes(number);
      stream[5] = numberBytes[0];
      stream[6] = numberBytes[1];
      stream[7] = numberBytes[2];
      stream[8] = numberBytes[3];
      stream[9] = numberBytes[4];
      stream[10] = numberBytes[5];
      stream[11] = numberBytes[6];
      stream[12] = numberBytes[7];

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

   public static byte[] pickupKey(int pid, int keyID){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.PICKUP_KEY.getvalue();
      stream[1] = (byte) pid;
      byte[] keyIDBytes = StreamMaker.intToBytes(keyID);
      stream[2] = keyIDBytes[0];
      stream[3] = keyIDBytes[1];
      stream[4] = keyIDBytes[2];
      stream[5] = keyIDBytes[3];

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

   public static byte[] keys(int pid, long frame, byte[] keymask, float x, float y, float mx, float my){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.KEYS.getvalue();
      stream[1] = (byte) pid;
      byte[] frameBytes = StreamMaker.longToBytes(frame);
      byte[] xBytes = StreamMaker.floatToBytes(x);
      byte[] yBytes = StreamMaker.floatToBytes(y);
      byte[] mxBytes = StreamMaker.floatToBytes(x);
      byte[] myBytes = StreamMaker.floatToBytes(y);

      System.arraycopy(frameBytes, 0, stream, 2, 8);
      System.arraycopy(keymask, 0, stream, 10, 7);
      System.arraycopy(xBytes, 0, stream, 64,4);
      System.arraycopy(yBytes, 0, stream, 68,4);
      System.arraycopy(mxBytes, 0, stream, 72,4);
      System.arraycopy(myBytes, 0, stream, 76,4);

      return stream;
   }

   public static byte[] dropCoins(float x, float y, int level){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.COINS.getvalue();
      byte[] xBytes = StreamMaker.floatToBytes(x);
      byte[] yBytes = StreamMaker.floatToBytes(y);
      byte[] levelBytes = StreamMaker.intToBytes(level);
      System.arraycopy(xBytes, 0, stream, 1,4);
      System.arraycopy(yBytes, 0, stream, 5,4);
      System.arraycopy(levelBytes, 0, stream, 9,4);
      return stream;
   }


   public static byte[] command(String[] cmd){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.COMMAND.getvalue();
      int tx = 1;
     for (int i = 0; i < cmd.length; i++){
        String cmdlet = cmd[i];
        byte[] cmdlet_bytes = cmdlet.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(cmdlet_bytes,0, stream, tx, cmdlet_bytes.length);
        if (tx > 1) {
           stream[tx - 1] = '#';
        }
        tx += cmdlet_bytes.length+1;
     }
     stream[tx-1] = '#';
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
    * Apply upgrade of type to player with pid
    * @param pid
    * @param type
    * @return
    */
   public static byte[] upgrade(int pid, BuyableItem type){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.UPGRADE.getvalue();
      stream[1] = (byte) pid;
      stream[2] = (byte) type.getvalue();
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
    * Tell the client to create a key at X and Y
    * @param x
    * @param y
    * @return
    */
   public static byte[] dropKey(float x, float y){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.DROP_KEY.getvalue();
      byte[] xBytes = StreamMaker.floatToBytes(x);
      floatbuffer = ByteBuffer.allocate(Float.BYTES);
      byte[] yBytes = StreamMaker.floatToBytes(y);

      stream[1] = xBytes[0];
      stream[2] = xBytes[1];
      stream[3] = xBytes[2];
      stream[4] = xBytes[3];

      stream[5] = yBytes[0];
      stream[6] = yBytes[1];
      stream[7] = yBytes[2];
      stream[8] = yBytes[3];

      float X = StreamMaker.bytesToFloat(xBytes);
      float Y = StreamMaker.bytesToFloat(yBytes);

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

   public static byte[] merchant(int pid, boolean enterOrLeave){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.MERCHANT.getvalue();
      stream[1] = (byte) pid;
      if (enterOrLeave){
         stream[2] = 1;
      }else{
         stream[2] = 0;
      }
      return stream;
   }

   public static byte[] killEnemy(int eid){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.KILL_ENEMY.getvalue();
      byte[] eidBytes = StreamMaker.intToBytes(eid);
      System.arraycopy(eidBytes,0,stream,1,4);
      return stream;
   }

   public static byte[] syncHealth(int pid, int hp){
      byte[] stream = new byte[128];
      stream[0] = (byte) PacketType.HEALTH.getvalue();
      stream[1] = (byte) pid;

      byte[] hpBytes = StreamMaker.intToBytes(hp);
      stream[2] = hpBytes[0];
      stream[3] = hpBytes[1];
      stream[4] = hpBytes[2];
      stream[5] = hpBytes[3];

      return stream;
   }

   public static byte[] intToBytes(int i){
      intbuffer = ByteBuffer.allocate(Integer.BYTES);
      intbuffer.putInt(i);
      return intbuffer.array();
   }

   public static int bytesToInt(byte[] bytes){
      intbuffer = ByteBuffer.allocate(Integer.BYTES);
      intbuffer.put(bytes, 0, bytes.length);
      intbuffer.flip();
      return intbuffer.getInt();
   }

   public static byte[] longToBytes(long l){
      longbuffer = ByteBuffer.allocate(Long.BYTES);
      longbuffer.putLong(0, l);
      return longbuffer.array();
   }

   public static long bytesToLong(byte[] bytes){
      longbuffer = ByteBuffer.allocate(Long.BYTES);
      longbuffer.put(bytes, 0, bytes.length);
      longbuffer.flip();
      return longbuffer.getLong();
   }

   public static float bytesToFloat(byte[] bytes){
      floatbuffer = ByteBuffer.allocate(Float.BYTES);
      floatbuffer.put(bytes,0,bytes.length);
      floatbuffer.flip();
      return floatbuffer.getFloat();
   }

   public static byte[] floatToBytes(float f){
      floatbuffer = ByteBuffer.allocate(Float.BYTES);
      floatbuffer.putFloat(0,f);
      return floatbuffer.array();
   }
}