package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.utils.color.QuadColor;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

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
        QuadColor outlineColorRed = QuadColor.single( 0.0F, 1.0F, 0.0F, 1f);
        QuadColor outlineColorGreen = QuadColor.single(1.0F, 0.0F, 0.0F, 1f);
        try {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        DebugRenderer wc = MinecraftClient.getInstance().debugRenderer;
            
            if(highestBlaze != null){  
                /* Outline */
                Box blaze = highestBlaze.getBoundingBox().expand(1);
                RenderUtils.drawBoxOutline(blaze,outlineColorRed,2.5f);
           }
            if(lowestBlaze != null){

                /* Outline */
                Box blaze = lowestBlaze.getBoundingBox().expand(1);
                RenderUtils.drawBoxOutline(blaze,outlineColorGreen,2.5f);
            }
        }catch(Exception e) {
            //System.out.println("BlazeRenderer: " + e.getStackTrace());
            System.out.println("BlazeRenderer: " + e);
        }
        
    }




}