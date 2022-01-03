package com.marcdif.ledagent.wss.packets;

import com.google.gson.JsonObject;

public class StopSongPacket extends BasePacket {

    public StopSongPacket(JsonObject object) {
        super(PacketID.STOP_SONG.getId(), object);
    }

    public StopSongPacket() {
        super(PacketID.STOP_SONG.getId(), null);
    }

    @Override
    public JsonObject getJSON() {
        return getBaseJSON();
    }
}
