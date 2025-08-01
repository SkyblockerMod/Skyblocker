package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BeaconHighlighter {
    private static final ObjectOpenHashSet<BlockPos> beaconPositions = new ObjectOpenHashSet<>();
    private static final float[] RED_COLOR_COMPONENTS = { 1.0f, 0.0f, 0.0f };

    /**
     * Initializes the beacon highlighting system.
     * {@link BeaconHighlighter#render(WorldRenderContext)} is called after translucent rendering.
     */
    @Init
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BeaconHighlighter::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
        ClientReceiveMessageEvents.ALLOW_GAME.register(BeaconHighlighter::onMessage);
        WorldEvents.BLOCK_STATE_UPDATE.register(BeaconHighlighter::onBlockStateUpdate);
    }

    private static void reset() {
        beaconPositions.clear();
    }

    private static void onBlockStateUpdate(BlockPos pos, BlockState oldState, BlockState newState) {
    	if (Utils.isInTheEnd() && SlayerManager.isBossSpawned()) {
    		beaconPositions.remove(pos);

    		if (newState.isOf(Blocks.BEACON)) {
    			beaconPositions.add(pos.toImmutable());
    		}
    	}
    }

    private static boolean onMessage(Text text, boolean overlay) {
        if (Utils.isInTheEnd() && !overlay) {
            String message = text.getString();

            if (message.contains("SLAYER QUEST COMPLETE!") || message.contains("NICE! SLAYER BOSS SLAIN!")) reset();
        }

        return true;
    }

    /**
     * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
     * is visible through walls.
     *
     * @param context An instance of WorldRenderContext for the RenderHelper to use
     */
    private static void render(WorldRenderContext context) {
        if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons && SlayerManager.isInSlayerType(SlayerType.VOIDGLOOM)) {
            for (BlockPos pos : beaconPositions) {
                RenderHelper.renderFilled(context, pos, RED_COLOR_COMPONENTS, 0.5f, true);
            }
        }
    }
}
