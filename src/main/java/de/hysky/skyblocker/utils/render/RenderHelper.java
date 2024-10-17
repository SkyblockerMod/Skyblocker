package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.BeaconBlockEntityRendererInvoker;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderHelper {
    private static final Identifier TRANSLUCENT_DRAW = Identifier.of(SkyblockerMod.NAMESPACE, "translucent_draw");
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final BufferAllocator ALLOCATOR = new BufferAllocator(1536);

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
        if (throughWalls) {
            if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, true);
            }
        } else {
            if (OcclusionCulling.getRegularCuller().isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, false);
            }
        }
    }

    private static void renderFilledInternal(WorldRenderContext context, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colorComponents, float alpha, boolean throughWalls) {
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer buffer = consumers.getBuffer(throughWalls ? SkyblockerRenderLayers.FILLED_THROUGH_WALLS : SkyblockerRenderLayers.FILLED);

        VertexRendering.drawFilledBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0], colorComponents[1], colorComponents[2], alpha);

        matrices.pop();
    }

    private static void renderBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents) {
        if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());

            BeaconBlockEntityRendererInvoker.renderBeam(matrices, context.consumers(), context.tickCounter().getTickDelta(true), context.world().getTime(), 0, MAX_OVERWORLD_BUILD_HEIGHT, ColorHelper.fromFloats(1f, colorComponents[0], colorComponents[1], colorComponents[2]));

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
            Tessellator tessellator = RenderSystem.renderThreadTesselator();

            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableBlend();
            RenderSystem.lineWidth(lineWidth);
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

            matrices.push();
            matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

            BufferBuilder buffer = tessellator.begin(DrawMode.LINES, VertexFormats.LINES);
            VertexRendering.drawBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0], colorComponents[1], colorComponents[2], alpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            matrices.pop();
            RenderSystem.lineWidth(1f);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
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

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        MatrixStack.Entry entry = matrices.peek();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        BufferBuilder buffer = tessellator.begin(DrawMode.LINE_STRIP, VertexFormats.LINES);

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

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
    }

    public static void renderLineFromCursor(WorldRenderContext context, Vec3d point, float[] colorComponents, float alpha, float lineWidth) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        MatrixStack.Entry entry = matrices.peek();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        // Start drawing the line from a point slightly in front of the camera
        Vec3d cameraPoint = camera.add(Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw()));

        BufferBuilder buffer = tessellator.begin(DrawMode.LINES, VertexFormats.LINES);

        Vector3f normal = point.toVector3f().sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z).normalize();
        buffer
                .vertex(entry, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);

        buffer
                .vertex(entry, (float) point.getX(), (float) point.getY(), (float) point.getZ())
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normal);


        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
    }

    public static void renderQuad(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, boolean throughWalls) {
        Matrix4f positionMatrix = new Matrix4f();
        Vec3d camera = context.camera().getPos();

        positionMatrix.translate((float) -camera.x, (float) -camera.y, (float) -camera.z);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
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
	 * @param shaderColor color to apply to the texture
	 * @param throughWalls if it should render though walls
	 */
	public static void renderTextureInWorld(WorldRenderContext context, Vec3d pos, float width, float height, float textureWidth, float textureHeight, Vec3d renderOffset, Identifier texture, Color shaderColor, boolean throughWalls) {
		Matrix4f positionMatrix = new Matrix4f();
		Camera camera = context.camera();
		Vec3d cameraPos = camera.getPos();

		positionMatrix
				.translate((float) (pos.getX() - cameraPos.getX()), (float) (pos.getY() - cameraPos.getY()), (float) (pos.getZ() - cameraPos.getZ()))
				.rotate(camera.getRotation());

		Tessellator tessellator = RenderSystem.renderThreadTesselator();

		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShaderColor(shaderColor.getRed() / 255f, shaderColor.getGreen() / 255f, shaderColor.getBlue() / 255f, shaderColor.getAlpha() / 255f);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);
		BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		buffer.vertex(positionMatrix, (float) renderOffset.getX(), (float) renderOffset.getY(), (float) renderOffset.getZ()).texture(1, 1 - textureHeight);
		buffer.vertex(positionMatrix, (float) renderOffset.getX(), (float) renderOffset.getY() + height, (float) renderOffset.getZ()).texture(1, 1);
		buffer.vertex(positionMatrix, (float) renderOffset.getX() + width, (float) renderOffset.getY() + height	, (float) renderOffset.getZ()).texture(1 - textureWidth, 1);
		buffer.vertex(positionMatrix, (float) renderOffset.getX() + width, (float) renderOffset.getY(), (float) renderOffset.getZ()).texture(1 - textureWidth, 1 - textureHeight);

		BufferRenderer.drawWithGlobalProgram(buffer.end());

		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.enableCull();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
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

        VertexConsumerProvider.Immediate consumers = VertexConsumerProvider.immediate(ALLOCATOR);

        //Don't trust the render layer to do this for you!
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        textRenderer.draw(text, xOffset, yOffset, 0xFFFFFFFF, false, positionMatrix, consumers, throughWalls ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        consumers.draw();

        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    /**
     * This is called after all {@link WorldRenderEvents#AFTER_TRANSLUCENT} listeners have been called so that we can draw all remaining render layers.
     */
    private static void drawTranslucents(WorldRenderContext context) {
    	Profiler profiler = Profilers.get();

    	profiler.push("skyblockerTranslucentDraw");
    	VertexConsumerProvider.Immediate immediate = (VertexConsumerProvider.Immediate) context.consumers();

    	//Work around RenderLayer impl problems (DepthTest#ALWAYS not actually turning off depth testing)
    	RenderSystem.disableDepthTest();
    	immediate.draw(SkyblockerRenderLayers.FILLED_THROUGH_WALLS);

    	//Draw all render layers that haven't been drawn yet - drawing a specific layer does nothing and idk why (IF bug maybe?)
    	immediate.draw();
        profiler.pop();
    }

    public static void runOnRenderThread(RenderCall renderCall) {
        if (RenderSystem.isOnRenderThread()) {
        	renderCall.execute();
        } else {
            RenderSystem.recordRenderCall(renderCall);
        }
    }

    /**
     * Retrieves the bounding box of a block in the world.
     *
     * @param world The client world.
     * @param pos   The position of the block.
     * @return The bounding box of the block.
     */
    public static Box getBlockBoundingBox(ClientWorld world, BlockPos pos) {
        return getBlockBoundingBox(world, world.getBlockState(pos), pos);
    }

    public static Box getBlockBoundingBox(ClientWorld world, BlockState state, BlockPos pos) {
        return state.getOutlineShape(world, pos).asCuboid().getBoundingBox().offset(pos);
    }

    /**
     * Adds the title to {@link TitleContainer} and {@link #playNotificationSound() plays the notification sound} if the title is not in the {@link TitleContainer} already.
     * No checking needs to be done on whether the title is in the {@link TitleContainer} already by the caller.
     *
     * @param title the title
     */
    public static void displayInTitleContainerAndPlaySound(Title title) {
        if (TitleContainer.addTitle(title)) {
            playNotificationSound();
        }
    }

    /**
     * Adds the title to {@link TitleContainer} for a set number of ticks and {@link #playNotificationSound() plays the notification sound} if the title is not in the {@link TitleContainer} already.
     * No checking needs to be done on whether the title is in the {@link TitleContainer} already by the caller.
     *
     * @param title the title
     * @param ticks the number of ticks the title will remain
     */
    public static void displayInTitleContainerAndPlaySound(Title title, int ticks) {
        if (TitleContainer.addTitle(title, ticks)) {
            playNotificationSound();
        }
    }

    private static void playNotificationSound() {
        if (CLIENT.player != null) {
            CLIENT.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
        }
    }

    public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, int argb) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x, y, width, height, argb);
    }

    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, Color color) {
        renderNineSliceColored(context, texture, x, y, width, height, ColorHelper.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
    }
}
