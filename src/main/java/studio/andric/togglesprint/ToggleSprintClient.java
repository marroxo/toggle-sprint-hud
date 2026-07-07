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
 * Toggle permanent sprint with a keybind, plus a clean on-screen indicator (26.2 Fabric HUD API).
 */
public class ToggleSprintClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("togglesprint");
    public static final SprintConfig CONFIG = SprintConfig.load();

    private static final int MARGIN = 4;

    /** Whether permanent sprint is currently toggled on. */
    public static boolean enabled = false;

    private KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        // Unbound by default so it never clashes; the user binds it in Controls.
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.togglesprint.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MOVEMENT
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
            }
            // While toggled on, hold the vanilla sprint key so the player keeps sprinting.
            if (enabled && mc.player != null) {
                mc.options.keySprint.setDown(true);
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

                int x, y; // top-left of the text
                switch (CONFIG.position) {
                    case TOP_LEFT -> { x = MARGIN; y = MARGIN; }
                    case TOP_RIGHT -> { x = ctx.guiWidth() - width - MARGIN; y = MARGIN; }
                    case BOTTOM_LEFT -> { x = MARGIN; y = ctx.guiHeight() - height - MARGIN; }
                    case BOTTOM_RIGHT -> { x = ctx.guiWidth() - width - MARGIN; y = ctx.guiHeight() - height - MARGIN; }
                    default -> { // ABOVE_HOTBAR: centered, just above the hotbar/xp bar
                        x = (ctx.guiWidth() - width) / 2;
                        y = ctx.guiHeight() - 59;
                    }
                }

                if (CONFIG.background) {
                    ctx.fill(x - 3, y - 2, x + width + 3, y + height + 1, 0x90000000);
                }
                ctx.text(font, text, x, y, color, CONFIG.shadow);
            }
        );

        LOGGER.info("Toggle Sprint HUD ready");
    }
}
