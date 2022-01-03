package com.marcdif.syncserver.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logging {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-d HH:mm:ss.SSS");
    private static final boolean DEBUG = System.getenv().getOrDefault("WSS_DEBUG", "false").equalsIgnoreCase("true");

    public static void print(String text) {
        print("INFO", text, null);
    }

    public static void print(String prefix, String text, Throwable t) {
        if (prefix.trim().isEmpty()) prefix = "INFO";
        // [2021-08-15 21:42:03] [MainThread]: This is a message
        System.out.println("[" + FORMAT.format(new Date()) + "] [" + prefix + "] [" + Thread.currentThread().getName() + "]: " +
                text + (t == null ? "" : " (" + t.getMessage() + ")"));
        if (t != null) t.printStackTrace(System.out);
    }

    public static void warn(String text) {
        print("WARN", text, null);
    }

    public static void error(String text, Throwable t) {
        print("ERROR", text, t);
    }

    public static void debug(String text) {
        if (DEBUG) print("DEBUG", text, null);
    }
}
