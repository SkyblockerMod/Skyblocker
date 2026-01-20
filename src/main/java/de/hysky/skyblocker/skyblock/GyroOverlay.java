package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GyroOverlay {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static float[] colorComponents;

	private static final int GYRO_RADIUS = 10;
	private static final int SEGMENTS = 32;
	private static final int MAX_REACH = 24;


	// init ofcourse
	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayColor);
		WorldRenderExtractionCallback.EVENT.register(GyroOverlay::extractRendering);
	}

	// render
	/**
	 * Renders a visual overlay in the world when the player is holding the Gyrokinetic Wand on Skyblock.
	 * <p>
	 * The overlay type depends on the configuration setting:
	 * <ul>
	 *     <li>{@code OFF} – does not render anything.</li>
	 *     <li>{@code CIRCLE_OUTLINE} – renders a ring-shaped outline using quads.</li>
	 *     <li>{@code CIRCLE} – renders a filled circle.</li>
	 *     <li>{@code SPHERE} – renders a 3D sphere.</li>
	 * </ul>
	 * The overlay is only rendered if:
	 * <ul>
	 *     <li>The player is in a Skyblock world.</li>
	 *     <li>The overlay mode is not {@code OFF}.</li>
	 *     <li>The player is holding the {@code GYROKINETIC_WAND} in the main hand.</li>
	 *     <li>The raycast from the camera hits a block or entity (i.e., not a miss).</li>
	 * </ul>
	 */
	public static void extractRendering(PrimitiveCollector collector) {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!Utils.isOnSkyblock()) return;
		if (SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayMode == Mode.OFF) return;

		String heldItem = CLIENT.player.getMainHandItem().getSkyblockId();
		if (!heldItem.equals("GYROKINETIC_WAND")) return;

		HitResult hit = CLIENT.getCameraEntity().pick(MAX_REACH, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), false);
		if (hit.getType() == HitResult.Type.MISS) {
			return;
		}

		int color = ARGB.colorFromFloat(colorComponents[3], colorComponents[0], colorComponents[1], colorComponents[2]);

		switch (SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayMode) {
			case OFF -> {}
			case CIRCLE_OUTLINE -> collector.submitOutlinedCircle(hit.getLocation().add(new Vec3(0, 0.1, 0)), GYRO_RADIUS, 0.25f, SEGMENTS, color);
			case CIRCLE -> collector.submitFilledCircle(hit.getLocation().add(new Vec3(0, 0.1, 0)), GYRO_RADIUS, SEGMENTS, color);
			case SPHERE -> collector.submitSphere(hit.getLocation(), GYRO_RADIUS, SEGMENTS, SEGMENTS, color);
		}
	}

	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}

	public enum Mode implements StringRepresentable {
		OFF("OFF"),
		CIRCLE("CIRCLE"),
		CIRCLE_OUTLINE("CIRCLE_OUTLINE"),
		SPHERE("SPHERE");

		private final String key;

		Mode(String key) {
			this.key = "skyblocker.config.uiAndVisuals.gyroOverlay.mode." + key;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String toString() {
			return I18n.get(this.key);
		}
	}
}
