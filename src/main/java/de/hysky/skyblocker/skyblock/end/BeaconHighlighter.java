package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BeaconHighlighter {
    public static final List<BlockPos> beaconPositions = new ArrayList<>();
    private static final float[] RED_COLOR_COMPONENTS = { 1.0f, 0.0f, 0.0f };

    /**
     * Initializes the beacon highlighting system.
     * {@link BeaconHighlighter#render(WorldRenderContext)} is called after translucent rendering.
     */
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BeaconHighlighter::render);
    }

    /**
     * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
     * is visible through walls.
     *
     * @param context An instance of WorldRenderContext for the RenderHelper to use
     */
    public static void render(WorldRenderContext context) {
        if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayer.endermanSlayer.highlightBeacons) {
            for (BlockPos pos : beaconPositions) {
                RenderHelper.renderFilled(context, pos, RED_COLOR_COMPONENTS, 0.5f, false);
            }
        }
    }
}
