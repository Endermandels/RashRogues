package Networking;

public enum PacketType {
    WELCOME(21),
    FAREWELL(22),
    CREATE(23),
    UPDATE(24),
    DESTROY(25),
    START_GAME(26),
    CREATE_PLAYER(27),
    UPDATE_PLAYER(28),
    KEYS(29),
    UPDATE_PLAYER_POSITION(30),
    HEARTBEAT(31),
    CLIENT_SHARE(32),
    CLIENT_UPDATE(33),
    DESTROY_PLAYER(34),
    DESTROY2(35),
    RANDOM_SEED(36),
    DESTROY3(37),
    KILL_PLAYER(38),
    COMMAND(39),
    PICKUP_KEY(40),
    MERCHANT(41),
    UPGRADE(42),
    SET_TARGET(43),
    DESTROY4(44),
    DROP_KEY(45),
    COINS(46),
    HEALTH(47),
    KILL_ENEMY(48);

    private final int value;
    PacketType(int value){
        this.value=value;
    }
    public int getvalue(){
        return value;
    }
}
