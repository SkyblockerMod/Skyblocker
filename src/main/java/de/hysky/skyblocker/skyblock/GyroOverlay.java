package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;


public class GyroOverlay {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static float[] colorComponents;

	private static final int GYRO_RADIUS = 10;
	private static final int SEGMENTS = 32;
	private static final int MAX_REACH = 24;


	// init ofcourse
	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(GyroOverlay::render);
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
	 *
	 * @param wrc The {@link WorldRenderContext} provided during world rendering,
	 *            containing rendering matrices and utilities for rendering custom elements.
	 */
	public static void render(WorldRenderContext wrc) {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!Utils.isOnSkyblock()) return;
		if (SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayMode == Mode.OFF) return;

		String heldItem = CLIENT.player.getMainHandStack().getSkyblockId();
		if (!heldItem.equals("GYROKINETIC_WAND")) return;

		HitResult hit = CLIENT.cameraEntity.raycast(MAX_REACH, MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false), false);
		if (hit.getType() == HitResult.Type.MISS) {
			return;
		}

		int color = ColorHelper.fromFloats(colorComponents[3], colorComponents[0], colorComponents[1], colorComponents[2]);

		switch (SkyblockerConfigManager.get().uiAndVisuals.gyroOverlay.gyroOverlayMode) {
			case OFF -> {}
			case CIRCLE_OUTLINE -> RenderHelper.renderCircleOutlineWithQuads(wrc, hit.getPos().add(new Vec3d(0, 0.1, 0)), GYRO_RADIUS, 0.25f, SEGMENTS, color);
			case CIRCLE -> RenderHelper.renderCircleFilled(wrc, hit.getPos().add(new Vec3d(0, 0.1, 0)), GYRO_RADIUS, SEGMENTS, color);
			case SPHERE -> RenderHelper.renderSphere(wrc, hit.getPos(), GYRO_RADIUS, SEGMENTS, SEGMENTS, color);
		}
	}

	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}

	public enum Mode implements StringIdentifiable {
		OFF("OFF"),
		CIRCLE("CIRCLE"),
		CIRCLE_OUTLINE("CIRCLE_OUTLINE"),
		SPHERE("SPHERE");

		private final String key;

		Mode(String key) {
			this.key = "skyblocker.config.uiAndVisuals.gyroOverlay.mode." + key;
		}

		@Override
		public String asString() {
			return name().toLowerCase();
		}

		@Override
		public String toString() {
			return I18n.translate(this.key);
		}
	}
}
