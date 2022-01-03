package com.marcdif.syncserver.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marcdif.syncserver.Main;
import com.marcdif.syncserver.handlers.ConnectionType;
import com.marcdif.syncserver.packets.*;
import com.marcdif.syncserver.utils.Logging;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private WebSocketServerHandshaker handshaker;

    public static ChannelGroup getGroup() {
        return channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(null, null, true, (int) Math.pow(2, 20));
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        try {
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (frame instanceof PongWebSocketFrame) {
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported",
                        frame.getClass().getName()));
            }
            String request = ((TextWebSocketFrame) frame).text();
            JsonObject object;
            try {
                object = (JsonObject) JsonParser.parseString(request);
            } catch (Exception e) {
                Logging.warn("Error processing packet [" + request + "] from " +
                        ctx.channel().localAddress());
                ctx.close();
                return;
            }
            if (!object.has("id")) {
                Logging.warn("Missing id field in packet [" + request + "] from " +
                        ctx.channel().localAddress());
                return;
            }
            int id = object.get("id").getAsInt();
            Logging.print(object.toString());
            ClientSocketChannel channel = (ClientSocketChannel) ctx.channel();
            if (channel.isSynchronizing() && (id > 2 || id < 1)) {
                Logging.warn("Non-sync packet before synced from " +
                        ctx.channel().localAddress());
                channel.send(new ConfirmSyncPacket(-1));
                return;
            }

            switch (id) {
                // Get Server Time
                case 1: {
                    channel.send(new GetTimePacket(System.currentTimeMillis()));
                    break;
                }
                // Confirm Time Sync
                case 2: {
                    long serverTime = System.currentTimeMillis();
                    ConfirmSyncPacket packet = new ConfirmSyncPacket(object);
                    long difference = Math.abs(packet.getClientTime() - serverTime);
                    if (difference <= 100) {
                        Logging.print("Successfully synced client " + ctx.channel().localAddress()
                                + " (" + difference + "ms offset) - " + channel.getConnectionID().toString());
                        channel.setSynchronizing(false);
                        channel.send(new ConfirmSyncPacket(difference));
                        Main.sendSongStart(channel);
//                        new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                StartShowPacket p = new StartShowPacket("gny");
//                                channel.send(p);
//                            }
//                        }, 3000L);
                    } else {
                        Logging.print("Failed to sync client " + ctx.channel().localAddress()
                                + " - " + channel.getConnectionID().toString() + " - " + difference + "ms difference");
                        channel.send(new ConfirmSyncPacket(-1));
                    }
                    break;
                }
                // Client Connect
                case 3: {
                    ClientConnectPacket packet = new ClientConnectPacket(object);
                    channel.setClientId(packet.getClientId());
                    channel.setType(packet.getConnectionType());
                    if (channel.getType().equals(ConnectionType.WEBCLIENT)) {
                        channel.send(new ServerStatusPacket(Main.isLightServerConnected()));
                    } else if (channel.getType().equals(ConnectionType.LIGHTSERVER)) {
                        Main.sendTo(new ServerStatusPacket(true), ConnectionType.WEBCLIENT);
                    }
                    break;
                }
                // Instruction from Agent to Start Song
                case 4: {
                    StartSongPacket packet = new StartSongPacket(object);
                    Main.processShowStarting(packet);
                    break;
                }
                // Instruction from Agent to Stop Song
                case 5:
                    // Request to Stop Show
                case 7: {
                    Main.stopShow();
                    break;
                }
                // Request to Start Show
                case 6: {
                    StartShowPacket packet = new StartShowPacket(object);
                    Main.processShowRequest(packet.getShowName());
                    break;
                }
            }
        } catch (Exception e) {
            Logging.error("Error processing incoming packet", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // when client disconnects
        super.channelActive(ctx);
        ClientSocketChannel client = (ClientSocketChannel) ctx.channel();
        if (client.getType().equals(ConnectionType.LIGHTSERVER)) {
            if (!Main.isLightServerConnected()) {
                Main.sendTo(new ServerStatusPacket(false), ConnectionType.WEBCLIENT);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause.getMessage().equals("Connection reset by peer")) {
            Logging.warn("Client disconnected: " + ctx.channel().localAddress().toString());
        } else {
            Logging.error("WebSocket exception", null);
        }
        ctx.close();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            try {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            } catch (Exception e) {
                Logging.error("Error reading websocket channel", e);
            }
        }
    }
}
