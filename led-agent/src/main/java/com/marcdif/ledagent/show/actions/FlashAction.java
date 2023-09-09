package com.marcdif.ledagent.show.actions;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;

public class FlashAction extends ShowAction {
    private final Color finalColor;
    private final int level, startPixel, endPixel;

    public FlashAction(double time, int level, Color color, int startPixel, int endPixel) {
        super(time, ActionType.FULL_LIGHT);
        this.level = level;
        this.finalColor = color;
        this.startPixel = startPixel;
        this.endPixel = endPixel;
    }

    @Override
    public void run_impl() {
        Main.getShowManager().getStage().getLevel(level).setPixels(finalColor, startPixel, endPixel);
        this.done = true;
    }

    @Override
    public String toString() {
        return "FullLightAction{" + "finalColor=" + finalColor + ", time=" + time + ", type=" + type + ", done=" + done
                + '}';
    }
}
