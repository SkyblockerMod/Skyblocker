package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BeaconHighlighter {
	private static final ObjectOpenHashSet<BlockPos> beaconPositions = new ObjectOpenHashSet<>();
	private static final float[] RED_COLOR_COMPONENTS = { 1.0f, 0.0f, 0.0f };

	/**
	 * Initializes the beacon highlighting system.
	 * {@link BeaconHighlighter#extractRendering(PrimitiveCollector)} is called to extract the beacon highlight for rendering.
	 */
	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(BeaconHighlighter::extractRendering);
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

			if (newState.is(Blocks.BEACON)) {
				beaconPositions.add(pos.immutable());
			}
		}
	}

	private static boolean onMessage(Component text, boolean overlay) {
		if (Utils.isInTheEnd() && !overlay) {
			String message = text.getString();

			if (message.contains("SLAYER QUEST COMPLETE!") || message.contains("NICE! SLAYER BOSS SLAIN!")) reset();
		}

		return true;
	}

	/**
	 * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
	 * is visible through walls.
	 */
	private static void extractRendering(PrimitiveCollector collector) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons && SlayerManager.isInSlayerType(SlayerType.VOIDGLOOM)) {
			for (BlockPos pos : beaconPositions) {
				collector.submitFilledBox(pos, RED_COLOR_COMPONENTS, 0.5f, true);
			}
		}
	}
}
