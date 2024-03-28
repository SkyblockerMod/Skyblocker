package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixin.accessor.BeaconBlockEntityRendererInvoker;
import de.hysky.skyblocker.mixin.accessor.DrawContextInvoker;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.texture.Scaling;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class RenderHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TRANSLUCENT_DRAW = new Identifier(SkyblockerMod.NAMESPACE, "translucent_draw");
    private static final MethodHandle SCHEDULE_DEFERRED_RENDER_TASK = getDeferredRenderTaskHandle();
    private static final Vec3d ONE = new Vec3d(1, 1, 1);
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.addPhaseOrdering(Event.DEFAULT_PHASE, TRANSLUCENT_DRAW);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(TRANSLUCENT_DRAW, RenderHelper::drawTranslucents);
    }

    public static void renderFilledWithBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, pos, colorComponents, alpha, throughWalls);
        renderBeaconBeam(context, pos, colorComponents);
    }

    public static void renderFilled(WorldRenderContext context, BlockPos pos, float[] colorComponents, float alpha, boolean throughWalls) {
        renderFilled(context, Vec3d.of(pos), ONE, colorComponents, alpha, throughWalls);
    }

    public static void renderFilled(WorldRenderContext context, BlockPos pos, Vec3d dimensions, float[] colorComponents, float alpha, boolean throughWalls) {
        if (throughWalls) {
            if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + dimensions.x, pos.getY() + dimensions.y, pos.getZ() + dimensions.z)) {
                renderFilled(context, Vec3d.of(pos), dimensions, colorComponents, alpha, true);
            }
        } else {
            if (OcclusionCulling.getRegularCuller().isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + dimensions.x, pos.getY() + dimensions.y, pos.getZ() + dimensions.z)) {
                renderFilled(context, Vec3d.of(pos), dimensions, colorComponents, alpha, false);
            }
        }
    }

    private static void renderFilled(WorldRenderContext context, Vec3d pos, Vec3d dimensions, float[] colorComponents, float alpha, boolean throughWalls) {
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer buffer = consumers.getBuffer(throughWalls ? SkyblockerRenderLayers.FILLED_THROUGH_WALLS : SkyblockerRenderLayers.FILLED);

        WorldRenderer.renderFilledBox(matrices, buffer, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colorComponents[0], colorComponents[1], colorComponents[2], alpha);

        matrices.pop();
    }

    private static void renderBeaconBeam(WorldRenderContext context, BlockPos pos, float[] colorComponents) {
        if (FrustumUtils.isVisible(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ());

            BeaconBlockEntityRendererInvoker.renderBeam(matrices, context.consumers(), context.tickDelta(), context.world().getTime(), 0, MAX_OVERWORLD_BUILD_HEIGHT, colorComponents);

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
     * @param throughWalls    Whether to render through walls or not
     */
    public static void renderLinesFromPoints(WorldRenderContext context, Vec3d[] points, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        buffer.begin(DrawMode.LINE_STRIP, VertexFormats.LINES);

        for (int i = 0; i < points.length; i++) {
            Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
            Vector3f normalVec = new Vector3f((float) nextPoint.getX(), (float) nextPoint.getY(), (float) nextPoint.getZ()).sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).normalize().mul(normalMatrix);
            buffer
                    .vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
                    .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                    .normal(normalVec.x, normalVec.y, normalVec.z)
                    .next();
        }

        tessellator.draw();

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void renderLineFromCursor(WorldRenderContext context, Vec3d point, float[] colorComponents, float alpha, float lineWidth) {
        Vec3d camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        Vec3d offset = Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw());
        Vec3d cameraPoint = camera.add(offset);

        buffer.begin(DrawMode.LINES, VertexFormats.LINES);
        Vector3f normal = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
        buffer
                .vertex(positionMatrix, (float) cameraPoint.x , (float) cameraPoint.y, (float) cameraPoint.z)
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(normal.x, normal.y, normal.z)
                .next();

        buffer
                .vertex(positionMatrix, (float) point.getX(), (float) point.getY(), (float) point.getZ())
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(normal.x, normal.y, normal.z)
                .next();


        tessellator.draw();

        matrices.pop();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(throughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);

        buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            buffer.vertex(positionMatrix, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha).next();
        }
        tessellator.draw();

        RenderSystem.enableCull();
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
     * This is called after all {@link WorldRenderEvents#AFTER_TRANSLUCENT} listeners have been called so that we can draw all remaining render layers.
     */
    private static void drawTranslucents(WorldRenderContext context) {
        //Draw all render layers that haven't been drawn yet - drawing a specific layer does nothing and idk why
        ((VertexConsumerProvider.Immediate) context.consumers()).draw();
    }

    public static void runOnRenderThread(Runnable runnable) {
        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else if (SCHEDULE_DEFERRED_RENDER_TASK != null) { //Sodium
            try {
                SCHEDULE_DEFERRED_RENDER_TASK.invokeExact(runnable);
            } catch (Throwable t) {
                LOGGER.error("[Skyblocker] Failed to schedule a render task!", t);
            }
        } else { //Vanilla
            RenderSystem.recordRenderCall(runnable::run);
        }
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
        if (client.player != null) {
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
        }
    }

    public static boolean pointIsInArea(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    private static void drawSprite(DrawContext context, Sprite sprite, int i, int j, int k, int l, int x, int y, int z, int width, int height, float red, float green, float blue, float alpha) {
        if (width == 0 || height == 0) {
            return;
        }
        ((DrawContextInvoker) context).invokeDrawTexturedQuad(sprite.getAtlasId(), x, x + width, y, y + height, z, sprite.getFrameU((float)k / (float)i), sprite.getFrameU((float)(k + width) / (float)i), sprite.getFrameV((float)l / (float)j), sprite.getFrameV((float)(l + height) / (float)j), red, green, blue, alpha);
    }
    private static void drawSpriteTiled(DrawContext context, Sprite sprite, int x, int y, int z, int width, int height, int i, int j, int tileWidth, int tileHeight, int k, int l, float red, float green, float blue, float alpha) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + tileWidth + "x" + tileHeight);
        }
        for (int m = 0; m < width; m += tileWidth) {
            int n = Math.min(tileWidth, width - m);
            for (int o = 0; o < height; o += tileHeight) {
                int p = Math.min(tileHeight, height - o);
                drawSprite(context, sprite, k, l, i, j, x + m, y + o, z, n, p, red, green, blue, alpha);
            }
        }
    }

    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, float red, float green, float blue, float alpha) {
        Sprite sprite = MinecraftClient.getInstance().getGuiAtlasManager().getSprite(texture);
        Scaling scaling = MinecraftClient.getInstance().getGuiAtlasManager().getScaling(sprite);
        if (!(scaling instanceof Scaling.NineSlice nineSlice)) return;
        Scaling.NineSlice.Border border = nineSlice.border();
        int z = 0;

        int i = Math.min(border.left(), width / 2);
        int j = Math.min(border.right(), width / 2);
        int k = Math.min(border.top(), height / 2);
        int l = Math.min(border.bottom(), height / 2);
        if (width == nineSlice.width() && height == nineSlice.height()) {
            drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, height, red, green, blue, alpha);
            return;
        }
        if (height == nineSlice.height()) {
            drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, i, height, red, green, blue, alpha);
            drawSpriteTiled(context, sprite, x + i, y, z, width - j - i, height, i, 0, nineSlice.width() - j - i, nineSlice.height(), nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
            drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, z, j, height, red, green, blue, alpha);
            return;
        }
        if (width == nineSlice.width()) {
            drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, width, k, red, green, blue, alpha);
            drawSpriteTiled(context, sprite, x, y + k, z, width, height - l - k, 0, k, nineSlice.width(), nineSlice.height() - l - k, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
            drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, z, width, l, red, green, blue, alpha);
            return;
        }
        drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, z, i, k, red, green, blue, alpha);
        drawSpriteTiled(context, sprite, x + i, y, z, width - j - i, k, i, 0, nineSlice.width() - j - i, k, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
        drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, 0, x + width - j, y, z, j, k, red, green, blue, alpha);
        drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - l, x, y + height - l, z, i, l, red, green, blue, alpha);
        drawSpriteTiled(context, sprite, x + i, y + height - l, z, width - j - i, l, i, nineSlice.height() - l, nineSlice.width() - j - i, l, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
        drawSprite(context, sprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - j, nineSlice.height() - l, x + width - j, y + height - l, z, j, l, red, green, blue, alpha);
        drawSpriteTiled(context, sprite, x, y + k, z, i, height - l - k, 0, k, i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
        drawSpriteTiled(context, sprite, x + i, y + k, z, width - j - i, height - l - k, i, k, nineSlice.width() - j - i, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
        drawSpriteTiled(context, sprite, x + width - j, y + k, z, i, height - l - k, nineSlice.width() - j, k, j, nineSlice.height() - l - k, nineSlice.width(), nineSlice.height(), red, green, blue, alpha);
    }

    private static final float[] colorBuffer = new float[4];
    public static void renderNineSliceColored(DrawContext context, Identifier texture, int x, int y, int width, int height, Color color) {
        color.getComponents(colorBuffer);
        renderNineSliceColored(context, texture, x, y, width, height, colorBuffer[0],colorBuffer[1],colorBuffer[2],colorBuffer[3]);
    }

    // TODO Get rid of reflection once the new Sodium is released
    private static MethodHandle getDeferredRenderTaskHandle() {
        try {
            Class<?> deferredTaskClass = Class.forName("me.jellysquid.mods.sodium.client.render.util.DeferredRenderTask");

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType mt = MethodType.methodType(void.class, Runnable.class);

            return lookup.findStatic(deferredTaskClass, "schedule", mt);
        } catch (Throwable ignored) {
        }

        return null;
    }
}
