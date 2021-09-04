package com.marcdif.lightwss.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class ClientConnectPacket extends BasePacket {
    @Getter private final String clientId;

    public ClientConnectPacket(JsonObject object) {
        super(PacketID.CLIENT_CONNECT.getId(), object);
        this.clientId = object.get("clientId").getAsString();
    }

    public ClientConnectPacket(String clientId) {
        super(PacketID.CLIENT_CONNECT.getId(), null);
        this.clientId = clientId;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("clientId", this.clientId);
        return object;
    }
}
