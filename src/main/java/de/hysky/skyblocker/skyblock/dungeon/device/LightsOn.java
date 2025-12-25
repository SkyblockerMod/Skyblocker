package de.hysky.skyblocker.skyblock.dungeon.device;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class LightsOn {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final BlockPos TOP_LEFT = new BlockPos(62, 136, 142);
	private static final BlockPos TOP_RIGHT = new BlockPos(58, 136, 142);
	private static final BlockPos MIDDLE_TOP = new BlockPos(60, 135, 142);
	private static final BlockPos MIDDLE_BOTTOM = new BlockPos(60, 134, 142);
	private static final BlockPos BOTTOM_LEFT = new BlockPos(62, 133, 142);
	private static final BlockPos BOTTOM_RIGHT = new BlockPos(58, 133, 142);
	private static final BlockPos[] LEVERS = { TOP_LEFT, TOP_RIGHT, MIDDLE_TOP, MIDDLE_BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT };
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);
	/**
	 * Higher than typical to ensure it stands out from redstone lamps that are off.
	 */
	private static final float ALPHA = 0.75f;

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(LightsOn::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!shouldProcess()) return;

		for (BlockPos lever : LEVERS) {
			ClientLevel world = CLIENT.level;
			BlockState state = world.getBlockState(lever);

			if (state.getBlock().equals(Blocks.LEVER) && state.hasProperty(BlockStateProperties.POWERED) && !state.getValue(BlockStateProperties.POWERED)) {
				AABB box = RenderHelper.getBlockBoundingBox(world, state, lever);

				if (box != null) {
					collector.submitFilledBox(box, RED, ALPHA, false);
				}
			}
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveLightsOn && Utils.isInDungeons() && DungeonManager.isInBoss()
				&& DungeonManager.getBoss() == DungeonBoss.MAXOR;
	}
}
