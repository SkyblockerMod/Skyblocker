package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

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
            map.draw(matrices, vertices, mapid, state, false, 15728880);
            vertices.draw();
            matrices.pop();
        }
    }

    public static void renderHUDMap(DrawContext context, int x, int y) {
        float scaling = SkyblockerConfig.get().locations.dungeons.mapScaling;
        int size = (int) (128 * scaling);
        context.drawTexture(MAP_BACKGROUND, x, y, 0, 0, size, size, size, size);
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeonmap")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))))));
    }
}