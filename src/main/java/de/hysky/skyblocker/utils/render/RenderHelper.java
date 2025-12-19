package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollectorImpl;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class RenderHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static PrimitiveCollectorImpl collector;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_SETUP.register(RenderHelper::startFrame);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(RenderHelper::executeDraws);
	}

	private static void startFrame(WorldRenderContext context) {
		collector = new PrimitiveCollectorImpl();

		WorldRenderExtractionCallback.EVENT.invoker().onExtract(collector);
		collector.endCollection();
	}

	private static void executeDraws(WorldRenderContext context) {
		Profiler profiler = Profilers.get();

		profiler.push("skyblockerSubmitPrimitives");
		CameraRenderState cameraState = new CameraRenderState();
		cameraState.pos = context.camera().getPos();
		cameraState.rotation = new Quaternionf(context.camera().getRotation());
		cameraState.pitch = context.camera().getPitch();
		cameraState.yaw = context.camera().getYaw();

		collector.dispatchPrimitivesToRenderers(cameraState);
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

	public static RenderTickCounter getTickCounter() {
		return CLIENT.getRenderTickCounter();
	}

	public static Camera getCamera() {
		return CLIENT.gameRenderer.getCamera();
	}

	/**
	 * Retrieves the bounding box of a block in the world.
	 *
	 * @param world The client world.
	 * @param pos   The position of the block.
	 * @return The bounding box of the block.
	 */
	@Nullable
	public static Box getBlockBoundingBox(ClientWorld world, BlockPos pos) {
		return getBlockBoundingBox(world, world.getBlockState(pos), pos);
	}

	@Nullable
	public static Box getBlockBoundingBox(ClientWorld world, BlockState state, BlockPos pos) {
		VoxelShape shape = state.getOutlineShape(world, pos).asCuboid();

		return shape.isEmpty() ? null : shape.getBoundingBox().offset(pos);
	}

	//The method names for TextureSetup are very... odd and misleading...

	/**
	 * Returns a {@code TextureSetup} with a single texture input only.
	 */
	public static TextureSetup singleTexture(GpuTextureView texture) {
		return TextureSetup.withoutGlTexture(texture);
	}

	/**
	 * Returns a {@code TextureSetup} with the texture input and a lightmap.
	 */
	public static TextureSetup textureWithLightmap(GpuTextureView texture) {
		return TextureSetup.of(texture);
	}
}
