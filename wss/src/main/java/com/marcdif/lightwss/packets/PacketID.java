package com.marcdif.lightwss.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PacketID {
    GET_TIME(1), CONFIRM_SYNC(2);

    @Getter private final int id;
}
