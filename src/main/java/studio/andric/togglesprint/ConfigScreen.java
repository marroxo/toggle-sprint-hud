package studio.andric.togglesprint;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Minimal vanilla config screen opened from Mod Menu. ponytail: hand-rolled CycleButtons, no config-UI lib.
 */
public class ConfigScreen extends Screen {
    private final Screen parent;
    private final SprintConfig config = ToggleSprintClient.CONFIG;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Toggle Sprint HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 200;
        int x = this.width / 2 - w / 2;
        int y = this.height / 4;
        int gap = 24;

        addRenderableWidget(CycleButton.onOffBuilder(config.showWhenOff)
            .create(x, y, w, 20, Component.literal("Show when off"), (btn, val) -> {
                config.showWhenOff = val;
                config.save();
            }));

        addRenderableWidget(CycleButton.onOffBuilder(config.background)
            .create(x, y + gap, w, 20, Component.literal("Background"), (btn, val) -> {
                config.background = val;
                config.save();
            }));

        addRenderableWidget(CycleButton.onOffBuilder(config.shadow)
            .create(x, y + gap * 2, w, 20, Component.literal("Text shadow"), (btn, val) -> {
                config.shadow = val;
                config.save();
            }));

        addRenderableWidget(CycleButton.<SprintConfig.Position>builder(
                pos -> Component.literal(pretty(pos)), config.position)
            .withValues(SprintConfig.Position.values())
            .create(x, y + gap * 3, w, 20, Component.literal("Position"), (btn, val) -> {
                config.position = val;
                config.save();
            }));

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
            .bounds(x, y + gap * 4 + 8, w, 20)
            .build());
    }

    private static String pretty(SprintConfig.Position pos) {
        return switch (pos) {
            case ABOVE_HOTBAR -> "Above Hotbar";
            case TOP_LEFT -> "Top Left";
            case TOP_RIGHT -> "Top Right";
            case BOTTOM_LEFT -> "Bottom Left";
            case BOTTOM_RIGHT -> "Bottom Right";
        };
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
