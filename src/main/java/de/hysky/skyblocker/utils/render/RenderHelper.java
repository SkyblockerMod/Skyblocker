package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.mixin.accessor.BeaconBlockEntityRendererInvoker;
import me.x150.renderer.render.Renderer3d;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderHelper {
    private static final Vec3d ONE = new Vec3d(1, 1, 1);
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void renderFilledThroughWallsWithBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha) {
        renderFilledThroughWalls(context, pos, colorComponents, alpha);
        renderBeaconBeam(context, pos, colorComponents);
    }

    public static void renderFilledThroughWalls(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha) {
        if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
            Renderer3d.renderThroughWalls();
            renderFilled(context, pos, colorComponents, alpha);
            Renderer3d.stopRenderThroughWalls();
        }
    }

    public static void renderFilledIfVisible(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha) {
        if (OcclusionCulling.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
            renderFilled(context, pos, colorComponents, alpha);
        }
    }

    private static void renderFilled(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha) {
        Renderer3d.renderFilled(context.matrixStack(), new Color(colorComponents[0], colorComponents[1], colorComponents[2], alpha), Vec3d.of(pos), ONE);
    }

    private static void renderBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents) {
        if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());

            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            BufferBuilder buffer = tessellator.getBuffer();
            VertexConsumerProvider.Immediate consumer = VertexConsumerProvider.immediate(buffer);

            BeaconBlockEntityRendererInvoker.renderBeam(matrices, consumer, context.tickDelta(), context.world().getTime(), 0, MAX_OVERWORLD_BUILD_HEIGHT, colorComponents);

            consumer.draw();
            matrices.pop();
        }
    }

    /**
     * Renders the outline of a box with the specified color components and line width.
     * This does not use renderer since renderer draws outline using debug lines with a fixed width.
     */
    public static void renderOutline(WorldRenderContext context, Box box, float[] colorComponents, float lineWidth, boolean throughWalls) {
        if (FrustumUtils.isVisible(box)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();
            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            BufferBuilder buffer = tessellator.getBuffer();

            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.lineWidth(lineWidth);
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

            matrices.push();
            matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

            buffer.begin(DrawMode.LINES, VertexFormats.LINES);
            WorldRenderer.drawBox(matrices, buffer, box, colorComponents[0], colorComponents[1], colorComponents[2], 1f);
            tessellator.draw();

            matrices.pop();
            RenderSystem.lineWidth(1f);
            RenderSystem.enableCull();
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
     */
    public static void renderLinesFromPoints(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, float lineWidth) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        buffer.begin(DrawMode.LINE_STRIP, VertexFormats.LINES);

        for (int i = 0; i < points.length; i++) {
            Vec3d normalVec = points[(i + 1) % points.length].subtract(points[i]).normalize();
            buffer
                    .vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
                    .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                    .normal(normalMatrix, (float) normalVec.x, (float) normalVec.y, (float) normalVec.z)
                    .next();
        }

        tessellator.draw();

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    public static void renderQuad(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, boolean throughWalls) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha).next();
        }
        tessellator.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        matrices.pop();
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
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();
        TextRenderer textRenderer = client.textRenderer;

        scale *= 0.025f;

        matrices.push();
        matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());
        matrices.peek().getPositionMatrix().mul(RenderSystem.getModelViewMatrix());
        matrices.multiply(context.camera().getRotation());
        matrices.scale(-scale, -scale, scale);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float xOffset = -textRenderer.getWidth(text) / 2f;

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();
        VertexConsumerProvider.Immediate consumers = VertexConsumerProvider.immediate(buffer);

        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        textRenderer.draw(text, xOffset, yOffset, 0xFFFFFFFF, false, positionMatrix, consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        consumers.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        matrices.pop();
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
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
        }
    }

    public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
}
