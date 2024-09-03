package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;

public class DungeonMap {
    private static final MapIdComponent DEFAULT_MAP_ID_COMPONENT = new MapIdComponent(1024);
    private static MapIdComponent cachedMapIdComponent = null;

    @Init
    public static void init() {
    	HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> render(context));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeon")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))
                        )
                )
        ));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

    public static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        MapIdComponent mapId = getMapIdComponent(client.player.getInventory().main.get(8));

        MapState state = FilledMapItem.getMapState(mapId, client.world);
        if (state == null) return;

        int x = SkyblockerConfigManager.get().dungeons.dungeonMap.mapX;
        int y = SkyblockerConfigManager.get().dungeons.dungeonMap.mapY;
        float scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling;
        VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
        MapRenderer mapRenderer = client.gameRenderer.getMapRenderer();

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scaling, scaling, 0f);
        mapRenderer.draw(matrices, vertices, mapId, state, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        vertices.draw();
        matrices.pop();
    }

    public static MapIdComponent getMapIdComponent(ItemStack stack) {
        if (stack.isOf(Items.FILLED_MAP) && stack.contains(DataComponentTypes.MAP_ID)) {
            MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
            cachedMapIdComponent = mapIdComponent;
            return mapIdComponent;
        } else return cachedMapIdComponent != null ? cachedMapIdComponent : DEFAULT_MAP_ID_COMPONENT;
    }

    private static void render(DrawContext context) {
        if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap) {
            render(context.getMatrices());
        }
    }

    private static void reset() {
        cachedMapIdComponent = null;
    }
}
