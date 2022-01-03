package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class ServerStatusPacket extends BasePacket {
    @Getter private final boolean lightServerConnected;

    public ServerStatusPacket(JsonObject object) {
        super(PacketID.SERVER_STATUS.getId(), object);
        this.lightServerConnected = object.get("lightServerConnected").getAsBoolean();
    }

    public ServerStatusPacket(boolean lightServerConnected) {
        super(PacketID.SERVER_STATUS.getId(), null);
        this.lightServerConnected = lightServerConnected;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("lightServerConnected", this.lightServerConnected);
        return object;
    }
}
