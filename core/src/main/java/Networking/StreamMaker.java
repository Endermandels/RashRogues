package Networking;

public class StreamMaker {
   public static byte[] farewell(){
      byte[] stream = new byte[2];
      stream[0] = (byte) PacketType.FAREWELL.getvalue();
      stream[1] = '*';
      return stream;
   }

   public static byte[] welcome(int pid){
      byte[] stream = new byte[3];
      stream[0] = (byte) PacketType.WELCOME.getvalue();
      stream[1] = (byte) pid;
      stream[2] = '*';
      return stream;
   }

   public static byte[] startGame(){
      byte[] stream = new byte[2];
      stream[0] = (byte) PacketType.START_GAME.getvalue();
      stream[1] = '*';
      return stream;
   }
}
