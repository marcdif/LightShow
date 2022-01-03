package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class StartShowPacket extends BasePacket {
    @Getter private final String showName;

    public StartShowPacket(JsonObject object) {
        super(PacketID.START_SHOW.getId(), object);
        this.showName = object.get("showName").getAsString();
    }

    public StartShowPacket(String showName) {
        super(PacketID.START_SHOW.getId(), null);
        this.showName = showName;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("showName", this.showName);
        return object;
    }
}
