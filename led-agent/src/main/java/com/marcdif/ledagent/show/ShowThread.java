package com.marcdif.ledagent.show;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.handlers.LEDStage;
import com.marcdif.ledagent.show.actions.*;
import com.marcdif.ledagent.utils.ColorUtil;
import com.marcdif.ledagent.utils.MathUtil;
import com.marcdif.ledagent.wss.packets.StartSongPacket;

public class ShowThread extends Thread {
    public static boolean DEBUG = false;
    private final boolean DEBUG_VERBOSE = false;
    @Getter private final String showName;
    @Getter private String showTitle = "Unknown";
    private final long startTime;
    @Getter LEDStage ledStage = new LEDStage(0, 1, 2, 3, 4);

    private ShowAction firstAction, nextAction = null;
    private List<ShowAction> runningActions = null;
    private double startTimeSeconds;
    private ScheduledFuture<?> scheduledFuture;

    public ShowThread(String showName, long startTime) {
        this.showName = showName;
        this.startTime = startTime;

        File file = new File(ShowManager.HOME_PATH + "/shows/" + showName + ".show");
        if (!file.exists()) {
            throw new IllegalArgumentException("Show file doesn't exist!");
        }

        if (DEBUG)
            Main.logMessage("Loading actions for " + showName + "...");
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            stream.forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long size = 0;
        String showAudio = "Unknown";

        ForAction forLoop = null;
        ShowAction forLoopFirstAction = null;

        for (String line : lines) {
            String[] tokens = line.split("\t");
            ShowAction nextAction = null;

            String first = tokens[0];

            if (first.startsWith("#")) {
                continue;
            } else if (first.equals("Show")) {
                switch (tokens[1]) {
                case "Name":
                    showTitle = tokens[2];
                    break;
                case "Audio":
                    showAudio = tokens[2];
                    break;
                case "Debug":
                    DEBUG = Boolean.parseBoolean(tokens[2]);
                    break;
                case "SetStage":
                    String[] list = tokens[2].split(",");
                    ledStage = new LEDStage(Integer.parseInt(list[0]), Integer.parseInt(list[1]),
                            Integer.parseInt(list[2]), Integer.parseInt(list[3]), Integer.parseInt(list[4]));
                    break;
                }
            } else if (MathUtil.isDouble(first)) {
                double t = Double.parseDouble(first);
                switch (tokens[1]) {
                case "Log":
                    nextAction = new LogAction(t, tokens[2]);
                    break;
                case "For":
                    if (!tokens[4].equals("{")) {
                        throw new ShowException("Missing { in 'For' action!");
                    }
                    forLoop = new ForAction(t, Integer.parseInt(tokens[2]), Double.parseDouble(tokens[3]));
                    break;
                case "FullLight":
                    nextAction = new FullLightAction(t, ColorUtil.getColor(tokens[2]));
                    break;
                case "FadeTo":
                    nextAction = new FadeToAction(t, ColorUtil.getColor(tokens[2]), Double.parseDouble(tokens[3]));
                    break;
                }
            } else if (first.equals("}")) {
                if (forLoopFirstAction == null) {
                    throw new ShowException("For loop must have at least 1 action!");
                }
                forLoop.setFirstAction(forLoopFirstAction);
                nextAction = forLoop;
                forLoop = null;
                forLoopFirstAction = null;
            }

            if (nextAction != null || (forLoop != null && forLoopFirstAction == null)) {
                ShowAction relativeFirstAction;
                if (forLoop != null) {
                    relativeFirstAction = forLoopFirstAction;
                } else {
                    relativeFirstAction = firstAction;
                }

                if (relativeFirstAction == null) {
                    if (forLoop != null) {
                        forLoopFirstAction = nextAction;
                    } else {
                        firstAction = nextAction;
                    }
                } else {
                    if (DEBUG)
                        Main.logMessage("  Processing " + nextAction);
                    // Sort as we go
                    ShowAction a = relativeFirstAction;
                    ShowAction last;
                    while (a.getNextAction() != null && a.getTime() < nextAction.getTime()) {
                        last = a;
                        a = a.getNextAction();
                        if (DEBUG)
                            Main.logMessage("    At " + a.toString());
                        if (a.getTime() > nextAction.getTime()) {
                            a = last;
                            if (DEBUG)
                                Main.logMessage("      Too far... jumping back to " + a);
                            break;
                        }
                    }

                    if (DEBUG)
                        Main.logMessage("      Setting parent to " + a);

                    // Insert our new action (nextAction) in between 'a' and its 'nextAction' (which
                    // may or may not be null)
                    ShowAction temp = a.getNextAction();
                    a.setNextAction(nextAction);
                    nextAction.setNextAction(temp);
                }
                size++;
            }
        }
        lines.clear();

        if (DEBUG)
            Main.logMessage("Finished loading " + size + " actions...");

        Main.logMessage("Audio: " + showAudio);
        String[] audio = showAudio.split(" ");
        String songPath = audio[0];
        int duration = Integer.parseInt(audio[1]) * 1000;
        StartSongPacket startSongPacket = new StartSongPacket(songPath, this.startTime, duration, showName);
        Main.logMessage("[INFO] Sending out StartSongPacket to start " + songPath + " (duration: " + duration
                + "ms) in " + ((this.startTime - System.currentTimeMillis()) / 1000D) + " seconds");
        Main.sendPacket(startSongPacket);
    }

    @Override
    public void run() {
        try {
            Main.logMessage("Waiting until " + (startTime) + " (server time) to start the show...");
            Thread.sleep((startTime + Main.getSyncServerTimeOffset()) - System.currentTimeMillis());
            Main.getShowManager().showRunning = true;
            if (DEBUG)
                Main.logMessage("Starting!!!");

            startTimeSeconds = System.currentTimeMillis() / 1000.0;

            runningActions = new ArrayList<>();

            nextAction = firstAction;

            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            scheduledFuture = executorService.scheduleAtFixedRate(() -> {
                if (DEBUG_VERBOSE) {
                    Main.logMessage(System.currentTimeMillis() + "");
                    Main.logMessage((System.currentTimeMillis() / 1000.0) + "");
                    Main.logMessage((System.currentTimeMillis() / 1000.0) + "");
                }
                double currentTime = System.currentTimeMillis() / 1000.0;
                if (DEBUG_VERBOSE)
                    Main.logMessage("Time: " + currentTime);
                // Calculate number of seconds we are into the show
                double timeDiff = currentTime - startTimeSeconds;

                // Array for storing any actions that are done
                List<ShowAction> done = new ArrayList<>();
                // Process all running actions
                for (ShowAction act : runningActions) {
                    // 1) If the action is done, add to the 'to remove' list...
                    if (act.isDone()) {
                        done.add(act);
                    } else {
                        // 2) otherwise run it.
                        // Note: Actions are responsible for tracking the interval they're supposed to
                        // run at.
                        // act.run() could be called 100 times per second.
                        // If the action is only meant to run 10 times per second, it should have a
                        // counter variable.
                        act.run();
                        if (DEBUG_VERBOSE)
                            Main.logMessage("Running " + act);
                        if (act.isDone())
                            done.add(act);
                    }
                }

                // Remove all done actions from runningActions
                for (ShowAction act : done) {
                    runningActions.remove(act);
                }
                done.clear();

                if (nextAction == null) {
                    Main.getLightStrip().render();
                    // Stop show if there are no actions left
                    if (runningActions.isEmpty()) {
                        Main.logMessage("[WARN] No more actions left, stopping show!");
                        Main.getShowManager().stopShow();
                        scheduledFuture.cancel(true);
                    }
                    return;
                }

                // 1) If it's time for the next action to start...
                while (nextAction != null && timeDiff >= nextAction.getTime()) {
                    // 2) add it to the list of RunningActions...
                    runningActions.add(nextAction);
                    // 3) and run it for the first time...
                    nextAction.run();
                    if (DEBUG)
                        Main.logMessage("Running " + nextAction);
                    // 4) and update nextaction to the next action.
                    nextAction = nextAction.getNextAction();
                    // 5) Continue looping until the next action shouldn't start yet.
                }
                Main.getLightStrip().render();
            }, 0, 10, TimeUnit.MILLISECONDS); // 100 times per second

            // while (true) {
            // long currentTime = System.currentTimeMillis();
            // long nextRun = (long) (startTime + ((count + 1) * 0.01));
            // // Only run every 0.01 seconds (100 times per second)
            // if (currentTime < nextRun) {
            // if (DEBUG_VERBOSE) Main.logMessage("Sleep for " + ((lastRun + 0.01) -
            // System.currentTimeMillis()));
            // sleep(nextRun - System.currentTimeMillis());
            // continue;
            // }
            //
            // lastRun = currentTime;
            // count++;
            //
            // // Calculate number of seconds we are into the show
            // long timeDiff = currentTime - startTime;
            //
            // // Array for storing any actions that are done
            // List<ShowAction> done = new ArrayList<>();
            // // Process all running actions
            // for (ShowAction act : runningActions) {
            // // 1) If the action is done, add to the 'to remove' list...
            // if (act.isDone()) {
            // done.add(act);
            // } else {
            // // 2) otherwise run it.
            // // Note: Actions are responsible for tracking the interval they're supposed
            // to run at.
            // // act.run() could be called 100 times per second.
            // // If the action is only meant to run 10 times per second, it should have a
            // counter variable.
            // act.run();
            // if (act.isDone()) done.add(act);
            // }
            // }
            //
            // // Remove all done actions from runningActions
            // for (ShowAction act : done) {
            // runningActions.remove(act);
            // }
            // done.clear();
            //
            // if (nextAction == null) {
            // // Stop show if there are no actions left
            // if (runningActions.isEmpty()) {
            // break;
            // } else {
            // continue;
            // }
            // }
            //
            // // 1) If it's time for the next action to start...
            // while (nextAction != null && timeDiff >= nextAction.getTime()) {
            // // 2) add it to the list of RunningActions...
            // runningActions.add(nextAction);
            // // 3) and run it for the first time...
            // nextAction.run();
            // // 4) and update nextaction to the next action.
            // nextAction = nextAction.getNextAction();
            // // 5) Continue looping until the next action shouldn't start yet.
            // continue;
            // }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forceStopShow() {
        firstAction = null;
        nextAction = null;
        if (runningActions != null)
            runningActions.clear();
        Main.getLightStrip().clear();
    }
}
