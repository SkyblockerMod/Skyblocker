package me.xmrvizzy.skyblocker.utils;

import me.x150.renderer.render.Renderer3d;
import me.xmrvizzy.skyblocker.mixin.accessor.BeaconBlockEntityRendererInvoker;
import me.xmrvizzy.skyblocker.utils.culling.OcclusionCulling;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.awt.*;

import com.mojang.blaze3d.systems.RenderSystem;

public class RenderHelper {
    private static final Vec3d ONE = new Vec3d(1, 1, 1);
    private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;

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
            matrices.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            
            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            BufferBuilder buffer = tessellator.getBuffer();
            VertexConsumerProvider.Immediate consumer = VertexConsumerProvider.immediate(buffer);
            
            BeaconBlockEntityRendererInvoker.renderBeam(matrices, consumer, context.tickDelta(), context.world().getTime(), 0, MAX_OVERWORLD_BUILD_HEIGHT, colorComponents);
            
            consumer.draw();
            matrices.pop();
        }
    }

    public static void displayTitleAndPlaySound(int stayTicks, int fadeOutTicks, String titleKey, Formatting formatting) {
        MinecraftClient.getInstance().inGameHud.setTitleTicks(0, stayTicks, fadeOutTicks);
        MinecraftClient.getInstance().inGameHud.setTitle(Text.translatable(titleKey).formatted(formatting));
        playNotificationSound();
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
            MinecraftClient.getInstance().player.playSound(SoundEvent.of(new Identifier("entity.experience_orb.pickup")), 100f, 0.1f);
        }
    }
}
