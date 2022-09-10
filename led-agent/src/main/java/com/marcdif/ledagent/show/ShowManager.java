package com.marcdif.ledagent.show;

import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.wss.packets.StopSongPacket;

public class ShowManager {
    private static final long SHOW_START_OFFSET = 2000L;
    private ShowThread activeShowThread = null;

    private long showStartTime = 0;
    private String showName = "";
    protected boolean showRunning = false;

    public void startShow(String showName) {
        Main.logMessage("[INFO] Received request to start " + showName + " - starting in 2 seconds...");
        if (activeShowThread != null || !this.showName.isEmpty()) {
            Main.logMessage("[WARN] Stopping active show " + this.showName + "!!!");
            stopShow();
        }
        this.showName = showName;
        this.showStartTime = (System.currentTimeMillis() + SHOW_START_OFFSET) - Main.getSyncServerTimeOffset();
        Main.logMessage("[INFO] Setting start time for " + showName + " to " + this.showStartTime + " (currentTime: "
                + System.currentTimeMillis() + ", SHOW_START_OFFSET: " + SHOW_START_OFFSET + ", syncServerTimeOffset: "
                + Main.getSyncServerTimeOffset());

        try {
            activeShowThread = new ShowThread(showName, showStartTime);
            activeShowThread.start();
        } catch (Exception e) {
            Main.logMessage("[ERROR] Failed to start show: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopShow() {
        Main.logMessage("[INFO] Received request to stop the active show!");
        if (activeShowThread == null && this.showName.isEmpty()) {
            Main.logMessage("[WARN] There is no active show to stop!!!");
        } else {
            if (activeShowThread != null) activeShowThread.forceStopShow();
            activeShowThread = null;
            this.showName = "";
            this.showStartTime = 0;
            Main.logMessage("[INFO] Show has ended, sending out StopSongPacket");
            Main.sendPacket(new StopSongPacket());
        }
    }
}
