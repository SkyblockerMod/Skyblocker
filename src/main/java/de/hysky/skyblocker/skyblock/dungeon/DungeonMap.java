package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;

public class DungeonMap {
    private static final int mapId = 1024;
    private static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        MapState state = FilledMapItem.getMapState(mapId, client.world);
        if (state == null) return;

        float scaling = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
        VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();

        matrices.push();
        matrices.translate(SkyblockerConfigManager.get().locations.dungeons.mapX, SkyblockerConfigManager.get().locations.dungeons.mapY, 0);
        matrices.scale(scaling, scaling, 0f);
        client.gameRenderer.getMapRenderer().draw(matrices, vertices, mapId, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        vertices.draw();
        matrices.pop();
    }

	public static void init() { //Todo: consider renaming the command to a more general name since it'll also have dungeon score and maybe other stuff in the future
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeonmap")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))))));
    }
}
