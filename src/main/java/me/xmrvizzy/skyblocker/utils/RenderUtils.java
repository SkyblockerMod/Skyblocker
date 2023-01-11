package me.xmrvizzy.skyblocker.utils;

import com.mojang.blaze3d.systems.RenderSystem;

import me.xmrvizzy.skyblocker.utils.color.QuadColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
//import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.RotationAxis;

public class RenderUtils {

    // -------------------- Outline Boxes --------------------

    public static void drawBoxOutline(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
        drawBoxOutline(new Box(blockPos), color, lineWidth, excludeDirs);
    }

    public static void drawBoxOutline(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
        if (!FrustumUtils.isBoxVisible(box)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Outline
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
        tessellator.draw();

        RenderSystem.enableCull();

        cleanup();
    }

    // -------------------- Utils --------------------

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        // matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        // matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static Vec3d getInterpolationOffset(Entity e) {
        if (MinecraftClient.getInstance().isPaused()) {
            return Vec3d.ZERO;
        }

        double tickDelta = (double) MinecraftClient.getInstance().getTickDelta();
        return new Vec3d(
                e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
                e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
                e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
    }

    public static Boolean pointExistsInArea(int x, int y, int x1, int y1, int x2, int y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public static void setup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
    }

    public static void cleanup() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
}