package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

/**
 * Highlights unbreakable carpets within ore veins in the Dwarven Mines.
 */
public final class CarpetHighlighter implements Renderable, Resettable {
	public static final CarpetHighlighter INSTANCE = new CarpetHighlighter();

	private static final Vec3d CARPET_BOUNDING_BOX = Boxes.getLengthVec(CarpetBlock.SHAPE.getBoundingBox());
	private static final int SEARCH_RADIUS = 15;
	private static final int TICK_INTERVAL = 15;
	private static final ObjectAVLTreeSet<BlockPos> CARPET_LOCATIONS = new ObjectAVLTreeSet<>();
	private static float[] colorComponents;
	private static boolean isLocationValid = false;

	@Init
	public static void init() {
		INSTANCE.configCallback(SkyblockerConfigManager.get().mining.dwarvenMines.carpetHighlightColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(INSTANCE::render);
		SkyblockEvents.LOCATION_CHANGE.register(INSTANCE::onLocationChange);
		Scheduler.INSTANCE.scheduleCyclic(INSTANCE::tick, TICK_INTERVAL);
		ClientPlayConnectionEvents.JOIN.register(INSTANCE);
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!isLocationValid || !SkyblockerConfigManager.get().mining.dwarvenMines.enableCarpetHighlighter) return;
		for (BlockPos carpetLocation : CARPET_LOCATIONS) {
			RenderHelper.renderFilled(context, Vec3d.of(carpetLocation), CARPET_BOUNDING_BOX, colorComponents, colorComponents[3], false);
		}
	}

	public void onLocationChange(Location location) {
		isLocationValid = location == Location.DWARVEN_MINES;
	}

	public void tick() {
		if (!isLocationValid || !SkyblockerConfigManager.get().mining.dwarvenMines.enableCarpetHighlighter || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;
		Iterable<BlockPos> iterable = BlockPos.iterateOutwards(MinecraftClient.getInstance().player.getBlockPos(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
		for (BlockPos blockPos : iterable) {
			//The iterator contains a BlockPos.Mutable that it changes the position of to iterate over blocks,
			// so it has to be converted to an immutable BlockPos or the position will change based on the player's position && the search radius
			if (checkForCarpet(blockPos)) CARPET_LOCATIONS.add(blockPos.toImmutable());
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
		BlockState actualBlock = MinecraftClient.getInstance().world.getBlockState(blockPos);
		// Gray/light blue - mithril
		// Light gray - tungsten
		// There are other colors for some ores in the royal mines,
		// but since the actual ores don't include wool blocks
		// they're not easily confused as ores so they are not accounted for here
		if (!(actualBlock.isOf(Blocks.GRAY_CARPET) ||
				actualBlock.isOf(Blocks.LIGHT_BLUE_CARPET) ||
				actualBlock.isOf(Blocks.LIGHT_GRAY_CARPET))) return false;
		BlockState blockBelow = MinecraftClient.getInstance().world.getBlockState(blockPos.down());
		return blockBelow.isOf(Blocks.SEA_LANTERN);
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
