package de.hysky.skyblocker.debug;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SnapshotDebug {
	private static final float[] RED = { 1.0f, 0.0f, 0.0f };
	private static final float ALPHA = 0.5f;
	private static final float LINE_WIDTH = 8f;

	private static boolean isInSnapshot() {
		return !SharedConstants.getGameVersion().isStable();
	}

	static void init() {
		if (isInSnapshot()) {
			WorldRenderEvents.AFTER_TRANSLUCENT.register(SnapshotDebug::renderTest);
		}
	}

	private static void renderTest(WorldRenderContext wrc) {
		RenderHelper.renderFilledWithBeaconBeam(wrc, new BlockPos(-3, 63, 5), RED, ALPHA, true);
		RenderHelper.renderLinesFromPoints(wrc, new Vec3d[] { new Vec3d(-2, 65, 6.5), new Vec3d(3, 65, 6.5) }, RED, ALPHA, LINE_WIDTH, false);
		RenderHelper.renderLineFromCursor(wrc, new Vec3d(-2.5, 63.5, 5.5), RED, ALPHA, LINE_WIDTH);
		RenderHelper.renderQuad(wrc, new Vec3d[] { new Vec3d(3, 66, 3), new Vec3d(3, 63, 3), new Vec3d(3, 63, 5), new Vec3d(3, 66, 5) }, RED, ALPHA, false);
		RenderHelper.renderText(wrc, Text.of("Skyblocker on " + SharedConstants.getGameVersion().getName() + "!"), new Vec3d(0.5, 66.5, 6.5), false);
	}
}
