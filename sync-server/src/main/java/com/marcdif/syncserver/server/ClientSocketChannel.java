package com.marcdif.syncserver.server;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.marcdif.syncserver.handlers.ConnectionType;
import com.marcdif.syncserver.packets.BasePacket;

public class ClientSocketChannel extends NioSocketChannel {
    private static final AtomicLong nextId = new AtomicLong(0L);
    @Getter protected long id = nextId.getAndIncrement();
    @Getter private final UUID connectionID = UUID.randomUUID();
    @Getter private final long connectTime = System.currentTimeMillis();

    @Getter @Setter private ConnectionType type = ConnectionType.UNKNOWN;
    @Getter @Setter private boolean synchronizing = true;
    @Getter @Setter private String clientId;

    public ClientSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public ClientSocketChannel(SocketChannel socket) {
        super(socket);
    }

    public ClientSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    public void send(String message) {
        writeAndFlush(new TextWebSocketFrame(message));
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }
}
