package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.foraging.log_highlighter.TreeHighlightHandler;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class ModernForagingIsland {

	@Init
	public static void init() {
		// Hook into block break for log counting
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if (Utils.getLocation() != Location.THE_PARK) return;
			if (player != MinecraftClient.getInstance().player) return;

			Block brokenBlock = state.getBlock();
			// Let ForagingHud decide if this block is a log, and store its ID:
			String skyId = ForagingHud.getEnchantedIdForBlock(brokenBlock);
			if (skyId != null) {
				ForagingHud.incrementLogs(skyId);
			}
		});

		// Hook into world render for tree highlighting
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client.world == null || client.player == null) return;

			MatrixStack matrices = context.matrixStack();
			Vec3d cameraPos = context.camera().getPos();
			WorldRenderer worldRenderer = context.worldRenderer();

			TreeHighlightHandler.render(matrices, worldRenderer, cameraPos);
		});
	}
}
