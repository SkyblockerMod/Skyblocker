package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.BeaconBlockEntityRendererInvoker;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderHelper {
    private static final Identifier TRANSLUCENT_DRAW = Identifier.of(SkyblockerMod.NAMESPACE, "translucent_draw");
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @Init
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.addPhaseOrdering(Event.DEFAULT_PHASE, TRANSLUCENT_DRAW);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(TRANSLUCENT_DRAW, RenderHelper::drawTranslucents);
    }

    public static void renderFilledWithBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, pos, colorComponents, alpha, throughWalls);
        renderBeaconBeam(context, pos, colorComponents);
    }

    public static void renderFilled(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, Vec3d pos, Vec3d dimensions, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, Box box, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colorComponents, float alpha, boolean throughWalls) {
    	if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
    		renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, throughWalls);
    	}
    }

    private static void renderFilledInternal(WorldRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colorComponents, float alpha, boolean throughWalls) {
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        BufferBuilder buffer = Renderer.getBuffer(throughWalls ? SkyblockerRenderPipelines.FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX);

        VertexRendering.drawFilledBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0], colorComponents[1], colorComponents[2], alpha);

        matrices.pop();
    }

    public static void renderBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents) {
        if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());

            float length = (float) camera.subtract(pos.toCenterPos()).horizontalLength();
            float scale = CLIENT.player != null && CLIENT.player.isUsingSpyglass() ? 1.0f : Math.max(1.0f, length / 96.0f);

            BeaconBlockEntityRendererInvoker.renderBeam(matrices, context.consumers(), context.tickCounter().getTickProgress(true), scale, context.world().getTime(), 0, MAX_OVERWORLD_BUILD_HEIGHT, ColorHelper.fromFloats(1f, colorComponents[0], colorComponents[1], colorComponents[2]));

            matrices.pop();
        }
    }

    public static void renderOutline(WorldRenderContext context, BlockPos pos, float[] colorComponents, float lineWidth, boolean throughWalls) {
        renderOutline(context, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Vec3d pos, Vec3d dimensions, float[] colorComponents, float lineWidth, boolean throughWalls) {
        renderOutline(context, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Box box, float[] colorComponents, float lineWidth, boolean throughWalls) {
        renderOutline(context, box, colorComponents, 1f, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, Box box, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        renderOutline(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorComponents, alpha, lineWidth, throughWalls);
    }

    public static void renderOutline(WorldRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

            RenderPipeline pipeline = throughWalls ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES;
            BufferBuilder buffer = Renderer.getBuffer(pipeline, lineWidth);

            VertexRendering.drawBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0], colorComponents[1], colorComponents[2], alpha);

            matrices.pop();
        }
    }

    /**
     * Draws lines from point to point.<br><br>
     * <p>
     * Tip: To draw lines from the center of a block, offset the X, Y and Z each by 0.5
     * <p>
     * Note: This is super messed up when drawing long lines. Tried different normals and {@link DrawMode#LINES} but nothing worked.
     *
     * @param context         The WorldRenderContext which supplies the matrices and tick delta
     * @param points          The points from which to draw lines between
     * @param colorComponents An array of R, G and B color components
     * @param alpha           The alpha of the lines
     * @param lineWidth       The width of the lines
     * @param throughWalls    Whether to render through walls or not
     */
    public static void renderLinesFromPoints(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        MatrixStack.Entry entry = matrices.peek();

        RenderPipeline pipeline = throughWalls ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES;
        BufferBuilder buffer = Renderer.getBuffer(pipeline, lineWidth);

        for (int i = 0; i < points.length; i++) {
            Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
            Vector3f normalVec = nextPoint.toVector3f().sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).normalize();
            // If the last point, the normal is the previous point minus the current point.
            // Negate the normal to make it point forward, away from the previous point.
            if (i + 1 == points.length) {
                normalVec.negate();
            }

            buffer
                    .vertex(entry, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
                    .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                    .normal(entry, normalVec);
        }

        matrices.pop();
    }

    public static void renderLineFromCursor(WorldRenderContext context, Vec3d point, float[] colorComponents, float alpha, float lineWidth) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        MatrixStack.Entry entry = matrices.peek();

        RenderPipeline pipeline = SkyblockerRenderPipelines.LINES_THROUGH_WALLS;
        BufferBuilder buffer = Renderer.getBuffer(pipeline, lineWidth);

        // Start drawing the line from a point slightly in front of the camera
        Vec3d cameraPoint = camera.add(Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw()));
        Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();

        buffer
                .vertex(entry, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);

        buffer
                .vertex(entry, (float) point.getX(), (float) point.getY(), (float) point.getZ())
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);

        matrices.pop();
    }

    public static void renderQuad(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, boolean throughWalls) {
        Matrix4f positionMatrix = new Matrix4f();
        Vec3d camera = context.camera().getPos();

        positionMatrix.translate((float) -camera.x, (float) -camera.y, (float) -camera.z);

        RenderPipeline pipeline = throughWalls ? SkyblockerRenderPipelines.QUADS_THROUGH_WALLS : RenderPipelines.DEBUG_QUADS;
        BufferBuilder buffer = Renderer.getBuffer(pipeline);

        for (int i = 0; i < 4; i++) {
            buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);
        }
    }

	/**
	 * Renders a texture in world space facing the player (like a name tag)
	 * @param context world render context
	 * @param pos world position
	 * @param width rendered width
	 * @param height rendered height
	 * @param textureWidth amount of texture rendered width
	 * @param textureHeight amount of texture rendered height
	 * @param renderOffset offset once it's been placed in the world facing the player
	 * @param texture reference to texture to render
	 * @param shaderColor color to apply to the texture (use white if none)
	 * @param throughWalls if it should render though walls
	 */
	public static void renderTextureInWorld(WorldRenderContext context, Vec3d pos, float width, float height, float textureWidth, float textureHeight, Vec3d renderOffset, Identifier texture, float[] shaderColor, float alpha, boolean throughWalls) {
		Matrix4f positionMatrix = new Matrix4f();
		Camera camera = context.camera();
		Vec3d cameraPos = camera.getPos();

		positionMatrix
				.translate((float) (pos.getX() - cameraPos.getX()), (float) (pos.getY() - cameraPos.getY()), (float) (pos.getZ() - cameraPos.getZ()))
				.rotate(camera.getRotation());

		RenderPipeline pipeline = throughWalls ? SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS : SkyblockerRenderPipelines.TEXTURE;
		BufferBuilder buffer = Renderer.getBuffer(pipeline, CLIENT.getTextureManager().getTexture(texture).getGlTextureView());

		int color = ColorHelper.fromFloats(alpha, shaderColor[0], shaderColor[1], shaderColor[2]);

		buffer.vertex(positionMatrix, (float) renderOffset.getX(), (float) renderOffset.getY(), (float) renderOffset.getZ()).texture(1, 1 - textureHeight).color(color);
		buffer.vertex(positionMatrix, (float) renderOffset.getX(), (float) renderOffset.getY() + height, (float) renderOffset.getZ()).texture(1, 1).color(color);
		buffer.vertex(positionMatrix, (float) renderOffset.getX() + width, (float) renderOffset.getY() + height, (float) renderOffset.getZ()).texture(1 - textureWidth, 1).color(color);
		buffer.vertex(positionMatrix, (float) renderOffset.getX() + width, (float) renderOffset.getY(), (float) renderOffset.getZ()).texture(1 - textureWidth, 1 - textureHeight).color(color);
	}

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, boolean throughWalls) {
        renderText(context, text, pos, 1, throughWalls);
    }

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, float scale, boolean throughWalls) {
        renderText(context, text, pos, scale, 0, throughWalls);
    }

    public static void renderText(WorldRenderContext context, Text text, Vec3d pos, float scale, float yOffset, boolean throughWalls) {
        renderText(context, text.asOrderedText(), pos, scale, yOffset, throughWalls);
    }

    /**
     * Renders text in the world space.
     *
     * @param throughWalls whether the text should be able to be seen through walls or not.
     */
    public static void renderText(WorldRenderContext context, OrderedText text, Vec3d pos, float scale, float yOffset, boolean throughWalls) {
        Matrix4f positionMatrix = new Matrix4f();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        TextRenderer textRenderer = CLIENT.textRenderer;

        scale *= 0.025f;

        positionMatrix
                .translate((float) (pos.getX() - cameraPos.getX()), (float) (pos.getY() - cameraPos.getY()), (float) (pos.getZ() - cameraPos.getZ()))
                .rotate(camera.getRotation())
                .scale(scale, -scale, scale);

        float xOffset = -textRenderer.getWidth(text) / 2f;

        textRenderer.draw(text, xOffset, yOffset, 0xFFFFFFFF, false, positionMatrix, context.consumers(), throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        ((VertexConsumerProvider.Immediate) context.consumers()).draw();
    }

    /**
     * Renders a cylinder without the top or bottom faces.
     *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the cylinder.
     * @param height   The total height of the cylinder with {@code pos} as the midpoint.
     * @param segments The number of triangles used to approximate the circle.
     */
    public static void renderCylinder(WorldRenderContext context, Vec3d centre, float radius, float height, int segments, int color) {
    	MatrixStack matrices = context.matrixStack();
    	Vec3d camera = context.camera().getPos();

    	matrices.push();
    	matrices.translate(-camera.x, -camera.y, -camera.z);

    	BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CYLINDER);
    	Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
    	float halfHeight = height / 2.0f;

    	for (int i = 0; i <= segments; i++) {
    		double angle = Math.TAU * i / segments;
    		float dx = (float) Math.cos(angle) * radius;
    		float dz = (float) Math.sin(angle) * radius;

    		buffer.vertex(positionMatrix, (float) centre.getX() + dx, (float) centre.getY() + halfHeight, (float) centre.getZ() + dz).color(color);
    		buffer.vertex(positionMatrix, (float) centre.getX() + dx, (float) centre.getY() - halfHeight, (float) centre.getZ() + dz).color(color);
    	}

    	matrices.pop();
    }

	/**
	 * Renders a circle filled in using triangle_fan drawmode
	 *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the circle.
	 * @param segments The number of triangles used to approximate the circle.
	 * @param color    The color of the circle as an argb int.
	 */
	public static void renderCircleFilled(WorldRenderContext context, Vec3d centre, float radius, int segments, int color) {
		MatrixStack matrices = context.matrixStack();
		Vec3d camera = context.camera().getPos();

		matrices.push();
		matrices.translate(-camera.x, -camera.y, -camera.z);
		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CIRCLE);
		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

		for (int i = 0; i <= segments; i++) {
			double angle = Math.TAU * i / segments;
			float dx = (float) Math.cos(angle) * radius;
			float dz = (float) Math.sin(angle) * radius;

			buffer.vertex(positionMatrix, (float) centre.getX() + dx, (float) centre.getY(), (float) centre.getZ() + dz).color(color);
		}

		matrices.pop();
	}

	/**
	 * Renders a circle sphere in using triange_strip
	 *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the sphere.
	 * @param rings    The number of rings to subdivide the sphere.
	 * @param segments The number of triangles used to approximate the circle.
	 * @param color    The color of the circle as an argb int.
	 */
	public static void renderSphere(WorldRenderContext context, Vec3d centre, float radius, int segments, int rings, int color) {
		MatrixStack matrices = context.matrixStack();
		Vec3d camera = context.camera().getPos();

		matrices.push();
		matrices.translate(-camera.x, -camera.y, -camera.z);

		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CYLINDER);
		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

		for (int lat = 0; lat < rings; lat++) {
			double lat0 = Math.PI * (double) lat / rings;
			double lat1 = Math.PI * (double) (lat + 1) / rings;

			float y0 = (float) Math.cos(lat0) * radius;
			float y1 = (float) Math.cos(lat1) * radius;

			float r0 = (float) Math.sin(lat0) * radius;
			float r1 = (float) Math.sin(lat1) * radius;

			for (int lon = 0; lon <= segments; lon++) {
				double angle = Math.TAU * (double) lon / segments;
				float x0 = (float) Math.cos(angle);
				float z0 = (float) Math.sin(angle);

				// First triangle
				buffer.vertex(positionMatrix,
								Math.fma(x0, r0, (float) centre.getX()),
								(float) centre.getY() + y0,
								Math.fma(z0, r0, (float) centre.getZ()))
						.color(color);

				buffer.vertex(positionMatrix,
								Math.fma(x0, r1, (float) centre.getX()),
								(float) centre.getY() + y1,
								Math.fma(z0, r1, (float) centre.getZ()))
						.color(color);
			}
		}

		matrices.pop();
	}

	/**
	 * Renders a circle outline in using quads
	 *
	 * @param centre    The position that the circle will be centred around.
	 * @param radius    The radius of the circle.
	 * @param thickness The thickness of the outline in blocks.
	 * @param segments  The number of triangles used to approximate the circle.
	 * @param color     The color of the circle as an argb int.
	 */
	public static void renderCircleOutlineWithQuads(WorldRenderContext context, Vec3d centre, float radius, float thickness, int segments, int color) {
		MatrixStack matrices = context.matrixStack();
		Vec3d camera = context.camera().getPos();

		matrices.push();
		matrices.translate(-camera.x, -camera.y, -camera.z);

		BufferBuilder buffer = Renderer.getBuffer(SkyblockerRenderPipelines.CIRCLE_LINES);
		Matrix4f positionMatrix = matrices.peek().getPositionMatrix();


		float innerRadius = radius - thickness / 2f;
		float outerRadius = radius + thickness / 2f;

		for (int i = 0; i < segments; i++) {
			double angle1 = Math.TAU * i / segments;
			double angle2 = Math.TAU * (i + 1) / segments;

			float x1Inner = (float) Math.cos(angle1) * innerRadius;
			float z1Inner = (float) Math.sin(angle1) * innerRadius;

			float x1Outer = (float) Math.cos(angle1) * outerRadius;
			float z1Outer = (float) Math.sin(angle1) * outerRadius;

			float x2Inner = (float) Math.cos(angle2) * innerRadius;
			float z2Inner = (float) Math.sin(angle2) * innerRadius;

			float x2Outer = (float) Math.cos(angle2) * outerRadius;
			float z2Outer = (float) Math.sin(angle2) * outerRadius;

			float cx = (float) centre.getX();
			float cy = (float) centre.getY();
			float cz = (float) centre.getZ();

			// Each quad is formed from two triangles
			buffer.vertex(positionMatrix, cx + x1Inner, cy, cz + z1Inner).color(color);
			buffer.vertex(positionMatrix, cx + x1Outer, cy, cz + z1Outer).color(color);
			buffer.vertex(positionMatrix, cx + x2Outer, cy, cz + z2Outer).color(color);
			buffer.vertex(positionMatrix, cx + x2Inner, cy, cz + z2Inner).color(color);
		}

		matrices.pop();
	}

	/**
     * This is called after all {@link WorldRenderEvents#AFTER_TRANSLUCENT} listeners have been called, this is used for drawing all buffered objects.
     */
    private static void drawTranslucents(WorldRenderContext context) {
    	Profiler profiler = Profilers.get();

    	profiler.push("skyblockerDraw");
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
}
