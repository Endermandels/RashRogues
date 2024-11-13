package Networking;

import io.github.RashRogues.EntityType;

public class PacketUpdate implements Packet {
    public final PacketType packetType = PacketType.UPDATE;
    public final EntityType entityType;
    public final int uid;
    public final int x;
    public final int y;
    public final String texture;

    /**
     * Update the entity's texture, x and y positions.
     * @param entityType The type of entity you are updating
     * @param uid The unique identifier for the entity you are updating.
     * @param texture Texture to apply. To not apply a texture, set this variable to ""
     * @param x X position to set the player to. To not change the x position, set this variable to -1
     * @param y Y position to set the player to. To not change the y position, set this variable to -1
     */
    public PacketUpdate(EntityType entityType, int uid, String texture, int x, int y){
        this.entityType = entityType;
        this.texture = texture;
        this.uid = uid;
        this.x = x;
        this.y = y;
    }

    /**
     * Update the entity's texture.
     * @param entityType The type of entity you are updating
     * @param uid The unique identifier for the entity you are updating.
     * @param texture Texture to apply. To not apply a texture, set this variable to ""
     */
    public PacketUpdate(EntityType entityType, int uid, String texture){
        this.entityType = entityType;
        this.texture=texture;
        this.uid = uid;
        this.x=-1;
        this.y=-1;
    }

    /**
     * Update the entity's x and y position.
     * @param entityType The type of entity you are updating
     * @param uid The unique identifier for the entity you are updating.
     * @param x X position to set the player to. To not change the x position, set this variable to -1
     * @param y Y position to set the player to. To not change the y position, set this variable to -1
     */
    public PacketUpdate(EntityType entityType, int uid, int x, int y){
        this.entityType = entityType;
        this.uid = uid;
        this.texture="";
        this.x=x;
        this.y=y;
    }

    public PacketType getPacketType(){
        return this.packetType;
    }

    public EntityType getEntityType(){
        return this.entityType;
    }
}
