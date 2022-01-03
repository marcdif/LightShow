package com.marcdif.syncserver.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

public class StartSongPacket extends BasePacket {
    @Getter private final String songPath, showName;
    @Getter private final long startTime, songDuration;

    public StartSongPacket(JsonObject object) {
        super(PacketID.START_SONG.getId(), object);
        this.songPath = object.get("songPath").getAsString();
        this.startTime = object.get("startTime").getAsLong();
        this.songDuration = object.get("songDuration").getAsLong();
        this.showName = object.get("showName").getAsString();
    }

    public StartSongPacket(String songPath, long startTime, long songDuration, String showName) {
        super(PacketID.START_SONG.getId(), null);
        this.songPath = songPath;
        this.startTime = startTime;
        this.songDuration = songDuration;
        this.showName = showName;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject object = getBaseJSON();
        object.addProperty("songPath", this.songPath);
        object.addProperty("startTime", this.startTime);
        object.addProperty("songDuration", this.songDuration);
        object.addProperty("showName", this.showName);
        return object;
    }
}
