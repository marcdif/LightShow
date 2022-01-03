package com.marcdif.ledagent.show.actions;

public class LogAction extends ShowAction {
    private final String message;

    public LogAction(double time, String message) {
        super(time, ActionType.LOG);
        this.message = message;
    }

    @Override
    public void run_impl() {
        System.out.println(message);
        this.done = true;
    }

    @Override
    public String toString() {
        return "LogAction{" + "message='" + message + '\'' + ", time=" + time + ", type=" + type + ", done=" + done
                + '}';
    }
}
