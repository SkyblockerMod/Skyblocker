package de.hysky.skyblocker.skyblock.teleport;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class TeleportOverlay {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static float[] colorComponents;

	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.teleportOverlayColor); // Initialize colorComponents from the config value
		WorldRenderExtractionCallback.EVENT.register(TeleportOverlay::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableTeleportOverlays && client.player != null && client.world != null) {
			ItemStack heldItem = client.player.getMainHandStack();
			String itemId = heldItem.getSkyblockId();
			NbtCompound customData = ItemUtils.getCustomData(heldItem);

			switch (itemId) {
				case "ASPECT_OF_THE_LEECH_1" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
						extractRendering(collector, 3);
					}
				}
				case "ASPECT_OF_THE_LEECH_2" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
						extractRendering(collector, 4);
					}
				}
				case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission && client.options.sneakKey.isPressed() && customData.getInt("ethermerge", 0) == 1) {
						extractRendering(collector, customData, 57);
					} else if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableInstantTransmission) {
						extractRendering(collector, customData, 8);
					}
				}
				case "ETHERWARP_CONDUIT" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission) {
						extractRendering(collector, customData, 57);
					}
				}
				case "SINSEEKER_SCYTHE" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableSinrecallTransmission) {
						extractRendering(collector, customData, 4);
					}
				}
				case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWitherImpact) {
						extractRendering(collector, 10);
					}
				}
			}
		}
	}

	/**
	 * Renders the teleport overlay with a given base range and the tuned transmission stat.
	 */
	private static void extractRendering(PrimitiveCollector collector, NbtCompound customData, int baseRange) {
		extractRendering(collector, customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getInt("tuned_transmission", 0) : baseRange);
	}

	/**
	 * Renders the teleport overlay with a given range. Uses {@link PredictiveSmoothAOTE#raycast(int, Vec3d, Vec3d)} to predict the target
	 *
	 * @implNote {@link MinecraftClient#player} and {@link MinecraftClient#world} must not be null when calling this method.
	 */
	private static void extractRendering(PrimitiveCollector collector, int range) {
		if (client.player == null || client.world == null) return;
		//set up values for smooth AOTEs raycast
		float pitch = client.player.getPitch();
		float yaw = client.player.getYaw();
		Vec3d look = client.player.getRotationVector(pitch, yaw);
		Vec3d startPos = client.player.getPos().add(0, 1.62, 0);
		Vec3d raycast = PredictiveSmoothAOTE.raycast(range, look, startPos);

		if (raycast != null) {
			BlockPos target = BlockPos.ofFloored(startPos.add(raycast)).down();
			//do not render if in air and disabled in air
			if (!SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.showWhenInAir && client.world.getBlockState(target).isAir()) return;
			//render they highlight
			collector.submitFilledBox(target, colorComponents, colorComponents[3], false);
		}
	}

	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}
}
