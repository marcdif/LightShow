package com.marcdif.lightwss;

import com.marcdif.lightwss.server.WebSocketServerInitializer;
import com.marcdif.lightwss.server.WebSocketServerSocketChannel;
import com.marcdif.lightwss.utils.Logging;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;

public class Main {
    public static final String HOST = "0.0.0.0";
    public static final int PORT = 3925;

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(WebSocketServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());
            Channel ch = b.bind(new InetSocketAddress(HOST, PORT)).sync().channel();
            Logging.print("LightWSS started at " + HOST + ":" + PORT);
            ch.closeFuture().sync();
        } catch (Exception e) {
            Logging.error("Error with web socket server", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
