package com.marcdif.ledagent.handlers;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.marcdif.ledagent.Main;

public class LightStrip {
    private static final int LED_COUNT = 150;
    private static final int LED_PIN = 18;
    private static final int LED_FREQ_HZ = 800000;
    private static final int LED_DMZ = 10;
    private static final int LED_BRIGHTNESS = 255;
    private static final boolean LED_INVERT = false;
    private static final int LED_CHANNEL = 0;

    private Ws281xLedStrip strip;
    private final long[] pixels = new long[LED_COUNT];
//    private final Color[] pixels = new Color[LED_COUNT];

    private LEDStage activeStage = null;

    private Color c = null;

    public LightStrip() {
//        try {
//            strip = new Ws281xLedStrip(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMZ, LED_BRIGHTNESS, LED_CHANNEL, LED_INVERT,
//                    LedStripType.WS2811_STRIP_GRB, true);
//        } catch (Exception e) {
//            strip = null;
//        }
    }

    public void render() {
        if (activeStage == null) {
            activeStage = Main.getStage();
            if (activeStage == null) {
                Main.logMessage("No valid stage!");
                return;
            }
        }

        Main.logMessage("Level count: " + activeStage.getLevels().size());

        for (LEDStage.Level level : activeStage.getLevels()) {
            int i = 0;
            for (long pixel : level.getPixels()) {
                pixels[i++] = pixel;
            }
            Main.logMessage("Level " + i);
        }

        for (int i = 0; i < pixels.length; i++) {
            c = new Color(pixels[i]);
            strip.setPixel(i, c);
//            Main.logMessage(i + "\t" + c.getColorBits());
        }

        strip.render();
    }

//    public void setAll(Color color) {
//        color = ColorUtil.verify(color);
//
//        if (ShowThread.DEBUG)
//            Main.logMessage("[LIGHTS] Set all to " + color.getColorBits() + "!");
//        strip.setStrip(color);
//        Arrays.fill(pixels, color.getColorBits());
//    }
//
//    public void setPixel(Color color, int pixel) {
//        color = ColorUtil.verify(color);
//
//        if (ShowThread.DEBUG)
//            Main.logMessage("[LIGHTS] Set ID:" + pixel + " to " + color.getColorBits() + "!");
//        strip.setPixel(pixel, color);
//        pixels[pixel] = color.getColorBits();
//    }
//
//    public void setPixels(Color color, int fromPixel, int endPixel) {
//        color = ColorUtil.verify(color);
//
//        if (ShowThread.DEBUG)
//            Main.logMessage("[LIGHTS] Set pixels to " + color.getColorBits() + "!");
//        for (int i = Math.min(fromPixel, endPixel); i <= Math.max(fromPixel, endPixel); i++) {
//            setPixel(color, i);
//        }
//    }
//
//    public void setPixels(Color color, int... pixel) {
//        color = ColorUtil.verify(color);
//
//        if (ShowThread.DEBUG)
//            Main.logMessage("[LIGHTS] Set pixels to " + color.getColorBits() + "!");
//        for (int i : pixel) {
//            setPixel(color, i);
//        }
//    }
//
//    public Color getPixel(int pixel) {
//        if (pixel < LED_COUNT && pixel >= 0) {
//            return new Color(pixels[pixel]);
//        }
//        return null;
//    }
//
//    /**
//     * Turn off all strip pixels
//     */
//    public void clear() {
//        setAll(Color.BLACK);
//    }
//
//    public Color getFullStripColor() {
//        return getPixel(0);
//    }
}
