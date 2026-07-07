package studio.andric.togglesprint;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persisted config backed by a properties file. ponytail: 5 fields, no config library needed.
 */
public final class SprintConfig {
    public enum Position {
        ABOVE_HOTBAR, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

        public Position next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("togglesprint.properties");

    /** Show the indicator even when sprint toggle is OFF. */
    public boolean showWhenOff = true;
    public boolean shadow = true;
    public boolean background = false;
    public double scale = 1.0;
    public Position position = Position.TOP_RIGHT;

    // ARGB colors for the ON / OFF states.
    public int onColor = 0xFF55FF55;
    public int offColor = 0xFFAAAAAA;

    public static SprintConfig load() {
        SprintConfig c = new SprintConfig();
        if (Files.exists(PATH)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(PATH)) {
                p.load(in);
                c.showWhenOff = Boolean.parseBoolean(p.getProperty("showWhenOff", "false"));
                c.shadow = Boolean.parseBoolean(p.getProperty("shadow", "true"));
                c.background = Boolean.parseBoolean(p.getProperty("background", "true"));
                c.scale = parseScale(p.getProperty("scale"), 1.0);
                c.position = parsePosition(p.getProperty("position"), Position.TOP_RIGHT);
                c.onColor = parseColor(p.getProperty("onColor"), 0xFF55FF55);
                c.offColor = parseColor(p.getProperty("offColor"), 0xFFAAAAAA);
            } catch (IOException e) {
                ToggleSprintClient.LOGGER.warn("Failed to read config, using defaults", e);
            }
        }
        return c;
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("showWhenOff", Boolean.toString(showWhenOff));
        p.setProperty("shadow", Boolean.toString(shadow));
        p.setProperty("background", Boolean.toString(background));
        p.setProperty("scale", Double.toString(scale));
        p.setProperty("position", position.name());
        p.setProperty("onColor", Integer.toHexString(onColor));
        p.setProperty("offColor", Integer.toHexString(offColor));
        try (OutputStream out = Files.newOutputStream(PATH)) {
            p.store(out, "Toggle Sprint HUD config");
        } catch (IOException e) {
            ToggleSprintClient.LOGGER.warn("Failed to save config", e);
        }
    }

    private static double parseScale(String s, double fallback) {
        if (s == null) return fallback;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Position parsePosition(String s, Position fallback) {
        if (s == null) return fallback;
        try {
            return Position.valueOf(s);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static int parseColor(String s, int fallback) {
        if (s == null) return fallback;
        try {
            return (int) Long.parseLong(s, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
