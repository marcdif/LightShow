package com.marcdif.ledagent;

import com.marcdif.ledagent.lights.LightManager;
import com.marcdif.ledagent.wss.LightWSSConnection;
import com.marcdif.ledagent.wss.packets.BasePacket;
import lombok.Getter;

public class Main {
    @Getter private static LightManager lightManager;
    @Getter private static LightWSSConnection connection;

    public static final boolean DEBUG = false, DEBUG_VERBOSE = false;

    public static void main(String[] args) throws Exception {
        logMessage("[INFO] Starting up at " + System.currentTimeMillis() + "...");
        lightManager = new LightManager();
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
