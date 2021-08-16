package com.marcdif.lightwss.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marcdif.lightwss.packets.BasePacket;
import com.marcdif.lightwss.utils.Logging;
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
                object = (JsonObject) new JsonParser().parse(request);
            } catch (Exception e) {
                Logging.warn("Error processing packet [" + request + "] from " +
                        ((io.netty.channel.socket.SocketChannel) ctx).localAddress());
                return;
            }
            if (!object.has("id")) {
                return;
            }
            int id = object.get("id").getAsInt();
            if (id != 43) {
                Logging.debug(object.toString());
            }
            ClientSocketChannel channel = (ClientSocketChannel) ctx.channel();
//            switch (id) {
//            }
        } catch (Exception e) {
            Logging.error("Error processing incoming packet", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ClientSocketChannel dash = (ClientSocketChannel) ctx.channel();
        // when client disconnects
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logging.error("WebSocket exception", cause);
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
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

    private void sendAll(BasePacket packet) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            ClientSocketChannel dash = (ClientSocketChannel) o;
            try {
                dash.send(packet);
            } catch (Exception e) {
                Logging.error("Error sending packet", e);
            }
        }
    }
}
