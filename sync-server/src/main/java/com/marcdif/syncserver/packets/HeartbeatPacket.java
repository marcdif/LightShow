package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;

public class HeartbeatPacket extends BasePacket {

    public HeartbeatPacket(JsonObject object) {
        super(PacketID.HEARTBEAT.getId(), object);
    }

    public HeartbeatPacket() {
        super(PacketID.HEARTBEAT.getId(), null);
    }

    @Override
    public JsonObject getJSON() {
        return getBaseJSON();
    }
}
