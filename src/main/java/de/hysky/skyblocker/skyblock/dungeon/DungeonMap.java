package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;

public class DungeonMap {
	private static final Identifier DUNGEON_MAP = Identifier.of(SkyblockerMod.NAMESPACE, "dungeon_map");
    private static final MapIdComponent DEFAULT_MAP_ID_COMPONENT = new MapIdComponent(1024);
    private static final MapRenderState MAP_RENDER_STATE = new MapRenderState();
    private static MapIdComponent cachedMapIdComponent = null;

    @Init
    public static void init() {
		HudLayerRegistrationCallback.EVENT.register(d -> d.attachLayerAfter(IdentifiedLayer.STATUS_EFFECTS, DUNGEON_MAP, (context, tickCounter) -> render(context)));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dungeon")
                                .executes(Scheduler.queueOpenScreenCommand(DungeonMapConfigScreen::new))
                        )
                )
        ));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

	private static void render(DrawContext context) {
		if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap) {
			render(context.getMatrices(), SkyblockerConfigManager.get().dungeons.dungeonMap.mapX, SkyblockerConfigManager.get().dungeons.dungeonMap.mapY, SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling);
		}
	}

    public static void render(MatrixStack matrices, int x, int y, float scale) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        MapIdComponent mapId = getMapIdComponent(client.player.getInventory().main.get(8));
        MapState state = FilledMapItem.getMapState(mapId, client.world);
        if (state == null) return;

        VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
        MapRenderer mapRenderer = client.getMapRenderer();

        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(scale, scale, 0f);
        mapRenderer.update(mapId, state, MAP_RENDER_STATE);
        mapRenderer.draw(MAP_RENDER_STATE, matrices, vertices, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
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

    private static void reset() {
        cachedMapIdComponent = null;
    }
}
