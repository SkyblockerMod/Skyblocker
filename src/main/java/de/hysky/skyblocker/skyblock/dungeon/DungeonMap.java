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
import org.jetbrains.annotations.Nullable;
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
		render(context, x, y, scale, fancy, Integer.MIN_VALUE, Integer.MIN_VALUE, null);
	}

	@Nullable
	public static UUID render(DrawContext context, int x, int y, float scale, boolean fancy, int mouseX, int mouseY, @Nullable UUID enlarge) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return null;

		MapIdComponent mapId = getMapIdComponent(client.player.getInventory().main.get(8));
		MapState state = FilledMapItem.getMapState(mapId, client.world);
		if (state == null) return null;

		VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
		MapRenderer mapRenderer = client.getMapRenderer();

		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 0f);
		mapRenderer.update(mapId, state, MAP_RENDER_STATE);
		mapRenderer.draw(MAP_RENDER_STATE, context.getMatrices(), vertices, false, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		vertices.draw();

		UUID hoveredHead = null;
		if (fancy) hoveredHead = renderPlayerHeads(context, state, mouseX, mouseY, enlarge);
		context.getMatrices().pop();
		return hoveredHead;
	}

	public static MapIdComponent getMapIdComponent(ItemStack stack) {
		if (stack.isOf(Items.FILLED_MAP) && stack.contains(DataComponentTypes.MAP_ID)) {
			MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
			cachedMapIdComponent = mapIdComponent;
			return mapIdComponent;
		} else return cachedMapIdComponent != null ? cachedMapIdComponent : DEFAULT_MAP_ID_COMPONENT;
	}

	private static UUID renderPlayerHeads(DrawContext context, MapState state, int mouseX, int mouseY, @Nullable UUID enlarge) {
		if (!DungeonManager.isClearingDungeon()) return null;

		UUID hovered = null;
		for (Map.Entry<String, MapDecoration> mapDecoration : ((MapStateAccessor) state).getDecorations().entrySet()) {
			PlayerRenderState player = PlayerRenderState.of(mapDecoration);
			if (player == null) continue;
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(player.name());

			context.getMatrices().push();
			context.getMatrices().translate(player.mapPos().x(), player.mapPos().y(), 0);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(player.deg() + 180));

			if (player.uuid().equals(enlarge)) {
				// Enlarge the player head when the corresponding button is hovered
				context.getMatrices().scale(2, 2, 1);
			} else if (hovered == null && player.mapPos().distanceSquared(mouseX, mouseY) < 16) {
				// Enlarge the player head when hovered
				context.getMatrices().scale(2, 2, 1);
				hovered = player.uuid();
			}
			RealmsUtil.drawPlayerHead(context, -4, -4, 8, player.uuid());
			context.drawBorder(-5, -5, 10, 10, ColorHelper.fullAlpha(dungeonClass.color()));
			context.fill(-1, -7, 1, -5, ColorHelper.fullAlpha(dungeonClass.color()));
			context.getMatrices().pop();
		}
		return hovered;
	}

	private static void reset() {
		cachedMapIdComponent = null;
		mapPlayers.clear();
	}

	public record PlayerRenderState(UUID uuid, String name, Vector2dc mapPos, float deg) {
		public static PlayerRenderState of(Map.Entry<String, MapDecoration> mapDecoration) {
			if (!mapPlayers.containsKey(mapDecoration.getKey())) return null;
			// Get the player uuid and name pair with the highest count (therefore most likely to be the correct player)
			Pair<UUID, String> mapPlayer = mapPlayers.get(mapDecoration.getKey()).entrySet().stream()
					.max(Map.Entry.comparingByValue()).orElseThrow().getKey();
			// Use the player entity if it exists, since it gives the most accurate position and rotation
			PlayerEntity player = MinecraftClient.getInstance().world.getPlayerByUuid(mapPlayer.left());
			Vector2dc mapPos = player != null ? DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), player.getPos()) : new Vector2d(mapDecoration.getValue().x() / 2d + 64, mapDecoration.getValue().z() / 2d + 64);
			float deg = player != null ? player.getYaw() : mapDecoration.getValue().rotation() * 360 / 16.0F;

			return new PlayerRenderState(mapPlayer.left(), mapPlayer.right(), mapPos, deg);
		}
	}
}
