package com.marcdif.syncserver.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PacketID {
    HEARTBEAT(0), GET_TIME(1), CONFIRM_SYNC(2), CLIENT_CONNECT(3), START_SONG(4), STOP_SONG(5), START_SHOW(6), STOP_SHOW(7), SERVER_STATUS(8);

    @Getter private final int id;
}
