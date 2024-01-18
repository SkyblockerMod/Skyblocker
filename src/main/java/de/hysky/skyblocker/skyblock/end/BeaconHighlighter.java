package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BeaconHighlighter {
    static List<BlockPos> pos = new ArrayList<>();

    /**
     * Initializes the beacon highlighting system.
     * `BeaconHighlighter::render` is called after translucent rendering.
     * `BeaconHighlighter::update` should be called every 5 ticks.
     */
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BeaconHighlighter::render);
        Scheduler.INSTANCE.scheduleCyclic(BeaconHighlighter::update, 5);
    }

    /**
     * Updates the position of the beacon.
     * It checks in a 15 block radius on the X/Z axis, and a ~5 block radius on the Y axis.
     * If a beacon is found, `pos` is updated to the beacon's position.
     */

    public static void update() {
        var player = MinecraftClient.getInstance().player;
        var world = MinecraftClient.getInstance().world;
        pos.clear();
        if(player != null && world != null &&
                SkyblockerConfigManager.get().slayer.endermanSlayer.highlightBeacons) {
            for(int x = (player.getBlockPos().getX()-15); x<player.getBlockPos().getX()+15; x++) {
                for(int z = (player.getBlockPos().getZ()-15); z<player.getBlockPos().getZ()+15; z++) {
                    for(int y = (player.getBlockPos().getY()-3); y<player.getBlockPos().getY()+7; y++) {
                        var state = world.getBlockState(new BlockPos(x, y, z));
                        if(state.toString().contains("minecraft:beacon")) {
                            pos.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
     * is visible through walls.
     * @param context An instance of WorldRenderContext for the RenderHelper to use
     */
    public static void render(WorldRenderContext context) {
        pos.forEach((it) -> {
            RenderHelper.renderFilled(
                    context,
                    it,
                    new float[]{1.0f, 0.0f, 0.0f},
                    0.5f,
                    false
            );
        });
    }
}
