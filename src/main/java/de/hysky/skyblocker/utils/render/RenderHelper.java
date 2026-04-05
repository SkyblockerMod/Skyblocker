package de.hysky.skyblocker.utils.render;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollectorImpl;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static PrimitiveCollectorImpl collector;

	@Init
	public static void init() {
		LevelRenderEvents.END_EXTRACTION.register(RenderHelper::startExtraction);
		LevelRenderEvents.COLLECT_SUBMITS.register(RenderHelper::submitVanillaSubmittables);
		LevelRenderEvents.END_MAIN.register(RenderHelper::executeDraws);
	}

	private static void startExtraction(LevelExtractionContext context) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("skyblockerPrimitiveCollection");
		collector = new PrimitiveCollectorImpl(context.levelState(), context.levelState().cameraRenderState.cullFrustum);
		LevelRenderExtractionCallback.EVENT.invoker().onExtract(collector);
		collector.endCollection();
		profiler.pop();
	}

	private static void submitVanillaSubmittables(LevelRenderContext context) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("skyblockerSubmitVanillaSubmittables");
		collector.dispatchVanillaSubmittables(context.levelState(), context.submitNodeCollector());
		profiler.pop();
	}

	private static void executeDraws(LevelRenderContext context) {
		ProfilerFiller profiler = Profiler.get();

		profiler.push("skyblockerSubmitPrimitives");
		collector.dispatchPrimitivesToRenderers(context.levelState().cameraRenderState);
		collector = null;
		profiler.pop();

		profiler.push("skyblockerExecuteDraws");
		Renderer.executeDraws();
		profiler.pop();
	}

	public static void runOnRenderThread(Runnable runnable) {
		if (RenderSystem.isOnRenderThread()) {
			runnable.run();
		} else {
			CLIENT.execute(runnable);
		}
	}

	/**
	 * A version of {@link RenderSystem#assertOnRenderThread()} that allows for a custom error message.
	 */
	public static void assertOnRenderThread(String message) {
		if (!RenderSystem.isOnRenderThread()) {
			throw new IllegalStateException(message);
		}
	}

	public static DeltaTracker getTickCounter() {
		return CLIENT.getDeltaTracker();
	}

	public static Camera getCamera() {
		return CLIENT.gameRenderer.getMainCamera();
	}

	/**
	 * Retrieves the bounding box of a block in the world.
	 *
	 * @param world The client world.
	 * @param pos   The position of the block.
	 * @return The bounding box of the block.
	 */
	public static @Nullable AABB getBlockBoundingBox(ClientLevel world, BlockPos pos) {
		return getBlockBoundingBox(world, world.getBlockState(pos), pos);
	}

	public static @Nullable AABB getBlockBoundingBox(ClientLevel world, BlockState state, BlockPos pos) {
		VoxelShape shape = state.getShape(world, pos).singleEncompassing();

		return shape.isEmpty() ? null : shape.bounds().move(pos);
	}
}
