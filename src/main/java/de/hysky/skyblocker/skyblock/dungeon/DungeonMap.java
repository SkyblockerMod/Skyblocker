package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;

public class DungeonMap {
    private static final int DEFAULT_MAP_ID = 1024;
    private static Integer cachedMapId = null;

    public static void init() {
    	HudRenderEvents.AFTER_MAIN_HUD.register((context, tickDelta) -> render(context));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeon")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))
                        )
                )
        ));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

    private static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        int mapId = getMapId(client.player.getInventory().main.get(8));

        MapState state = FilledMapItem.getMapState(mapId, client.world);
        if (state == null) return;

        int x = SkyblockerConfigManager.get().locations.dungeons.mapX;
        int y = SkyblockerConfigManager.get().locations.dungeons.mapY;
        float scaling = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
        VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
        MapRenderer mapRenderer = client.gameRenderer.getMapRenderer();

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scaling, scaling, 0f);
        mapRenderer.draw(matrices, vertices, mapId, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        vertices.draw();
        matrices.pop();
    }

    public static int getMapId(ItemStack stack) {
        if (stack.isOf(Items.FILLED_MAP)) {
            @SuppressWarnings("DataFlowIssue")
            int mapId = FilledMapItem.getMapId(stack);
            cachedMapId = mapId;
            return mapId;
        } else return cachedMapId != null ? cachedMapId : DEFAULT_MAP_ID;
    }

    private static void render(DrawContext context) {
        if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().locations.dungeons.enableMap) {
            render(context.getMatrices());
        }
    }

    private static void reset() {
        cachedMapId = null;
    }
}
