package com.marcdif.ledagent.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigUtil {

    private static LightConfig config;

    static {
        try {
            File configFile = new File("config.yml");

            if (!configFile.exists()) configFile.createNewFile();

            InputStream targetStream = new FileInputStream(configFile);
            config = new Yaml().load(targetStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] getPixelLocations() {
        return config.getPixelLocations();
    }

    @Getter
    @AllArgsConstructor
    static class LightConfig {
        private int corner1, corner2, corner3, corner4, end;

        public int[] getPixelLocations() {
            return new int[]{corner1, corner2, corner3, corner4, end};
        }
    }
}
