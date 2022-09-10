package com.marcdif.ledagent.handlers;

import com.github.mbelling.ws281x.Color;
import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.utils.ColorUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class LightStrip {
    private final Ws281xLedStrip strip;
    private final long[] pixels;
    @Getter private final StripSettings stripSettings;
    @Getter private final LEDStage ledStage;

    public LightStrip(int[] pixelLocations) throws IndexOutOfBoundsException {
        this(new LEDStage(pixelLocations[0], pixelLocations[1], pixelLocations[2], pixelLocations[3], pixelLocations[4]));

        if (pixelLocations.length != 5)
            throw new IllegalArgumentException("'pixelLocations' array should have exactly five values!");
    }

    public LightStrip(LEDStage ledStage) {
        this(18, 800000, 10, 255, 0, false, ledStage);
    }


    public LightStrip(int ledPin, int ledFreqHz, int ledDMZ, int ledBrightness, int ledChannel, boolean ledInvert, LEDStage ledStage) {
        this(new StripSettings(ledStage.getEnd(), ledPin, ledFreqHz, ledDMZ, ledBrightness, ledChannel, ledInvert), ledStage);
    }

    public LightStrip(StripSettings stripSettings, LEDStage ledStage) {
        this.stripSettings = stripSettings;
        this.ledStage = ledStage;

        this.strip = stripSettings.instantiateHardwareLedStrip();
        this.pixels = new long[strip.getLedsCount()];
    }

    public void render() {
        strip.render();
    }

    public void setAll(Color color) {
        color = ColorUtil.verify(color);

        if (Main.DEBUG)
            Main.logMessage("[LIGHTS] Set all to " + color.getColorBits() + "!");
        strip.setStrip(color);
        Arrays.fill(pixels, color.getColorBits());
    }

    public void setPixel(Color color, int pixel) {
        color = ColorUtil.verify(color);

        if (Main.DEBUG)
            Main.logMessage("[LIGHTS] Set ID:" + pixel + " to " + color.getColorBits() + "!");
        strip.setPixel(pixel, color);
        pixels[pixel] = color.getColorBits();
    }

    public void setPixels(Color color, int... pixel) {
        color = ColorUtil.verify(color);

        if (Main.DEBUG)
            Main.logMessage("[LIGHTS] Set pixels to " + color.getColorBits() + "!");
        for (int i : pixel) {
            setPixel(color, i);
        }
    }

    public void setPixels(long[] arr) {
        if (Main.DEBUG)
            Main.logMessage("[LIGHTS] Set pixels to array \"" + Arrays.toString(arr) + "\"!");
        int bound;
        if (arr.length >= pixels.length) {
            if (arr.length > pixels.length) {
                Main.logMessage("[LIGHTS] [WARNING] Color array is larger than pixel array, color array will be truncated!");
            }
            bound = pixels.length;
        } else {
            bound = arr.length;
        }
        for (int i = 0; i < bound; i++) {
            setPixel(ColorUtil.getColor(arr[i]), i);
        }
    }

    public Color getPixel(int pixel) {
        if (pixel < pixels.length && pixel >= 0) {
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

    public int getPixelCount() {
        return ledStage.getEnd();
    }

    public void renderAllOneColor(Color color) {
        long[] arr = new long[ledStage.getEnd()];
        Arrays.fill(arr, color.getColorBits());
        setPixels(arr);
    }

    @Getter
    @AllArgsConstructor
    public static class StripSettings {
        private final int ledCount, ledPin, ledFreqHz, ledDMZ, ledBrightness, ledChannel;
        private final boolean ledInvert;

        public Ws281xLedStrip instantiateHardwareLedStrip() {
            return new Ws281xLedStrip(ledCount, ledPin, ledFreqHz, ledDMZ, ledBrightness, ledChannel, ledInvert,
                    LedStripType.WS2811_STRIP_GRB, true);
        }
    }

    @Getter
    public static class LEDStage {
        private final int corner1, corner2, corner3, corner4, end;

        public LEDStage(int corner1, int corner2, int corner3, int corner4, int end) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.corner3 = corner3;
            this.corner4 = corner4;
            this.end = end;
            if (corner1 != 0) {
                throw new IllegalArgumentException("'corner1' value must be 0!");
            }
            if (corner1 > corner2 || corner2 > corner3 || corner3 > corner4 || corner4 > end) {
                throw new IllegalArgumentException("Corner values must be increasing in size!");
            }
        }

        public long[] renderAllOneColor(Color color) {
            long[] arr = new long[end];
            Arrays.fill(arr, color.getColorBits());
            return arr;
        }
    }
}
