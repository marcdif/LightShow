package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import com.marcdif.syncserver.handlers.ConnectionType;

import lombok.Getter;

public class ClientConnectPacket extends BasePacket {
    @Getter private final String clientId;
    @Getter private final ConnectionType connectionType;

    public ClientConnectPacket(JsonObject object) {
        super(PacketID.CLIENT_CONNECT.getId(), object);
        this.clientId = object.get("clientId").getAsString();
        this.connectionType = ConnectionType.fromString(object.get("connectionType").getAsString());
    }

    public ClientConnectPacket(String clientId, ConnectionType connectionType) {
        super(PacketID.CLIENT_CONNECT.getId(), null);
        this.clientId = clientId;
        this.connectionType = connectionType;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("clientId", this.clientId);
        object.addProperty("connectionType", this.connectionType.name());
        return object;
    }
}
