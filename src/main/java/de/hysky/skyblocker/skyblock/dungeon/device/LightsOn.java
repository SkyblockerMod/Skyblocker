package de.hysky.skyblocker.skyblock.dungeon.device;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public class LightsOn {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final BlockPos TOP_LEFT = new BlockPos(62, 136, 142);
	private static final BlockPos TOP_RIGHT = new BlockPos(58, 136, 142);
	private static final BlockPos MIDDLE_TOP = new BlockPos(60, 135, 142);
	private static final BlockPos MIDDLE_BOTTOM = new BlockPos(60, 134, 142);
	private static final BlockPos BOTTOM_LEFT = new BlockPos(62, 133, 142);
	private static final BlockPos BOTTOM_RIGHT = new BlockPos(58, 133, 142);
	private static final BlockPos[] LEVERS = { TOP_LEFT, TOP_RIGHT, MIDDLE_TOP, MIDDLE_BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT };
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LightsOn::render);
	}

	private static void render(WorldRenderContext context) {
		if (SkyblockerConfigManager.get().dungeons.devices.solveLightsOn && Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR) {
			for (BlockPos lever : LEVERS) {
				ClientWorld world = CLIENT.world;
				BlockState state = world.getBlockState(lever);

				if (state.getBlock().equals(Blocks.LEVER) && state.contains(Properties.POWERED) && !state.get(Properties.POWERED)) {
					RenderHelper.renderFilled(context, lever, RED, 0.5f, false);
				}
			}
		}
	}
}
