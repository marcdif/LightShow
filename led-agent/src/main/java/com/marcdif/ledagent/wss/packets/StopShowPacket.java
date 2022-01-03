package com.marcdif.ledagent.wss.packets;

import com.google.gson.JsonObject;

public class StopShowPacket extends BasePacket {

    public StopShowPacket(JsonObject object) {
        super(PacketID.STOP_SHOW.getId(), object);
    }

    public StopShowPacket() {
        super(PacketID.STOP_SHOW.getId(), null);
    }

    @Override
    public JsonObject getJSON() {
        return getBaseJSON();
    }
}
