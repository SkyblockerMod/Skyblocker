package de.hysky.skyblocker.debug;

import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SnapshotDebug {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final float[] RED = { 1.0f, 0.0f, 0.0f };
	private static final float ALPHA = 0.5f;
	private static final int CYAN = ARGB.color(ARGB.as8BitChannel(ALPHA), CommonColors.HIGH_CONTRAST_DIAMOND);
	private static final int BLUE = ARGB.color(ARGB.as8BitChannel(ALPHA), CommonColors.BLUE);
	private static final float LINE_WIDTH = 8f;
	public static final long AARON_WORLD_SEED = 5629719634239627355L;

	public static boolean isInSnapshot() {
		return !SharedConstants.getCurrentVersion().stable();
	}

	static void init() {
		if (Debug.debugEnabled()) {
			WorldRenderExtractionCallback.EVENT.register(SnapshotDebug::extractRendering);
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (getSeed() == AARON_WORLD_SEED) {
			collector.submitFilledBoxWithBeaconBeam(new BlockPos(175, 63, -14), RED, ALPHA, true);
			collector.submitLinesFromPoints(new Vec3[] { new Vec3(173, 66, -7.5), new Vec3(178, 66, -7.5) }, RED, ALPHA, LINE_WIDTH, false);
			collector.submitQuad(new Vec3[] { new Vec3(183, 66, -16), new Vec3(183, 63, -16), new Vec3(183, 63, -14), new Vec3(183, 66, -14) }, RED, ALPHA, false);
			collector.submitText(Component.nullToEmpty("Skyblocker on " + SharedConstants.getCurrentVersion().name() + "!"), new Vec3(175.5, 67.5, -7.5), false);
			collector.submitCylinder(new BlockPos(172, 78, 44).getCenter(), 12, 12, 32, CYAN);
			collector.submitCylinder(new BlockPos(144, 78, 44).getCenter(), 12, 12, 32, BLUE);
			collector.submitBlockHologram(new BlockPos(183, 65, 9), Blocks.DIAMOND_BLOCK.defaultBlockState(), 0.5f);
		} else if (isInSnapshot()) {
			collector.submitFilledBoxWithBeaconBeam(new BlockPos(-3, 63, 5), RED, ALPHA, true);
			collector.submitOutlinedBox(new BlockPos(-3, 63, 5), RED, 5, true); // Use waypoint default line width
			collector.submitLinesFromPoints(new Vec3[] { new Vec3(-2, 65, 6.5), new Vec3(3, 65, 6.5) }, RED, ALPHA, LINE_WIDTH, false);
			collector.submitLineFromCursor(new Vec3(-2.5, 63.5, 5.5), RED, ALPHA, LINE_WIDTH);
			collector.submitQuad(new Vec3[] { new Vec3(3, 66, 3), new Vec3(3, 63, 3), new Vec3(3, 63, 5), new Vec3(3, 66, 5) }, RED, ALPHA, false);
			collector.submitText(Component.nullToEmpty("Skyblocker on " + SharedConstants.getCurrentVersion().name() + "!"), new Vec3(0.5, 66.5, 6.5), false);
		}
	}

	private static long getSeed() {
		return CLIENT.hasSingleplayerServer() ? CLIENT.getSingleplayerServer().overworld().getSeed() : 0L;
	}
}
