package com.marcdif.ledagent.show.actions;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.utils.ColorUtil;
import lombok.Getter;

public class FadeToAction extends ShowAction {
    @Getter private final Color color;
    private final double duration;

    private boolean firstRun = true;
    private double startTime;
    private Color starting;
    private Color delta;

    public FadeToAction(double time, Color color, double duration) {
        super(time, ActionType.FADE_TO);
        this.color = color;
        this.duration = duration;
    }

    @Override
    public void run_impl() {
        double now = System.currentTimeMillis() / 1000.0;
        if (firstRun) {
            this.startTime = now;
            this.firstRun = false;
            this.starting = Main.getLightManager().getLightStrip().getFullStripColor();
            this.delta = ColorUtil.getColorDelta(this.color, this.starting);
        }
        double percent = (now - this.startTime) / this.duration;
        Main.getLightManager().getLightStrip().setAll(ColorUtil.colorAdd(this.starting, ColorUtil.colorMult(this.delta, percent)));
        if (now - this.startTime >= this.duration) {
            this.done = true;
        }
    }

    @Override
    public String toString() {
        return "FadeToAction{" + "color=" + color + ", duration=" + duration + ", firstRun=" + firstRun + ", startTime="
                + startTime + ", starting=" + starting + ", delta=" + delta + ", time=" + time + ", type=" + type
                + ", done=" + done + '}';
    }
}
