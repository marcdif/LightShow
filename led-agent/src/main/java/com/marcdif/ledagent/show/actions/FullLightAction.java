package com.marcdif.ledagent.show.actions;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;

public class FullLightAction extends ShowAction {
    private final int level;
    private final Color finalColor;

    public FullLightAction(double time, int level, Color color) {
        super(time, ActionType.FULL_LIGHT);
        this.level = level;
        this.finalColor = color;
    }

    @Override
    public void run_impl() {
        Main.getStage().getLevel(level).setAll(finalColor);
        this.done = true;
    }

    @Override
    public String toString() {
        return "FullLightAction{" + "finalColor=" + finalColor + ", time=" + time + ", type=" + type + ", done=" + done
                + '}';
    }
}
