package com.marcdif.lightwss.packets;

import com.google.gson.JsonObject;

public abstract class BasePacket {
    protected int id = 0;

    public int getID() {
        return id;
    }

    public abstract BasePacket fromJSON(JsonObject obj);

    public abstract JsonObject getJSON();
}
