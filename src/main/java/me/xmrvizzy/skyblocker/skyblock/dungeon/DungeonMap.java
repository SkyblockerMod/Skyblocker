package me.xmrvizzy.skyblocker.skyblock.dungeon;

import org.apache.commons.lang3.StringUtils;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;

public class DungeonMap {

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

            if (state == null) return;
            matrices.push();
            matrices.translate(2, 2, 0);
            matrices.scale(scaling, scaling, 0f);
            map.draw( matrices, vertices, mapid, state, false, 15728880);
            vertices.draw();
            matrices.pop();
        }
    }
}