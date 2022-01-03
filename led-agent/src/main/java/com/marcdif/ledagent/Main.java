package com.marcdif.ledagent;

import com.marcdif.ledagent.handlers.LightStrip;
import com.marcdif.ledagent.show.ShowManager;
import com.marcdif.ledagent.wss.LightWSSConnection;
import com.marcdif.ledagent.wss.packets.BasePacket;

import lombok.Getter;

public class Main {
    public static final String HOME_PATH = "/home/pi";
    @Getter private static ShowManager showManager;
    @Getter private static LightStrip lightStrip;

    private static LightWSSConnection connection;

    public static void main(String[] args) throws Exception {
        logMessage("[INFO] Starting up at " + System.currentTimeMillis() + "...");
        showManager = new ShowManager();
        lightStrip = new LightStrip();
        connection = new LightWSSConnection();
    }

    public static void logMessage(String msg) {
        System.out.println(msg);
    }

    public static long getSyncServerTimeOffset() {
        if (connection == null) return -1;
        return connection.getSyncServerTimeOffset();
    }

    public static void sendPacket(BasePacket packet) {
        connection.send(packet);
    }
}
