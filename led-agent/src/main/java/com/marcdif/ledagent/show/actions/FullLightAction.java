package com.marcdif.ledagent.show.actions;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;

public class FullLightAction extends ShowAction {
    private final Color finalColor;

    public FullLightAction(double time, Color color) {
        super(time, ActionType.FULL_LIGHT);
        this.finalColor = color;
    }

    @Override
    public void run_impl() {
        Main.getLightManager().getLightStrip().setAll(finalColor);
        this.done = true;
    }

    @Override
    public String toString() {
        return "FullLightAction{" + "finalColor=" + finalColor + ", time=" + time + ", type=" + type + ", done=" + done
                + '}';
    }
}
