package com.marcdif.lightagent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marcdif.lightagent.packets.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class LightWSSConnection {
    private static final String socketURL = "ws://home.marcdif.com:3926";

    //    private String clientId = null;
    private WebSocketClient ws;
    private boolean synchronizing = false;
    private long syncStartLocalTime = 0;
    private long syncServerTimeOffset = 0;
    private boolean syncDone = false;

    public LightWSSConnection() {
        start();
    }

    private void start() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
        try {
            ws = new WebSocketClient(new URI(socketURL), new Draft_10()) {
                @Override
                public void onMessage(String message) {
                    JsonObject object = (JsonObject) new JsonParser().parse(message);
                    if (!object.has("id")) {
                        return;
                    }
                    int id = object.get("id").getAsInt();
                    Main.logMessage("Incoming: " + object);
                    switch (id) {
                        case 1: {
                            if (syncStartLocalTime == 0 || !synchronizing) {
                                Main.logMessage("Not handling GET_TIME packet - haven't started a sync process!");
                                return;
                            }
                            GetTimePacket packet = new GetTimePacket(object);
                            long receivedTime = System.currentTimeMillis();
                            long difference = receivedTime - syncStartLocalTime;
                            syncServerTimeOffset = syncStartLocalTime - (packet.getServerTime() - (difference / 2));

                            Main.logMessage("Response took " + difference + "ms... setting syncServerTimeOffset to " + syncServerTimeOffset);

                            long currentServerTime = System.currentTimeMillis() - syncServerTimeOffset;

                            Main.logMessage("Responding with server time being " + currentServerTime);
                            syncStartLocalTime = 0;

                            ConfirmSyncPacket response = new ConfirmSyncPacket(currentServerTime);
                            LightWSSConnection.this.send(response);
                            break;
                        }
                        case 2: {
                            if (!synchronizing) {
                                Main.logMessage("Not handling CONFIRM_SYNC Packet - haven't started a sync process!");
                                return;
                            }
                            synchronizing = false;
                            ConfirmSyncPacket packet = new ConfirmSyncPacket(object);
                            if (packet.getClientTime() >= 0) {
                                // accepted
                                Main.logMessage("[INFO] Sync succeeded! " + packet.getClientTime() + "ms offset");
                                syncStartLocalTime = 0;
                                syncDone = true;
                            } else {
                                // failed
                                Main.logMessage("[ERROR] Sync failed!");
                                syncServerTimeOffset = 0;
                                syncStartLocalTime = 0;
                            }
                            break;
                        }
                        case 4: {
                            if (!syncDone) {
                                Main.logMessage("[ERROR] Can't start a song, we aren't synchronized!");
                                return;
                            }
                            StartSongPacket packet = new StartSongPacket(object);
                            // start the song :shrug:
                            break;
                        }
                        case 5: {
                            if (!syncDone) {
                                Main.logMessage("[ERROR] Can't stop a song, we aren't synchronized!");
                                return;
                            }
                            // stop the song :shrug:
                            break;
                        }
                        case 6: {
                            if (!syncDone) {
                                Main.logMessage("[ERROR] Can't start a show, we aren't synchronized!");
                                return;
                            }
                            StartShowPacket packet = new StartShowPacket(object);

                            String command = "../python/.venv/Scripts/python ../python/main.py";
                            try {
                                Process p = Runtime.getRuntime().exec(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    Main.logMessage("[INFO] Successfully connected to LightWSS");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            synchronizing = true;
                            LightWSSConnection.this.send(new GetTimePacket(0));
                            syncStartLocalTime = System.currentTimeMillis();

                            System.out.println("A");
                            String command = "../python/.venv/Scripts/python -i ../python/main.py";
                            try {
                                System.out.println("B");
                                Process process = Runtime.getRuntime().exec(command);
                                System.out.println("C");

                                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

                                while (process.isAlive()) {
                                    System.out.println("E");
                                    String output = in.readLine();
                                    if (output != null) {
                                        System.out.println("value is : " + output);
                                        if (output.startsWith("Press Enter to start the show...")) {
                                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                                            System.out.println("Starting show...");
                                            writer.write("abc");
                                            writer.write("\n\n\n");
                                            writer.flush();
//                                            writer.close();
                                            System.out.println("Started show!");
                                        }
                                    } else {
                                        System.out.println("output null");
                                    }
                                }
                                System.out.println("D");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 3000L);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Main.logMessage("[ERROR] Disconnected from LightWSS! Reconnecting...");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            start();
                        }
                    }, 5000L);
                }

                @Override
                public void onError(Exception ex) {
                    Main.logMessage("[ERROR] Error in LightWSS connection");
                    ex.printStackTrace();
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ws.connect();
    }

    public void send(String s) {
        if (!isConnected()) {
            Main.logMessage("WebSocket disconnected, cannot send packet!");
            return;
        }
        Main.logMessage("Outgoing: " + s);
        ws.send(s);
    }

    public boolean isConnected() {
        return ws != null && ws.getConnection() != null && !ws.getConnection().isConnecting() && ws.getConnection().isOpen();
    }

    public void stop() {
        if (ws == null) return;
        ws.close();
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }
}
