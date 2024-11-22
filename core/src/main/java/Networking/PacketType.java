package Networking;

public enum PacketType {
    WELCOME(21),
    FAREWELL(22),
    CREATE(23),
    UPDATE(24),
    DESTROY(25),
    START_GAME(26),
    CREATE_PLAYER(27),
    UPDATE_PLAYER(28);

    private final int value;
    PacketType(int value){
        this.value=value;
    }
    public int getvalue(){
        return value;
    }
}
