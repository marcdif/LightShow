package com.marcdif.ledagent.show.actions;

import lombok.Setter;

public class ForAction extends ShowAction {
    private final int count;
    private final double delay;

    @Setter private ShowAction firstAction = null;

    public ForAction(double time, int count, double delay) {
        super(time, ActionType.FOR);
        this.count = count;
        this.delay = delay;
    }

    @Override
    public void run_impl() {
        this.done = true;
    }

    @Override
    public String toString() {
        return "ForAction{" + "count=" + count + ", delay=" + delay + ", firstAction=" + firstAction + ", time=" + time
                + ", type=" + type + ", done=" + done + '}';
    }
}
