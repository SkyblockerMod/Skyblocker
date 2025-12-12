package de.hysky.skyblocker.skyblock.teleport;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public class TeleportOverlay {
	private static final Minecraft client = Minecraft.getInstance();
	private static float[] colorComponents;

	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.teleportOverlayColor); // Initialize colorComponents from the config value
		WorldRenderExtractionCallback.EVENT.register(TeleportOverlay::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableTeleportOverlays && client.player != null && client.level != null) {
			ItemStack heldItem = client.player.getMainHandItem();
			String itemId = heldItem.getSkyblockId();
			CompoundTag customData = ItemUtils.getCustomData(heldItem);

			switch (itemId) {
				case "ASPECT_OF_THE_LEECH_1" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
						extractRendering(collector, 3, false);
					}
				}
				case "ASPECT_OF_THE_LEECH_2" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
						extractRendering(collector, 4, false);
					}
				}
				case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission && client.options.keyShift.isDown() && customData.getIntOr("ethermerge", 0) == 1) {
						extractRendering(collector, customData, 57, true);
					} else if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableInstantTransmission) {
						extractRendering(collector, customData, 8, false);
					}
				}
				case "ETHERWARP_CONDUIT" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission) {
						extractRendering(collector, customData, 57, true);
					}
				}
				case "SINSEEKER_SCYTHE" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableSinrecallTransmission) {
						extractRendering(collector, customData, 4, false);
					}
				}
				case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
					if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWitherImpact) {
						extractRendering(collector, 10, false);
					}
				}
			}
		}
	}

	/**
	 * Renders the teleport overlay with a given base range and the tuned transmission stat.
	 */
	private static void extractRendering(PrimitiveCollector collector, CompoundTag customData, int baseRange, boolean isEtherwarp) {
		extractRendering(collector, customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getIntOr("tuned_transmission", 0) : baseRange, isEtherwarp);
	}

	/**
	 * Renders the teleport overlay with a given range. Uses {@link PredictiveSmoothAOTE#raycast(int, Vec3, Vec3, boolean)} to predict the target
	 *
	 * @implNote {@link Minecraft#player} and {@link Minecraft#level} must not be null when calling this method.
	 */
	private static void extractRendering(PrimitiveCollector collector, int range, boolean isEtherwarp) {
		if (client.player == null || client.level == null) return;
		//set up values for smooth AOTEs raycast
		float pitch = client.player.getXRot();
		float yaw = client.player.getYRot();
		Vec3 look = client.player.calculateViewVector(pitch, yaw);
		Vec3 startPos = client.player.position().add(0, Utils.getEyeHeight(client.player), 0);
		Vec3 raycast = PredictiveSmoothAOTE.raycast(range, look, startPos, isEtherwarp);

		if (raycast != null) {
			BlockPos target = BlockPos.containing(startPos.add(raycast));
			if (isEtherwarp) {
				if (!client.level.getBlockState(target.above()).isAir()) return;
				if (!client.level.getBlockState(target.above(2)).isAir()) return;
			} else {
				target = target.below();
			}
			if (!SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.showWhenInAir && client.level.getBlockState(target).isAir()) return;
			collector.submitFilledBox(target, colorComponents, colorComponents[3], false);
		}
	}

	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}
}
