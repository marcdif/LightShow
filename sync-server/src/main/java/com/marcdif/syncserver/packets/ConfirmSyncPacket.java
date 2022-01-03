package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class ConfirmSyncPacket extends BasePacket {
    // Client to server: this field contains the client's estimate of the server time upon receipt
    // Server to client: 0 if client is accepted, -1 if denied
    @Getter private final long clientTime;

    public ConfirmSyncPacket(JsonObject object) {
        super(PacketID.CONFIRM_SYNC.getId(), object);
        this.clientTime = object.get("clientTime").getAsLong();
    }

    public ConfirmSyncPacket(long clientTime) {
        super(PacketID.CONFIRM_SYNC.getId(), null);
        this.clientTime = clientTime;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("clientTime", this.clientTime);
        return object;
    }
}
