package me.xmrvizzy.skyblocker.skyblock;

import com.mojang.blaze3d.systems.RenderSystem;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class TeleportOverlay {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final float[] COLOR_COMPONENTS = { 118f / 255f, 21f / 255f, 148f / 255f };

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(TeleportOverlay::define);
	}

	private static void define(WorldRenderContext wrc) {
		if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.teleportOverlay.enableTeleportOverlays) {
			ItemStack heldItem = CLIENT.player.getMainHandStack();
			String itemId = PriceInfoTooltip.getInternalNameFromNBT(heldItem);
			NbtCompound nbt = heldItem.getNbt();

			if (itemId != null) {
				switch (itemId) {
					case "ASPECT_OF_THE_LEECH_1" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableWeirdTransmission) {
							render(wrc, 3);
						}
					}
					case "ASPECT_OF_THE_LEECH_2" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableWeirdTransmission) {
							render(wrc, 4);
						}
					}
					case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableEtherTransmission && nbt.getCompound("ExtraAttributes").getInt("ethermerge") == 1 && CLIENT.options.sneakKey.isPressed()) {
							render(wrc, (nbt.getCompound("ExtraAttributes").contains("tuned_transmission")) ? 57 + nbt.getCompound("ExtraAttributes").getInt("tuned_transmission") : 57);
						}
						else if (SkyblockerConfig.get().general.teleportOverlay.enableInstantTransmission) {
							render(wrc, (nbt.getCompound("ExtraAttributes").contains("tuned_transmission")) ? 8 + nbt.getCompound("ExtraAttributes").getInt("tuned_transmission") : 8);
						}
					}
					case "ETHERWARP_CONDUIT" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableEtherTransmission) {
							render(wrc, (nbt.getCompound("ExtraAttributes").contains("tuned_transmission")) ? 57 + nbt.getCompound("ExtraAttributes").getInt("tuned_transmission") : 57);
						}
					}
					case "SINSEEKER_SCYTHE" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableSinrecallTransmission) {
							render(wrc, (nbt.getCompound("ExtraAttributes").contains("tuned_transmission")) ? 4 + nbt.getCompound("ExtraAttributes").getInt("tuned_transmission") : 4);
						}
					}
					case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
						if (SkyblockerConfig.get().general.teleportOverlay.enableWitherImpact) {
							render(wrc, 10);
						}
					}
				}
			}
		}
	}

	private static void render(WorldRenderContext wrc, double range) {
		HitResult result = CLIENT.player.raycast(range, wrc.tickDelta(), false);

		if (result instanceof BlockHitResult blockHit) {
			BlockPos pos = blockHit.getBlockPos();
			BlockState state = CLIENT.world.getBlockState(pos);
			if (state.getBlock() != Blocks.AIR && CLIENT.world.getBlockState(pos.up()).getBlock() == Blocks.AIR && CLIENT.world.getBlockState(pos.up(2)).getBlock() == Blocks.AIR) {
				RenderSystem.polygonOffset(-1f, -10f);
				RenderSystem.enablePolygonOffset();

				RenderHelper.renderFilledIfVisible(wrc, pos, COLOR_COMPONENTS, 0.5f);

				RenderSystem.polygonOffset(0f, 0f);
				RenderSystem.disablePolygonOffset();
			}
		}
	}
}
