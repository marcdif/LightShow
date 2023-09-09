package com.marcdif.ledagent.handlers;

import com.github.mbelling.ws281x.Color;
import com.marcdif.ledagent.Main;
import com.marcdif.ledagent.show.ShowThread;
import com.marcdif.ledagent.utils.ColorUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class LEDStage {
    private final int corner1, corner2, corner3, corner4, end;
    private final List<Level> levels = new ArrayList<>();

    public Level getLevel(int level) {
        if (level < 0 || level >= levels.size()) {
            Level newLevel = new Level(level);
            levels.set(level, newLevel);
            return newLevel;
        }
        return levels.get(level);
    }

    public void clear() {
        levels.forEach(Level::clear);
    }

    public class Level {
        private final int levelID;
        private final long[] pixels;
        private boolean changed = false;

        Level(int levelID) {
            this.levelID = levelID;
            pixels = new long[end - corner1];
        }

        public Color getPixel(int pixel) {
            if (pixel < pixels.length && pixel >= 0) {
                return new Color(pixels[pixel]);
            }
            return null;
        }

        public long[] getPixels() {
            return pixels;
        }

        public void clear() {
            setAll(Color.BLACK);
        }

        public void setAll(Color color) {
            color = ColorUtil.verify(color);

            if (ShowThread.DEBUG)
                Main.logMessage("[LIGHTS LVL" + levelID + "] Set all to " + color.getColorBits() + "!");
            Arrays.fill(pixels, color.getColorBits());
            changed = true;
        }

        public void setPixel(Color color, int pixel) {
            color = ColorUtil.verify(color);

            if (ShowThread.DEBUG)
                Main.logMessage("[LIGHTS LVL" + levelID + "] Set ID:" + pixel + " to " + color.getColorBits() + "!");
            pixels[pixel] = color.getColorBits();
            changed = true;
        }

        public void setPixels(Color color, int fromPixel, int endPixel) {
            color = ColorUtil.verify(color);

            if (ShowThread.DEBUG)
                Main.logMessage("[LIGHTS LVL" + levelID + "] Set pixels to " + color.getColorBits() + "!");
            for (int i = Math.min(fromPixel, endPixel); i <= Math.max(fromPixel, endPixel); i++) {
                pixels[i] = color.getColorBits();
            }
            changed = true;
        }

        public boolean hasChanged() {
            return changed;
        }

        public void resetChanged() {
            this.changed = false;
        }
    }
}
