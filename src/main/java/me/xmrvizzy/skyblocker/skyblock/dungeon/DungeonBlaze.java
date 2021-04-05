package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.util.List;

import com.mojang.blaze3d.systems.RenderCall;

import org.lwjgl.opengl.GL11;

import me.xmrvizzy.skyblocker.utils.RenderUtils;
import me.xmrvizzy.skyblocker.utils.RenderUtilsLiving;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterEntities;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BeforeEntities;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.DebugRender;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Start;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.DebugRenderer.Renderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class DungeonBlaze {
    static Entity highestBlaze = null;
    static Entity lowestBlaze = null;
    static boolean renderHooked = false;
    private static long lastCalculationTime = 0;
	private static boolean lastCalculationExists = false;
	private static int lastCalculationMinX = 0;
	private static int lastCalculationMinY = 0;
	private static int lastCalculationWidth = 0;
	private static int lastCalculationHeight = 0;
    
    public static void DungeonBlaze() {
        MinecraftClient client = MinecraftClient.getInstance();
        if(!renderHooked){
            
            WorldRenderEvents.END.register(DungeonBlaze::blazeRenderer);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("--- BlazeSolver ---"), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Blaze Low: ").append(new LiteralText("Red").formatted(Formatting.RED)), false);
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Blaze High: ").append(new LiteralText("Green").formatted(Formatting.GREEN)), false);
            renderHooked = true;
        }
        Iterable<Entity> entities = client.world.getEntities();
        int highestHealth = 0;
        int lowestHealth = 99999999;

        for (Entity entity : entities) {
            //System.out.println(entity.getName().getString());
            if (entity.getName().getString().contains("Blaze") && entity.getName().getString().contains("/")) {
        
                String blazeName = entity.getName().getString();
                try {
                    
                    int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                  
                    if (health > highestHealth) {
                        highestHealth = health;
                        
                        highestBlaze = entity;
                        
                    }
                    if (health < lowestHealth) {
                        lowestHealth = health;
                        lowestBlaze = entity;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public static void blazeRenderer(WorldRenderContext wrc) {
        try {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        DebugRenderer wc = MinecraftClient.getInstance().debugRenderer;
            
            if(highestBlaze != null){  
                                /* Outline */
                Box blaze = highestBlaze.getBoundingBox().expand(1);
                RenderUtils.drawOutlineBox(blaze, 0.0F, 1.0F, 0.0F, 1f);
           }
            if(lowestBlaze != null){

                    /* Outline */
                Box blaze = lowestBlaze.getBoundingBox().expand(1);
                RenderUtils.drawOutlineBox(blaze, 1.0F, 0.0F, 0.0F, 1f);
            }
        }catch(Exception e) {
            System.out.println("BlazeRenderer: " + e.getStackTrace());
        }
        
    }




}