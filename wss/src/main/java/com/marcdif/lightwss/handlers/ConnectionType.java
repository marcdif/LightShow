package com.marcdif.lightwss.handlers;

public enum ConnectionType {
    LIGHTSERVER, WEBCLIENT, UNKNOWN;

    public static ConnectionType fromString(String s) {
        for (ConnectionType type : ConnectionType.values()) {
            if (type.name().equalsIgnoreCase(s)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
