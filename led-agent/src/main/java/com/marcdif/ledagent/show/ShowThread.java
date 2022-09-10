package com.marcdif.ledagent.show;

import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.show.actions.ShowAction;
import com.marcdif.ledagent.wss.packets.StartSongPacket;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ShowThread extends Thread {
    @Getter private final String showName;
    @Getter private final String showTitle;
    private final long startTime;
    private ShowAction firstAction, nextAction = null;
    private List<ShowAction> runningActions = null;
    private double startTimeSeconds;
    private ScheduledFuture<?> scheduledFuture;

    public ShowThread(String showName, long startTime) {
        this.showName = showName;
        this.startTime = startTime;

        File file = new File("shows/" + showName + ".show");
        if (!file.exists()) throw new IllegalArgumentException("Show file doesn't exist!");

        ShowFileParser.ShowData showData = ShowFileParser.parseShowFile(file, showName);
        showTitle = showData.getShowTitle();
        firstAction = showData.getFirstAction();

        Main.logMessage("Audio: " + showData.getShowAudio());
        String[] audio = showData.getShowAudio().split(" ");
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
            Main.getLightManager().getShowManager().showRunning = true;
            if (Main.DEBUG)
                Main.logMessage("Starting!!!");

            startTimeSeconds = System.currentTimeMillis() / 1000.0;

            runningActions = new ArrayList<>();

            nextAction = firstAction;

            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            scheduledFuture = executorService.scheduleAtFixedRate(() -> {
                if (Main.DEBUG_VERBOSE) {
                    Main.logMessage(System.currentTimeMillis() + "");
                    Main.logMessage((System.currentTimeMillis() / 1000.0) + "");
                    Main.logMessage((System.currentTimeMillis() / 1000.0) + "");
                }
                double currentTime = System.currentTimeMillis() / 1000.0;
                if (Main.DEBUG_VERBOSE)
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
                        if (Main.DEBUG_VERBOSE)
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
                    Main.getLightManager().getLightStrip().render();
                    // Stop show if there are no actions left
                    if (runningActions.isEmpty()) {
                        Main.logMessage("[WARN] No more actions left, stopping show!");
                        Main.getLightManager().getShowManager().stopShow();
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
                    if (Main.DEBUG)
                        Main.logMessage("Running " + nextAction);
                    // 4) and update nextaction to the next action.
                    nextAction = nextAction.getNextAction();
                    // 5) Continue looping until the next action shouldn't start yet.
                }
                Main.getLightManager().getLightStrip().render();
            }, 0, 10, TimeUnit.MILLISECONDS); // 100 times per second

            // while (true) {
            // long currentTime = System.currentTimeMillis();
            // long nextRun = (long) (startTime + ((count + 1) * 0.01));
            // // Only run every 0.01 seconds (100 times per second)
            // if (currentTime < nextRun) {
            // if (Main.DEBUG_VERBOSE) Main.logMessage("Sleep for " + ((lastRun + 0.01) -
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
        Main.getLightManager().getLightStrip().clear();
    }
}
