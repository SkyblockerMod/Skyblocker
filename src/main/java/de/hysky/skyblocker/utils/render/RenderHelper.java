package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
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

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.primitive.BlockHologramFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.CursorLineFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.CylinderFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.FilledBoxFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.FilledBoxInstancedFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.FilledCircleFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.LinesFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.OutlinedBoxFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.OutlinedBoxInstancedFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.OutlinedCircleFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollectorImpl;
import de.hysky.skyblocker.utils.render.primitive.QuadFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.SphereFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.TextFeatureRenderer;
import de.hysky.skyblocker.utils.render.primitive.TexturedQuadFeatureRenderer;

public class RenderHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static PrimitiveCollectorImpl collector;

	@Init
	public static void init() {
		LevelExtractionEvents.END_EXTRACTION.register(RenderHelper::startExtraction);
		LevelRenderEvents.COLLECT_SUBMITS.register(RenderHelper::dispatchSubmits);
		FeatureRendererRegistry.register(FilledBoxInstancedFeatureRenderer.TYPE, FilledBoxInstancedFeatureRenderer::new);
		FeatureRendererRegistry.register(FilledBoxFeatureRenderer.TYPE, FilledBoxFeatureRenderer::new);
		FeatureRendererRegistry.register(OutlinedBoxInstancedFeatureRenderer.TYPE, OutlinedBoxInstancedFeatureRenderer::new);
		FeatureRendererRegistry.register(OutlinedBoxFeatureRenderer.TYPE, OutlinedBoxFeatureRenderer::new);
		FeatureRendererRegistry.register(LinesFeatureRenderer.TYPE, LinesFeatureRenderer::new);
		FeatureRendererRegistry.register(CursorLineFeatureRenderer.TYPE, CursorLineFeatureRenderer::new);
		FeatureRendererRegistry.register(QuadFeatureRenderer.TYPE, QuadFeatureRenderer::new);
		FeatureRendererRegistry.register(TexturedQuadFeatureRenderer.TYPE, TexturedQuadFeatureRenderer::new);
		FeatureRendererRegistry.register(BlockHologramFeatureRenderer.TYPE, BlockHologramFeatureRenderer::new);
		FeatureRendererRegistry.register(TextFeatureRenderer.TYPE, TextFeatureRenderer::new);
		FeatureRendererRegistry.register(CylinderFeatureRenderer.TYPE, CylinderFeatureRenderer::new);
		FeatureRendererRegistry.register(SphereFeatureRenderer.TYPE, SphereFeatureRenderer::new);
		FeatureRendererRegistry.register(FilledCircleFeatureRenderer.TYPE, FilledCircleFeatureRenderer::new);
		FeatureRendererRegistry.register(OutlinedCircleFeatureRenderer.TYPE, OutlinedCircleFeatureRenderer::new);
	}

	private static void startExtraction(LevelExtractionContext context) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("skyblockerPrimitiveCollection");
		collector = new PrimitiveCollectorImpl(context.levelState(), context.levelState().cameraRenderState.cullFrustum);
		LevelRenderExtractionCallback.EVENT.invoker().onExtract(collector);
		collector.endCollection();
		profiler.pop();
	}

	private static void dispatchSubmits(LevelRenderContext context) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("skyblockerDispatchSubmits");
		collector.dispatchSubmits(context.levelState(), context.submitNodeCollector());
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
		return CLIENT.gameRenderer.mainCamera();
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
