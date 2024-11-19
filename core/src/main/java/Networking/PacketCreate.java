package Networking;

import io.github.RashRogues.EntityType;

public class PacketCreate implements Packet {
    public final PacketType packetType = PacketType.CREATE;
    public final EntityType entityType;
    public final String uid;
    public final String texture;
    public final int x;
    public final int y;

    /**
     * Create a new entity
     * @param entityType Type of entity to create (any class that inherits entity is viable)
     * @param uid Unique identifier for the entity
     * @param texture Texture to apply to new entity
     * @param x X coord to place the entity at
     * @param y Y coord to place the entity at
     */
    public PacketCreate(EntityType entityType, String uid, String texture, int x, int y){
        this.entityType = entityType;
        this.texture = texture;
        this.uid = uid;
        this.x = x;
        this.y = y;
    }

    public PacketType getPacketType(){
        return this.packetType;
    }

    public EntityType getEntityType(){
        return this.entityType;
    }
}
