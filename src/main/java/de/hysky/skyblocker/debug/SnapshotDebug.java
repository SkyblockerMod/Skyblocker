package de.hysky.skyblocker.debug;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

public class SnapshotDebug {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final float[] RED = { 1.0f, 0.0f, 0.0f };
	private static final float ALPHA = 0.5f;
	private static final int CYAN = ColorHelper.withAlpha(ColorHelper.channelFromFloat(ALPHA), Colors.CYAN);
	private static final int BLUE = ColorHelper.withAlpha(ColorHelper.channelFromFloat(ALPHA), Colors.BLUE);
	private static final float LINE_WIDTH = 8f;
	public static final long AARON_WORLD_SEED = 5629719634239627355L;

	public static boolean isInSnapshot() {
		return !SharedConstants.getGameVersion().stable();
	}

	static void init() {
		if (Debug.debugEnabled()) {
			WorldRenderEvents.AFTER_TRANSLUCENT.register(SnapshotDebug::renderTest);
		}
	}

	private static void renderTest(WorldRenderContext wrc) {
		if (getSeed() == AARON_WORLD_SEED) {
			RenderHelper.renderFilledWithBeaconBeam(wrc, new BlockPos(175, 63, -14), RED, ALPHA, true);
			RenderHelper.renderLinesFromPoints(wrc, new Vec3d[] { new Vec3d(173, 66, -7.5), new Vec3d(178, 66, -7.5) }, RED, ALPHA, LINE_WIDTH, false);
			RenderHelper.renderQuad(wrc, new Vec3d[] { new Vec3d(183, 66, -16), new Vec3d(183, 63, -16), new Vec3d(183, 63, -14), new Vec3d(183, 66, -14) }, RED, ALPHA, false);
			RenderHelper.renderText(wrc, Text.of("Skyblocker on " + SharedConstants.getGameVersion().name() + "!"), new Vec3d(175.5, 67.5, -7.5), false);
			RenderHelper.renderCylinder(wrc, new BlockPos(172, 78, 44).toCenterPos(), 12, 12, 32, CYAN);
			RenderHelper.renderCylinder(wrc, new BlockPos(144, 78, 44).toCenterPos(), 12, 12, 32, BLUE);
		} else if (isInSnapshot()) {
			RenderHelper.renderFilledWithBeaconBeam(wrc, new BlockPos(-3, 63, 5), RED, ALPHA, true);
			RenderHelper.renderOutline(wrc, new BlockPos(-3, 63, 5), RED, 5, true); // Use waypoint default line width
			RenderHelper.renderLinesFromPoints(wrc, new Vec3d[] { new Vec3d(-2, 65, 6.5), new Vec3d(3, 65, 6.5) }, RED, ALPHA, LINE_WIDTH, false);
			RenderHelper.renderLineFromCursor(wrc, new Vec3d(-2.5, 63.5, 5.5), RED, ALPHA, LINE_WIDTH);
			RenderHelper.renderQuad(wrc, new Vec3d[] { new Vec3d(3, 66, 3), new Vec3d(3, 63, 3), new Vec3d(3, 63, 5), new Vec3d(3, 66, 5) }, RED, ALPHA, false);
			RenderHelper.renderText(wrc, Text.of("Skyblocker on " + SharedConstants.getGameVersion().name() + "!"), new Vec3d(0.5, 66.5, 6.5), false);
		}
	}

	private static long getSeed() {
		return CLIENT.isIntegratedServerRunning() ? CLIENT.getServer().getOverworld().getSeed() : 0L;
	}
}
