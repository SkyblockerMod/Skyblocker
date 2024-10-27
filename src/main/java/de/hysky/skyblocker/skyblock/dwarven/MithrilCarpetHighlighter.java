package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Boxes;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class MithrilCarpetHighlighter implements Renderable, Tickable {
	private static final Vec3d CARPET_BOUNDING_BOX = Boxes.getLengthVec(CarpetBlock.SHAPE.getBoundingBox());
	private static final float[] RED_COLOR_COMPONENTS = {1.0F, 0.0F, 0.0F};
	private static final float BOX_ALPHA = 0.3F;
	private static final int SEARCH_RADIUS = 15;
	private static final int TICK_INTERVAL = 15;
	private static int tickCounter = 0;
	private static final MithrilCarpetHighlighter INSTANCE = new MithrilCarpetHighlighter();
	private static final ObjectArraySet<BlockPos> CARPET_LOCATIONS = new ObjectArraySet<>();
	private static boolean isLocationValid = false;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(INSTANCE::render);
		SkyblockEvents.LOCATION_CHANGE.register(INSTANCE::onLocationChange);
		ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!isLocationValid) return;
		for (BlockPos carpetLocation : CARPET_LOCATIONS) {
			RenderHelper.renderFilled(context, carpetLocation, CARPET_BOUNDING_BOX, RED_COLOR_COMPONENTS, BOX_ALPHA, false);
		}
	}

	public void onLocationChange(Location location) {
		isLocationValid = location == Location.DWARVEN_MINES;
		CARPET_LOCATIONS.clear();
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!isLocationValid || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;
		if (tickCounter++ < TICK_INTERVAL) return; // Only search for carpets every 15 ticks
		Iterable<BlockPos> iterable = BlockPos.iterateOutwards(MinecraftClient.getInstance().player.getBlockPos(), SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
		for (BlockPos blockPos : iterable) {
			if (checkForCarpet(blockPos)) CARPET_LOCATIONS.add(blockPos.toImmutable());
		}
		tickCounter = 0;
	}

	private boolean checkForCarpet(BlockPos blockPos) {
		@SuppressWarnings("DataFlowIssue") // Null check is already done in the run method
		BlockState actualBlock = MinecraftClient.getInstance().world.getBlockState(blockPos);
		BlockState blockBelow = MinecraftClient.getInstance().world.getBlockState(blockPos.down());
		return actualBlock.isOf(Blocks.GRAY_CARPET) && blockBelow.isOf(Blocks.SEA_LANTERN);
	}
}
