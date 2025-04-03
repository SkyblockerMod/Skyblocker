package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2dc;

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
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss() && dungeonMap.enableMap) {
			render(context, dungeonMap.mapX, dungeonMap.mapY, dungeonMap.mapScaling, dungeonMap.fancyMap);
		}
	}

    public static void render(DrawContext context, int x, int y, float scale, boolean fancy) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        MapIdComponent mapId = getMapIdComponent(client.player.getInventory().main.get(8));
        MapState state = FilledMapItem.getMapState(mapId, client.world);
        if (state == null) return;

        VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
        MapRenderer mapRenderer = client.getMapRenderer();

		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 0f);
        mapRenderer.update(mapId, state, MAP_RENDER_STATE);
        mapRenderer.draw(MAP_RENDER_STATE, context.getMatrices(), vertices, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        vertices.draw();

		if (fancy) renderPlayerHeads(client, context);
		context.getMatrices().pop();
    }

	private static void renderPlayerHeads(MinecraftClient client, DrawContext context) {
		if (!DungeonManager.isClearingDungeon()) return;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof PlayerEntity player)) {
				continue;
			}
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(player);
			if (dungeonClass == DungeonClass.UNKNOWN) continue;

			Vector2dc mapPos = DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), entity.getPos());
			context.getMatrices().push();
			context.getMatrices().translate(mapPos.x(), mapPos.y(), 0);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getYaw() + 180));
			RealmsUtil.drawPlayerHead(context, -4, -4, 8, entity.getUuid());
			context.drawBorder(-5, -5, 10, 10, ColorHelper.fullAlpha(dungeonClass.color()));
			context.getMatrices().pop();
		}
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
