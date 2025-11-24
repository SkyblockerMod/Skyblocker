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
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class DungeonMap {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonMap.class);
	private static final Identifier DUNGEON_MAP = SkyblockerMod.id("dungeon_map");
	private static final MapIdComponent DEFAULT_MAP_ID_COMPONENT = new MapIdComponent(1024);
	private static final MapRenderState MAP_RENDER_STATE = new MapRenderState();
	private static MapIdComponent cachedMapIdComponent = null;

	@Init
	public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS, DUNGEON_MAP, (context, tickCounter) -> render(context));
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

	private static void render(DrawContext context) {
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		if (shouldProcess() && dungeonMap.enableMap) {
			render(context, dungeonMap.mapX, dungeonMap.mapY, dungeonMap.mapScaling, dungeonMap.fancyMap);
		}
	}

	public static void render(DrawContext context, int x, int y, float scale, boolean fancy) {
		render(context, x, y, scale, fancy, Integer.MIN_VALUE, Integer.MIN_VALUE, null);
	}

	/**
	 * @return the {@link UUID} of the hovered player head, or null if no player head is hovered.
	 */
	@Nullable
	public static UUID render(DrawContext context, int x, int y, float scale, boolean fancy, int mouseX, int mouseY, @Nullable UUID enlarge) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null) return null;

		MapIdComponent mapId = getMapIdComponent(client.player.getInventory().getMainStacks().get(8));
		MapState state = FilledMapItem.getMapState(mapId, client.world);
		if (state == null) return null;

		MapRenderer mapRenderer = client.getMapRenderer();
		mapRenderer.update(mapId, state, MAP_RENDER_STATE);

		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale(scale, scale);
		context.drawMap(MAP_RENDER_STATE);

		DungeonMapLabels.renderRoomNames(context);

		UUID hoveredHead = null;
		if (fancy) hoveredHead = renderPlayerHeads(context, client.world, state, mouseX / scale, mouseY / scale, enlarge);
		context.getMatrices().popMatrix();
		return hoveredHead;
	}

	public static MapIdComponent getMapIdComponent(ItemStack stack) {
		if (stack.isOf(Items.FILLED_MAP) && stack.contains(DataComponentTypes.MAP_ID)) {
			MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
			cachedMapIdComponent = mapIdComponent;
			return mapIdComponent;
		} else return cachedMapIdComponent != null ? cachedMapIdComponent : DEFAULT_MAP_ID_COMPONENT;
	}

	@Nullable
	private static UUID renderPlayerHeads(DrawContext context, World world, MapState state, double mouseX, double mouseY, @Nullable UUID enlarge) {
		if (!DungeonManager.isClearingDungeon()) return null;

		// Used to index through the player list to find which dungeon player corresponds to which map decoration.
		// Start at 1 because the first entry in the player list is the self player.
		int i = 1;
		UUID hovered = null;
		for (Map.Entry<String, MapDecoration> mapDecoration : ((MapStateAccessor) state).getDecorations().entrySet()) {
			// Get the corresponding dungeon player for the map decoration.
			DungeonPlayerManager.DungeonPlayer dungeonPlayer = null;
			// If the map decoration is the self player, use the first player in this list. The self player is always the first player in the list.
			if (mapDecoration.getValue().type().value().equals(MapDecorationTypes.FRAME.value())) {
				if (!SkyblockerConfigManager.get().dungeons.dungeonMap.showSelfHead) continue;
				dungeonPlayer = DungeonPlayerManager.getPlayers()[0];
			} else while (i < DungeonPlayerManager.getPlayers().length && (dungeonPlayer == null || !dungeonPlayer.alive())) { // Find the next alive player in the player list.
				dungeonPlayer = DungeonPlayerManager.getPlayers()[i];
				i++;
			}

			// If we still didn't find a valid dungeon player after searching though the entire player list, something is wrong.
			if (dungeonPlayer == null) {
				dungeonPlayerError(mapDecoration.getKey(), "not found", i - 1, DungeonPlayerManager.getPlayers(), ((MapStateAccessor) state).getDecorations());
				continue;
			} else if (!dungeonPlayer.alive()) {
				dungeonPlayerError(mapDecoration.getKey(), "not alive", i - 1, DungeonPlayerManager.getPlayers(), ((MapStateAccessor) state).getDecorations());
				continue;
			} else if (dungeonPlayer.uuid() == null) {
				dungeonPlayerError(mapDecoration.getKey(), "has null uuid", i - 1, DungeonPlayerManager.getPlayers(), ((MapStateAccessor) state).getDecorations());
				continue;
			}
			PlayerRenderState player = PlayerRenderState.of(world, dungeonPlayer, mapDecoration.getValue());

			// Actually render the player head
			context.getMatrices().pushMatrix();
			context.getMatrices().translate((float) player.mapPos().x(), (float) player.mapPos().y());
			context.getMatrices().rotate((float) Math.toRadians(player.deg() + 180f));

			if (player.uuid().equals(enlarge)) {
				// Enlarge the player head when the corresponding button is hovered
				context.getMatrices().scale(2, 2);
			} else if (hovered == null && isPlayerHovered(player, mouseX, mouseY)) {
				// Enlarge the player head when hovered
				context.getMatrices().scale(2, 2);
				hovered = player.uuid();
			}
			HudHelper.drawPlayerHead(context, -4, -4, 8, player.uuid());
			HudHelper.drawBorder(context, -5, -5, 10, 10, dungeonPlayer.dungeonClass().color());
			context.fill(-1, -7, 1, -5, dungeonPlayer.dungeonClass().color());
			context.getMatrices().popMatrix();
		}
		return hovered;
	}

	private static void dungeonPlayerError(String decorationId, String reason, int i, DungeonPlayerManager.DungeonPlayer[] dungeonPlayers, Map<String, MapDecoration> mapDecorations) {
		LOGGER.error("[Skyblocker Dungeon Map] Dungeon player for map decoration '{}' {}. Player list index (zero-indexed): {}. Player list: {}. Map decorations: {}", decorationId, reason, i, Arrays.toString(dungeonPlayers), mapDecorations);
	}

	public static boolean isPlayerHovered(PlayerRenderState player, double mouseX, double mouseY) {
		return player.mapPos().distanceSquared(mouseX, mouseY) < 16;
	}

	private static void reset() {
		cachedMapIdComponent = null;
	}

	public record PlayerRenderState(UUID uuid, String name, Vector2dc mapPos, float deg) {
		public static PlayerRenderState of(@NotNull World world, @NotNull DungeonPlayerManager.DungeonPlayer dungeonPlayer, @NotNull MapDecoration mapDecoration) {
			// Use the player entity if it exists, since it gives the most accurate position and rotation
			PlayerEntity playerEntity = world.getPlayerByUuid(dungeonPlayer.uuid());
			Vector2dc mapPos = playerEntity != null ? DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), playerEntity.getEntityPos()) : new Vector2d(mapDecoration.x() / 2d + 64, mapDecoration.z() / 2d + 64);
			float deg = playerEntity != null ? playerEntity.getYaw() : mapDecoration.rotation() * 360 / 16.0F;

			return new PlayerRenderState(dungeonPlayer.uuid(), dungeonPlayer.name(), mapPos, deg);
		}
	}
}
