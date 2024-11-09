package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
public final class CarpetHighlighter implements Renderable, Tickable, Resettable {
	public static final CarpetHighlighter INSTANCE = new CarpetHighlighter();

	private static final Vec3d CARPET_BOUNDING_BOX = Boxes.getLengthVec(CarpetBlock.SHAPE.getBoundingBox());
	private static final int SEARCH_RADIUS = 15;
	private static final int TICK_INTERVAL = 15;
	private static final ObjectArraySet<BlockPos> CARPET_LOCATIONS = new ObjectArraySet<>();

	private static float[] colorComponents;
	private static int tickCounter = 0;
	private static boolean isLocationValid = false;

	@Init
	public static void init() {
		INSTANCE.configCallback(SkyblockerConfigManager.get().mining.dwarvenMines.carpetHighlightColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(INSTANCE::render);
		SkyblockEvents.LOCATION_CHANGE.register(INSTANCE::onLocationChange);
		ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
		ClientPlayConnectionEvents.JOIN.register(INSTANCE);
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!isLocationValid || !SkyblockerConfigManager.get().mining.dwarvenMines.enableCarpetHighlighter) return;
		for (BlockPos carpetLocation : CARPET_LOCATIONS) {
			RenderHelper.renderFilled(context, carpetLocation, CARPET_BOUNDING_BOX, colorComponents, colorComponents[3], false);
		}
	}

	public void onLocationChange(Location location) {
		isLocationValid = location == Location.DWARVEN_MINES;
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!isLocationValid || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;
		if (tickCounter++ < TICK_INTERVAL) return; // Only search for carpets every TICK_INTERVAL ticks
		Iterable<BlockPos> iterable = BlockPos.iterateOutwards(MinecraftClient.getInstance().player.getBlockPos(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
		for (BlockPos blockPos : iterable) {
			//The iterator contains a BlockPos.Mutable that it changes the position of to iterate over blocks,
			// so it has to be converted to an immutable BlockPos or the position will change based on the player's position && the search radius
			if (checkForCarpet(blockPos)) CARPET_LOCATIONS.add(blockPos.toImmutable());
		}
		tickCounter = 0;
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
