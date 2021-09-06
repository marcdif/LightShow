package com.marcdif.lightwss;

import com.marcdif.lightwss.packets.HeartbeatPacket;
import com.marcdif.lightwss.packets.StartShowPacket;
import com.marcdif.lightwss.packets.StartSongPacket;
import com.marcdif.lightwss.server.ClientSocketChannel;
import com.marcdif.lightwss.server.WebSocketServerHandler;
import com.marcdif.lightwss.server.WebSocketServerInitializer;
import com.marcdif.lightwss.server.WebSocketServerSocketChannel;
import com.marcdif.lightwss.utils.Logging;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static final String HOST = "0.0.0.0";
    public static final int PORT = 3926; // nginx listens on 3925, server on 3926

    /**
     * The name of the active song, or an empty string if nothing is playing.
     */
    @Getter @Setter public static String ACTIVE_SHOW = "";
    /**
     * The name of the active song, or an empty string if nothing is playing.
     */
    @Getter @Setter public static String ACTIVE_SONG = "";
    /**
     * The time the active song started in unix milliseconds, or 0 if nothing is playing.
     */
    @Getter @Setter public static long ACTIVE_SONG_START_TIME = 0;
    /**
     * The duration of the active song in milliseconds, or 0 if nothing is playing.
     */
    @Getter @Setter public static long ACTIVE_SONG_DURATION = 0;

    public static void main(String[] args) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                HeartbeatPacket pk = new HeartbeatPacket();
                for (Channel ch : WebSocketServerHandler.getGroup()) {
                    ((ClientSocketChannel) ch).send(pk);
                }
            }
        };
        new Timer("HeartbeatTimer").scheduleAtFixedRate(timerTask, 10000, 10000);

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

    public static boolean hasActiveSong() {
        // skip if no active song, or current song is over
        return !ACTIVE_SONG.isEmpty() && (System.currentTimeMillis() - ACTIVE_SONG_START_TIME) < ACTIVE_SONG_DURATION;
    }

    public static void sendSongStart(ClientSocketChannel channel) {
        if (!hasActiveSong()) return;
        StartSongPacket packet = new StartSongPacket(ACTIVE_SONG, ACTIVE_SONG_START_TIME, ACTIVE_SONG_DURATION, ACTIVE_SHOW);
        channel.send(packet);
    }

    public static void processShowRequest(String showName) {
        StartShowPacket packet = new StartShowPacket(showName);
        for (Channel ch : WebSocketServerHandler.getGroup()) {
            ((ClientSocketChannel) ch).send(packet);
        }
    }

    public static void processShowStarting(StartSongPacket packet) {
        ACTIVE_SHOW = packet.getShowName();
        ACTIVE_SONG = packet.getSongPath();
        ACTIVE_SONG_START_TIME = packet.getStartTime();
        ACTIVE_SONG_DURATION = packet.getSongDuration();
        for (Channel ch : WebSocketServerHandler.getGroup()) {
            ((ClientSocketChannel) ch).send(packet);
        }
    }
}
