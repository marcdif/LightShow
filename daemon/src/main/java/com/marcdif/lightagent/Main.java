package com.marcdif.lightagent;

public class Main {

    public static void main(String[] args) throws Exception {
        logMessage("[INFO] Starting up...");
        LightWSSConnection connection = new LightWSSConnection();
    }

    public static void logMessage(String msg) {
        System.out.println(msg);
    }
}
