package com.marcdif.lightagent.handlers;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.marcdif.lightagent.Main;
import com.marcdif.lightagent.show.ShowThread;
import com.marcdif.lightagent.utils.ColorUtil;

import java.util.Arrays;

public class LightStrip {
    private static final int LED_COUNT = 150;
    private static final int LED_PIN = 18;
    private static final int LED_FREQ_HZ = 800000;
    private static final int LED_DMZ = 10;
    private static final int LED_BRIGHTNESS = 255;
    private static final boolean LED_INVERT = false;
    private static final int LED_CHANNEL = 0;

    private final Ws281xLedStrip strip;
    private final long[] pixels = new long[LED_COUNT];

    public LightStrip() {
        strip = new Ws281xLedStrip(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMZ, LED_BRIGHTNESS, LED_CHANNEL, LED_INVERT, LedStripType.WS2811_STRIP_GRB, true);
    }

    public void render() {
        strip.render();
    }

    public void setAll(Color color) {
        color = ColorUtil.verify(color);

        if (ShowThread.DEBUG) Main.logMessage("[LIGHTS] Set all to " + color.getColorBits() + "!");
        strip.setStrip(color);
        Arrays.fill(pixels, color.getColorBits());
    }

    public void setPixel(Color color, int pixel) {
        color = ColorUtil.verify(color);

        if (ShowThread.DEBUG) Main.logMessage("[LIGHTS] Set ID:" + pixel + " to " + color.getColorBits() + "!");
        strip.setPixel(pixel, color);
        pixels[pixel] = color.getColorBits();
    }

    public void setPixels(Color color, int... pixel) {
        color = ColorUtil.verify(color);

        if (ShowThread.DEBUG) Main.logMessage("[LIGHTS] Set pixels to " + color.getColorBits() + "!");
        for (int i : pixel) {
            setPixel(color, i);
        }
    }

    public Color getPixel(int pixel) {
        if (pixel < LED_COUNT && pixel >= 0) {
            return new Color(pixels[pixel]);
        }
        return null;
    }

    /**
     * Turn off all strip pixels
     */
    public void clear() {
        setAll(Color.BLACK);
    }

    public Color getFullStripColor() {
        return getPixel(0);
    }
}
