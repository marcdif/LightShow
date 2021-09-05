package com.marcdif.lightagent.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class StartSongPacket extends BasePacket {
    @Getter private final String songPath;
    @Getter private final long startTime, songDuration;

    public StartSongPacket(JsonObject object) {
        super(PacketID.START_SONG.getId(), object);
        this.songPath = object.get("songPath").getAsString();
        this.startTime = object.get("startTime").getAsLong();
        this.songDuration = object.get("songDuration").getAsLong();
    }

    public StartSongPacket(String songPath, long startTime, long songDuration) {
        super(PacketID.START_SONG.getId(), null);
        this.songPath = songPath;
        this.startTime = startTime;
        this.songDuration = songDuration;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("songPath", this.songPath);
        object.addProperty("startTime", this.startTime);
        object.addProperty("songDuration", this.songDuration);
        return object;
    }
}
