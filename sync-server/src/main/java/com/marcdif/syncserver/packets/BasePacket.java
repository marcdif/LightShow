package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public abstract class BasePacket {
    @Getter private int id = 0;

    protected BasePacket(int id, JsonObject object) {
        this.id = id;
        if (object != null && object.get("id").getAsInt() != id)
            throw new IllegalArgumentException("Packet id does not match!");
    }

    public BasePacket(JsonObject obj) {
    }

    public abstract JsonObject getJSON();

    protected JsonObject getBaseJSON() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        return object;
    }
}
