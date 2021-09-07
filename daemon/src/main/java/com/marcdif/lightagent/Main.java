package com.marcdif.lightagent;

public class Main {
    public static final String HOME_PATH = "/home/pi";

    public static void main(String[] args) throws Exception {
        logMessage("[INFO] Starting up at " + System.currentTimeMillis() + "...");
        LightWSSConnection connection = new LightWSSConnection();
    }

    public static void logMessage(String msg) {
        System.out.println(msg);
    }
}
