package de.hysky.skyblocker.skyblock.dwarven;

import java.awt.Color;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Highlights unbreakable carpets within ore veins in the Dwarven Mines.
 */
public final class CarpetHighlighter implements Renderable, Resettable {
	public static final CarpetHighlighter INSTANCE = new CarpetHighlighter();

	private static final Vec3 CARPET_BOUNDING_BOX = Boxes.getLengthVec(CarpetBlock.SHAPE.bounds());
	private static final int SEARCH_RADIUS = 15;
	private static final int TICK_INTERVAL = 15;
	private static final BlockPosSet CARPET_LOCATIONS = new BlockPosSet();
	private static float[] colorComponents;
	private static boolean isLocationValid = false;

	@Init
	public static void init() {
		INSTANCE.configCallback(SkyblockerConfigManager.get().mining.dwarvenMines.carpetHighlightColor);
		WorldRenderExtractionCallback.EVENT.register(INSTANCE::extractRendering);
		SkyblockEvents.LOCATION_CHANGE.register(INSTANCE::onLocationChange);
		Scheduler.INSTANCE.scheduleCyclic(INSTANCE::tick, TICK_INTERVAL);
		ClientPlayConnectionEvents.JOIN.register(INSTANCE);
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!isLocationValid || !SkyblockerConfigManager.get().mining.dwarvenMines.enableCarpetHighlighter) return;
		for (BlockPos carpetLocation : CARPET_LOCATIONS.iterateMut()) {
			collector.submitFilledBox(Vec3.atLowerCornerOf(carpetLocation), CARPET_BOUNDING_BOX, colorComponents, colorComponents[3], false);
		}
	}

	public void onLocationChange(Location location) {
		isLocationValid = location == Location.DWARVEN_MINES;
	}

	public void tick() {
		if (!isLocationValid || !SkyblockerConfigManager.get().mining.dwarvenMines.enableCarpetHighlighter || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
		Iterable<BlockPos> iterable = BlockPos.withinManhattan(Minecraft.getInstance().player.blockPosition(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
		for (BlockPos blockPos : iterable) {
			//The iterator contains a BlockPos.Mutable that it changes the position of to iterate over blocks,
			// so it has to be converted to an immutable BlockPos or the position will change based on the player's position && the search radius
			if (checkForCarpet(blockPos)) CARPET_LOCATIONS.add(blockPos.immutable());
		}
	}

	/**
	 * @param blockPos The position to check for a carpet
	 * @return Whether the block at the given position is a gray carpet with a sea lantern below it, which is how all unbreakable carpets are placed
	 * @implNote <p>getBlockState is a heavy method, so this method will become a hot spot as the search radius increases || the tick interval decreases.</p>
	 * 		<p>Consider profiling this method if either of those values are changed.</p>
	 */
	private boolean checkForCarpet(BlockPos blockPos) {
		@SuppressWarnings("DataFlowIssue") // Null check is already done in the run method
		BlockState actualBlock = Minecraft.getInstance().level.getBlockState(blockPos);
		// Gray/light blue - mithril
		// Light gray - tungsten
		// There are other colors for some ores in the royal mines,
		// but since the actual ores don't include wool blocks
		// they're not easily confused as ores so they are not accounted for here
		if (!(actualBlock.is(Blocks.GRAY_CARPET) ||
				actualBlock.is(Blocks.LIGHT_BLUE_CARPET) ||
				actualBlock.is(Blocks.LIGHT_GRAY_CARPET))) return false;
		BlockState blockBelow = Minecraft.getInstance().level.getBlockState(blockPos.below());
		return blockBelow.is(Blocks.SEA_LANTERN);
	}

	/**
	 * <p>Caches the color components from the given color for rendering to avoid recalculating them every frame.</p>
	 * <p>Called by the {@link de.hysky.skyblocker.config.categories.MiningCategory MiningCategory} > carpetHighlightColor when the color is updated.</p>
	 */
	public void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}

	@Override
	public void reset() {
		isLocationValid = false;
		CARPET_LOCATIONS.clear();
	}
}
