package com.marcdif.lightwss.server;

import com.marcdif.lightwss.utils.Logging;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.SocketChannel;
import java.util.List;

public class WebSocketServerSocketChannel extends NioServerSocketChannel {

    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = javaChannel().accept();
        try {
            if (ch != null) {
                buf.add(new ClientSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            Logging.error("Failed to create a new channel from an accepted socket", t);
            try {
                ch.close();
            } catch (Throwable t2) {
                Logging.error("Failed to close a socket", t2);
            }
        }

        return 0;
    }
}
