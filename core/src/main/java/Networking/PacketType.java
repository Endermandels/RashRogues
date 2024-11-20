package Networking;

public enum PacketType {
    WELCOME(1),
    FAREWELL(2),
    CREATE(3),
    UPDATE(4),
    DESTROY(5),
    START_GAME(6);

    private final int value;
    PacketType(int value){
        this.value=value;
    }
    public int getvalue(){
        return value;
    }
}
