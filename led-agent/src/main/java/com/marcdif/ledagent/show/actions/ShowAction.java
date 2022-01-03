package com.marcdif.ledagent.show.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public abstract class ShowAction {
    protected final double time;
    protected final ActionType type;

    @Setter private ShowAction nextAction;
    protected boolean done = false;

    public abstract void run_impl();

    public ShowAction getNextAction() {
        return nextAction;
    }

    public boolean run() {
        if (done)
            return true;
        run_impl();
        return done;
    }

    @Override
    public abstract String toString();
}
