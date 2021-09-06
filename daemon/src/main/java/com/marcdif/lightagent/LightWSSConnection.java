package com.marcdif.lightagent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marcdif.lightagent.packets.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class LightWSSConnection {
        private static final String socketURL = "ws://home.marcdif.com:3926";
//    private static final String socketURL = "ws://192.168.10.42:3926";

    //    private String clientId = null;
    private WebSocketClient ws;
    private boolean synchronizing = false;
    private long syncStartLocalTime = 0;
    private long syncServerTimeOffset = 0;
    private boolean syncDone = false;

    private Thread agentThread;
    private long showStartTime = 0;
    private String showName = "";
    private boolean showRunning = false;

    public LightWSSConnection() {
        start();
    }

    private void start() {
        if (ws != null) {
            ws.close();
            ws = null;
        }
        try {
            ws = new WebSocketClient(new URI(socketURL), new Draft_6455()) {
                @Override
                public void onMessage(String message) {
                    JsonObject object = (JsonObject) JsonParser.parseString(message);
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

                            Main.logMessage("[INFO] Received request to start " + packet.getShowName() + " - starting in 5 seconds...");
                            showStartTime = System.currentTimeMillis() + 5000 - syncServerTimeOffset;
                            showName = packet.getShowName();

                            if (agentThread != null && agentThread.isAlive()) {
                                //noinspection deprecation
                                agentThread.stop();
                                agentThread = null;
                            }
                            startAgent();
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

//                            System.out.println("A");
//                            String command = "../agent/.venv/Scripts/python -i ../agent/main.py";
//                            try {
//                                System.out.println("B");
//                                Process process = Runtime.getRuntime().exec(command);
//                                System.out.println("C");
//
//                                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//                                while (process.isAlive()) {
//                                    System.out.println("E");
//                                    String output = in.readLine();
//                                    if (output != null) {
//                                        System.out.println("value is : " + output);
//                                        if (output.startsWith("Press Enter to start the show...")) {
//                                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
//                                            System.out.println("Starting show...");
//                                            writer.write("abc");
//                                            writer.write("\n\n\n");
//                                            writer.flush();
////                                            writer.close();
//                                            System.out.println("Started show!");
//                                        }
//                                    } else {
//                                        System.out.println("output null");
//                                    }
//                                }
//                                System.out.println("D");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
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
        return ws != null && ws.getConnection() != null && ws.getConnection().isOpen();
    }

    public void stop() {
        if (ws == null) return;
        ws.close();
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }

    public void startAgent() {
        agentThread = new Thread(() -> {
            Main.logMessage("[AgentThread] Starting python agent...");
            System.out.println("A");
            String command = "../agent/.venv/Scripts/python -i ../agent/main.py -s " + showName;
            try {
                System.out.println("B");
                Process process = Runtime.getRuntime().exec(command);
                showRunning = true;
                System.out.println("C");

                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

                while (process.isAlive()) {
                    System.out.println("E");
                    String output = in.readLine();
                    if (output != null) {
                        System.out.println("value is : " + output);
                        if (output.startsWith("Audio: ")) {
                            String[] audio = output.split(" ");
                            if (!audio[1].equals("Unknown")) {
                                String songPath = audio[1];
                                int duration = Integer.parseInt(audio[2]) * 1000;
                                StartSongPacket startSongPacket = new StartSongPacket(songPath, showStartTime, duration, showName);
                                Main.logMessage("[INFO] Sending out StartSongPacket to start " + audio[1] + " (duration: " + duration + "s) in " + ((showStartTime - System.currentTimeMillis()) / 1000D) + " seconds");
                                LightWSSConnection.this.send(startSongPacket);
                            }
                        } else if (output.startsWith("Press Enter to start the show...")) {
                            try {
                                //noinspection BusyWait
                                Thread.sleep((showStartTime - System.currentTimeMillis()) + syncServerTimeOffset);
                                System.out.println("Starting show...");
                                writer.write("\n");
                                writer.flush();
                                System.out.println("Started show!");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (output.startsWith("Show ended")) {
                            StopSongPacket stopSongPacket = new StopSongPacket();
                            Main.logMessage("[INFO] Show has ended, sending out StopSongPacket");
                            LightWSSConnection.this.send(stopSongPacket);
                        }
                    } else {
                        System.out.println("output null");
                    }
                }
                System.out.println("D");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        agentThread.start();
    }
}
