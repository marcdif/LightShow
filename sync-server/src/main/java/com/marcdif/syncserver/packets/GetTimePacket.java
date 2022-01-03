package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class GetTimePacket extends BasePacket {
    @Getter private final long serverTime;

    public GetTimePacket(JsonObject object) {
        super(PacketID.GET_TIME.getId(), object);
        this.serverTime = object.get("serverTime").getAsLong();
    }

    public GetTimePacket(long serverTime) {
        super(PacketID.GET_TIME.getId(), null);
        this.serverTime = serverTime;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("serverTime", this.serverTime);
        return object;
    }
}
