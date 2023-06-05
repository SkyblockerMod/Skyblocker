package me.xmrvizzy.skyblocker.skyblock.dungeon;

import org.apache.commons.lang3.StringUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.Command;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DungeonMap {
	private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");

    public static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && client.world == null) return;
        ItemStack item = client.player.getInventory().main.get(8);
        NbtCompound tag = item.getNbt();

        if (tag != null && tag.contains("map")) {
            String tag2 = tag.asString();
            tag2 = StringUtils.substringBetween(tag2, "map:", "}");
            int mapid = Integer.parseInt(tag2);
            VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
            MapRenderer map = client.gameRenderer.getMapRenderer();
            MapState state = FilledMapItem.getMapState(mapid, client.world);
            float scaling = SkyblockerConfig.get().locations.dungeons.mapScaling;
        	int x = SkyblockerConfig.get().locations.dungeons.mapX;
        	int y = SkyblockerConfig.get().locations.dungeons.mapY;

            if (state == null) return;
            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(scaling, scaling, 0f);
            map.draw( matrices, vertices, mapid, state, false, 15728880);
            vertices.draw();
            matrices.pop();
        }
    }
    
    public static void renderHUDMap(MatrixStack matrices, int x, int y) {
    	RenderSystem.setShaderTexture(0, MAP_BACKGROUND);
    	DrawableHelper.drawTexture(matrices, x, y, 0, 0, 64, 64, 64, 64);
    }
    
    public static void init() {
    	ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
    		dispatcher.register(ClientCommandManager.literal("skyblocker")
    				.then(ClientCommandManager.literal("hud")
    						.then(ClientCommandManager.literal("dungeonmap")
    								.executes(context -> {
    									MinecraftClient client = context.getSource().getClient();
    									client.send(() -> client.setScreen(new DungeonMapConfigScreen(Text.literal("Dungeon Map Config"))));
    									
    									return Command.SINGLE_SUCCESS;
    								}))));
    	});
    }
}