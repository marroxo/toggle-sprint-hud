package studio.andric.togglesprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tap the vanilla Sprint key to toggle permanent sprint, with a clean on-screen indicator (26.2 Fabric HUD API).
 * Uses whatever key Sprint is bound to - no separate keybind.
 */
public class ToggleSprintClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("togglesprint");
    public static final SprintConfig CONFIG = SprintConfig.load();

    private static final int MARGIN = 4;

    /** Whether permanent sprint is currently toggled on. */
    public static boolean enabled = false;

    // Rising-edge detection on the physical sprint key (unaffected by our own setDown).
    private boolean wasSprintDown = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null) {
                wasSprintDown = false;
                return;
            }

            KeyMapping sprint = mc.options.keySprint;
            InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(sprint);

            // Only keyboard-bound sprint can be polled physically; mouse-bound falls back to no toggle.
            boolean down = key.getType() == InputConstants.Type.KEYSYM
                && InputConstants.isKeyDown(mc.getWindow(), key.getValue());

            if (down && !wasSprintDown) {
                enabled = !enabled; // a fresh tap flips the toggle
            }
            wasSprintDown = down;

            // While toggled on, hold the sprint key so the player keeps sprinting without holding it.
            if (enabled) {
                sprint.setDown(true);
            }
        });

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("togglesprint", "indicator"),
            (ctx, tickCounter) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;
                if (!enabled && !CONFIG.showWhenOff) return;

                Font font = mc.font;
                String text = enabled ? "Sprinting" : "Sprint Off";
                int color = enabled ? CONFIG.onColor : CONFIG.offColor;

                int width = font.width(text);
                int height = font.lineHeight;

                double s = CONFIG.scale;
                int wReal = (int) (width * s);
                int hReal = (int) (height * s);

                int rx, ry;
                switch (CONFIG.position) {
                    case TOP_LEFT -> { rx = MARGIN; ry = MARGIN; }
                    case BOTTOM_LEFT -> { rx = MARGIN; ry = ctx.guiHeight() - hReal - MARGIN; }
                    case BOTTOM_RIGHT -> { rx = ctx.guiWidth() - wReal - MARGIN; ry = ctx.guiHeight() - hReal - MARGIN; }
                    case ABOVE_HOTBAR -> { rx = (ctx.guiWidth() - wReal) / 2; ry = ctx.guiHeight() - 59 - (hReal - height); }
                    default -> { rx = ctx.guiWidth() - wReal - MARGIN; ry = MARGIN; } // TOP_RIGHT
                }

                boolean scaled = Math.abs(s - 1.0) > 0.001;
                int dx = scaled ? (int) Math.round(rx / s) : rx;
                int dy = scaled ? (int) Math.round(ry / s) : ry;

                if (scaled) {
                    ctx.pose().pushMatrix();
                    ctx.pose().scale((float) s, (float) s);
                }

                if (CONFIG.background) {
                    ctx.fill(dx - 3, dy - 2, dx + width + 3, dy + height + 1, 0x90000000);
                }
                ctx.text(font, text, dx, dy, color, CONFIG.shadow);

                if (scaled) {
                    ctx.pose().popMatrix();
                }
            }
        );

        LOGGER.info("Toggle Sprint HUD ready");
    }
}
