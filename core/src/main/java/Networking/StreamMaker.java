package Networking;

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


}