package me.xmrvizzy.skyblocker.utils;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class RenderUtils {

    public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
        drawFilledBox(new Box(blockPos), r, g, b, a);
    }

    public static void drawFilledBox(Box box, float r, float g, float b, float a) {
        gl11Setup();

        // Fill
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(5, VertexFormats.POSITION_COLOR);
        WorldRenderer.drawBox(buffer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ, r, g, b, a / 2f);
        tessellator.draw();

        // Outline
        buffer.begin(3, VertexFormats.POSITION_COLOR);
        buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).next();
        buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).next();
        buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).next();
        buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
        tessellator.draw();

        gl11Cleanup();
    }

    public static void drawOutlineBox(BlockPos blockPos, float r, float g, float b, float a) {
        drawOutlineBox(new Box(blockPos), r, g, b, a);
    }

    public static void fillGradient(MatrixStack matrix, int x1, int y1, int x2, int y2, int color1, int color2) {
        float float_1 = (color1 >> 24 & 255) / 255.0F;
        float float_2 = (color1 >> 16 & 255) / 255.0F;
        float float_3 = (color1 >> 8 & 255) / 255.0F;
        float float_4 = (color1 & 255) / 255.0F;
        float float_5 = (color2 >> 24 & 255) / 255.0F;
        float float_6 = (color2 >> 16 & 255) / 255.0F;
        float float_7 = (color2 >> 8 & 255) / 255.0F;
        float float_8 = (color2 & 255) / 255.0F;
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBuffer();
        bufferBuilder_1.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder_1.vertex(x1, y1, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x1, y2, 0).color(float_2, float_3, float_4, float_1).next();
        bufferBuilder_1.vertex(x2, y2, 0).color(float_6, float_7, float_8, float_5).next();
        bufferBuilder_1.vertex(x2, y1, 0).color(float_6, float_7, float_8, float_5).next();
        tessellator_1.draw();
    }


    public static void drawOutlineBox(Box box, float r, float g, float b, float a) {
        gl11Setup();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Outline
        buffer.begin(3, VertexFormats.POSITION_COLOR);
        buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
        buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).next();
        buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).next();
        buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
        buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).next();
        buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
        tessellator.draw();

        gl11Cleanup();
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float t) {
        gl11Setup();
        GL11.glLineWidth(t);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, z1).color(r, g, b, 0.0F).next();
        buffer.vertex(x1, y1, z1).color(r, g, b, 1.0F).next();
        buffer.vertex(x2, y2, z2).color(r, g, b, 1.0F).next();
        tessellator.draw();

        gl11Cleanup();

    }

    public static void drawRect(float x, float y, float w, float h, int color, float alpha) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(x, h, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex(w, h, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex(w, y, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex(x, y, 0.0D).color(red, green, blue, alpha).next();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void offsetRender() {
        Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
        Vec3d camPos = camera.getPos();
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1, 0, 0);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0), 0, 1, 0);
        GL11.glTranslated(-camPos.x, -camPos.y, -camPos.z);
    }

    public static void gl11Setup() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.5F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        offsetRender();
    }

    public static void gl11Cleanup() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }


    public static void DrawPolygon(double x, double y, int radius, int sides, int color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION);

        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        bufferbuilder.vertex(x, y, 0).next();
        final double TWICE_PI = Math.PI * 2;

        for (int i = 0; i <= sides; i++) {
            double angle = (TWICE_PI * i / sides) + Math.toRadians(180);
            bufferbuilder.vertex(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).next();
        }
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}