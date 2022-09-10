package com.marcdif.ledagent.show;

import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.show.actions.*;
import com.marcdif.ledagent.utils.ColorUtil;
import com.marcdif.ledagent.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ShowFileParser {

    public static ShowData parseShowFile(File file, String showName) {
        if (Main.DEBUG)
            Main.logMessage("Loading actions for " + showName + "...");
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            stream.forEach(lines::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long size = 0;
        String showAudio = "Unknown", showTitle = "Unknown";

        ShowAction firstAction = null;

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
                    if (Main.DEBUG)
                        Main.logMessage("  Processing " + nextAction);
                    // Sort as we go
                    ShowAction a = relativeFirstAction;
                    ShowAction last;
                    while (a.getNextAction() != null && a.getTime() < nextAction.getTime()) {
                        last = a;
                        a = a.getNextAction();
                        if (Main.DEBUG)
                            Main.logMessage("    At " + a.toString());
                        if (a.getTime() > nextAction.getTime()) {
                            a = last;
                            if (Main.DEBUG)
                                Main.logMessage("      Too far... jumping back to " + a);
                            break;
                        }
                    }

                    if (Main.DEBUG)
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

        if (Main.DEBUG)
            Main.logMessage("Finished loading " + size + " actions...");

        // ShowActions are a LinkedList
        // Returning the first ShowAction is equivalent to returning the entire list of actions
        return new ShowData(showTitle, showAudio, firstAction);
    }

    @Getter
    @AllArgsConstructor
    public static class ShowData {
        private String showTitle, showAudio;
        private ShowAction firstAction;
    }
}
