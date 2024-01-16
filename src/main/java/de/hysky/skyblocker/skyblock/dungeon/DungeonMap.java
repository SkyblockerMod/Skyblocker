package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import org.apache.commons.lang3.StringUtils;

public class DungeonMap {
    public static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        ItemStack item = client.player.getInventory().main.get(8);
        NbtCompound tag = item.getNbt();

        if (tag != null && tag.contains("map")) {
            String tag2 = tag.asString();
            tag2 = StringUtils.substringBetween(tag2, "map:", "}");
            int mapid = Integer.parseInt(tag2);
            VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
            MapRenderer map = client.gameRenderer.getMapRenderer();
            MapState state = FilledMapItem.getMapState(mapid, client.world);
            float scaling = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
            int x = SkyblockerConfigManager.get().locations.dungeons.mapX;
            int y = SkyblockerConfigManager.get().locations.dungeons.mapY;

            if (state == null) return;
            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(scaling, scaling, 0f);
            map.draw(matrices, vertices, mapid, state, false, 15728880);
            vertices.draw();
            matrices.pop();
        }
    }

	public static void init() { //Todo: consider renaming the command to a more general name since it'll also have dungeon score and maybe other stuff in the future
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeonmap")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))))));
    }
}