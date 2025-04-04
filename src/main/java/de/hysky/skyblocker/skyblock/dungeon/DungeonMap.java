package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.mixins.accessors.MapStateAccessor;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
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
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonMap {
	private static final Identifier DUNGEON_MAP = Identifier.of(SkyblockerMod.NAMESPACE, "dungeon_map");
	private static final MapIdComponent DEFAULT_MAP_ID_COMPONENT = new MapIdComponent(1024);
	private static final MapRenderState MAP_RENDER_STATE = new MapRenderState();
	private static MapIdComponent cachedMapIdComponent = null;
	/**
	 * Data structure that stores how likely a map decoration (identified by the decoration's string key) is a player (identified by the uuid and name).
	 * This does so by counting how many times a map decoration and a player was closest to each other.
	 * The higher the number, the more likely that the map decoration is the player.
	 */
	private static final Map<String, Map<Pair<UUID, String>, MutableInt>> mapPlayers = HashMap.newHashMap(5);

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DungeonMap::update, 1);
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

	private static boolean shouldProcess() {
		return Utils.isInDungeons() && DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss();
	}

	/**
	 * Keeps track of players and map player markers to determine which marker corresponds to which player.
	 */
	private static void update() {
		if (!shouldProcess() || !DungeonManager.isClearingDungeon()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		assert client.player != null;
		MapIdComponent mapId = getMapIdComponent(client.player.getInventory().main.get(8));
		MapState state = FilledMapItem.getMapState(mapId, client.world);
		if (state == null) return;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof PlayerEntity player) || DungeonPlayerManager.getClassFromPlayer(player) == DungeonClass.UNKNOWN) continue;
			Vector2dc mapPos = DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), entity.getPos());

			((MapStateAccessor) state).getDecorations().entrySet().stream()
					.filter(e -> e.getValue().type() == MapDecorationTypes.FRAME || e.getValue().type() == MapDecorationTypes.BLUE_MARKER)
					.filter(e -> mapPos.distanceSquared(e.getValue().x() / 2d + 64, e.getValue().z() / 2d + 64) <= 8)
					.min(Comparator.comparingDouble(e -> mapPos.distanceSquared(e.getValue().x() / 2d + 64, e.getValue().z() / 2d + 64)))
					.ifPresent(e -> mapPlayers.computeIfAbsent(e.getKey(), _key -> new HashMap<>()).computeIfAbsent(Pair.of(player.getUuid(), player.getGameProfile().getName()), _key -> new MutableInt(0)).increment());
		}
	}

	private static void render(DrawContext context) {
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		if (shouldProcess() && dungeonMap.enableMap) {
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

		if (fancy) renderPlayerHeads(client, context, state);
		context.getMatrices().pop();
	}

	public static MapIdComponent getMapIdComponent(ItemStack stack) {
		if (stack.isOf(Items.FILLED_MAP) && stack.contains(DataComponentTypes.MAP_ID)) {
			MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
			cachedMapIdComponent = mapIdComponent;
			return mapIdComponent;
		} else return cachedMapIdComponent != null ? cachedMapIdComponent : DEFAULT_MAP_ID_COMPONENT;
	}

	private static void renderPlayerHeads(MinecraftClient client, DrawContext context, MapState state) {
		if (!DungeonManager.isClearingDungeon()) return;

		for (Map.Entry<String, MapDecoration> mapPlayerDecoration : ((MapStateAccessor) state).getDecorations().entrySet()) {
			if (!mapPlayers.containsKey(mapPlayerDecoration.getKey())) continue;
			// Get the player uuid and name pair with the highest count (therefore most likely to be the correct player)
			Pair<UUID, String> mapPlayer = mapPlayers.get(mapPlayerDecoration.getKey()).entrySet().stream()
					.max(Map.Entry.comparingByValue()).orElseThrow().getKey();
			// Use the player entity if it exists, since it gives the most accurate position and rotation
			PlayerEntity player = client.world.getPlayerByUuid(mapPlayer.left());
			Vector2dc mapPos = player != null ? DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), player.getPos()) : new Vector2d(mapPlayerDecoration.getValue().x() / 2d + 64, mapPlayerDecoration.getValue().z() / 2d + 64);
			float deg = player != null ? player.getYaw() : mapPlayerDecoration.getValue().rotation() * 360 / 16.0F;
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(mapPlayer.right());

			context.getMatrices().push();
			context.getMatrices().translate(mapPos.x(), mapPos.y(), 0);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(deg + 180));
			RealmsUtil.drawPlayerHead(context, -4, -4, 8, mapPlayer.left());
			context.drawBorder(-5, -5, 10, 10, ColorHelper.fullAlpha(dungeonClass.color()));
			context.fill(-1, -7, 1, -5, ColorHelper.fullAlpha(dungeonClass.color()));
			context.getMatrices().pop();
		}
	}

	private static void reset() {
		cachedMapIdComponent = null;
		mapPlayers.clear();
	}
}
