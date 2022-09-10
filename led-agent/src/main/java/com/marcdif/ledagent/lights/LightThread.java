package com.marcdif.ledagent.lights;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.handlers.LightStrip;
import com.marcdif.ledagent.utils.ColorUtil;
import lombok.Getter;

public class LightThread extends Thread {
    private final LightStrip lightStrip;

    @Getter private LightState lightState;
    @Getter private LightState previousLightState;
    private boolean recentlyChangedState = false;

    /* STATIC VALUES */
    private final Color STARTUP_COLOR = ColorUtil.getColor(0, 171, 216);

    private int counter = 0;

    public LightThread(LightState lightState, LightStrip lightStrip) {
        this.lightState = lightState;
        this.previousLightState = lightState;

        this.lightStrip = lightStrip;
    }

    @Override
    public void run() {
        if (Main.DEBUG)
            Main.logMessage("[DEBUG] lightState: " + lightState.name() + ", previousLightState: " + previousLightState.name());
        switch (lightState) {
            case STARTUP:
                if (counter < 1000) {
                    if (renderStartupState(counter)) {
                        switchState(LightState.INTERSTITIAL);
                    }
                } else {
                    counter = 0;
                }
                break;
            case INTERSTITIAL:
                if (renderInterstitial(counter)) {
                    counter = 0;
                }
                break;
            case SHOW_STARTING:
            case SHOW_RUNNING:
            case SHOW_STOPPED:
        }
        counter++;
    }

    public void switchState(LightState newState) {
        Main.logMessage("[LIGHTTHREAD] Switching from " + lightState.name() + " state to " + newState.name() + " state!");
        if (lightState.equals(newState)) {
            Main.logMessage("[LIGHTTHREAD] State switch failed, already in the " + newState.name() + " state!");
            return;
        }
        counter = 0;
        recentlyChangedState = true;

        previousLightState = lightState;
        lightState = newState;
    }

    private boolean renderInterstitial(final int counter) {
        if (recentlyChangedState) {
            if (counter < 100) {
                lightStrip.setAll(Color.BLACK);
                lightStrip.render();
                return false;
            }
            if (counter % 20 == 0) {
                int num = Math.floorDiv((counter - 100), 20);
                long[] arr = new long[10];
                int i = 0;
                for (int pos = num; pos > 0; pos--) {
                    Color color;
                    if (i++ < 5) {
                        color = Color.RED;
                    } else {
                        color = Color.BLUE;
                    }
                    arr[pos - 1] = color.getColorBits();
                }
                long[] bigArr = new long[lightStrip.getPixelCount()];
                for (i = 0; i < bigArr.length; i++) {
                    bigArr[i] = arr[i % 10];
                }
                lightStrip.setPixels(bigArr);
                lightStrip.render();
            }
            if (counter >= 300) {
                recentlyChangedState = false;
                return true;
            }
            return false;
        }
        if (counter % 20 == 0) {
            int num = Math.floorDiv((counter - 100), 20);
            long[] arr = new long[10];
            int i = 0;
            for (int pos = num; i < 10; pos++) {
                Color color;
                if (i++ < 5) {
                    color = Color.RED;
                } else {
                    color = Color.BLUE;
                }
                if (pos >= 10) pos = 0;
                arr[pos] = color.getColorBits();
            }
            long[] bigArr = new long[lightStrip.getPixelCount()];
            for (i = 0; i < bigArr.length; i++) {
                bigArr[i] = arr[i % 10];
            }
            lightStrip.setPixels(bigArr);
            lightStrip.render();
        }
        //noinspection RedundantIfStatement
        if (counter >= 200) {
            return true;
        }
        return false;
    }

    private boolean renderStartupState(final int counter) {
        if (counter < 100) {
            // 100 ticks per second - lights are black/off for first 1 second
            lightStrip.renderAllOneColor(Color.BLACK);
        } else if (counter < 400) {
            // Lights then fade up to STARTUP_COLOR over 3 seconds
            lightStrip.renderAllOneColor(ColorUtil.colorMult(STARTUP_COLOR, counter / 500.0));
        } else if (counter < 700) {
            // Lights then stay solid on STARTUP_COLOR for 3 seconds
            lightStrip.renderAllOneColor(STARTUP_COLOR);
        } else if (counter < 700 + (lightStrip.getPixelCount() * 2)) {
            // Lights then change from STARTUP_COLOR to black/off one by one every 2 ticks until all lights are off
            // Starting from light 0 to the last light
            int pos = Math.floorDiv((counter - 700), 2);
            lightStrip.setPixel(Color.BLACK, pos);
        } else {
            lightStrip.setAll(Color.BLACK);
            lightStrip.render();
            return true;
        }
        lightStrip.render();
        return false;
    }
}

// Interstitial pattern
//
// XOOOOOXXXX
// XXOOOOOXXX
// XXXOOOOOXX
// XXXXOOOOOX
// XXXXXOOOOO
// OXXXXXOOOO
// OOXXXXXOOO
// OOOXXXXXOO
// OOOOXXXXXO
// OOOOOXXXXX
