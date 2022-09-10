package com.marcdif.ledagent.lights;

import com.marcdif.ledagent.handlers.LightStrip;
import com.marcdif.ledagent.show.ShowManager;
import com.marcdif.ledagent.utils.ConfigUtil;
import lombok.Getter;

public class LightManager {
    @Getter private final LightThread lightThread;
    @Getter private final LightStrip lightStrip;
    @Getter private final ShowManager showManager;

    public LightManager() {
        lightStrip = new LightStrip(ConfigUtil.getPixelLocations());
        lightStrip.clear();

        lightThread = new LightThread(LightState.STARTUP, lightStrip);
        lightThread.start();

        showManager = new ShowManager();
    }

    public LightState getLightState() {
        return lightThread.getLightState();
    }
}
