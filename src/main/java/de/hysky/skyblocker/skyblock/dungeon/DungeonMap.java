package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.mixins.accessors.MapItemSavedDataAccessor;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class DungeonMap {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonMap.class);
	private static final Identifier DUNGEON_MAP = SkyblockerMod.id("dungeon_map");
	private static final MapId DEFAULT_MAP_ID_COMPONENT = new MapId(1024);
	private static @Nullable MapId cachedMapIdComponent = null;

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

	private static void render(GuiGraphics context) {
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		if (shouldProcess() && dungeonMap.enableMap) {
			render(context, dungeonMap.mapX, dungeonMap.mapY, dungeonMap.mapScaling, dungeonMap.fancyMap);
		}
	}

	public static void render(GuiGraphics context, int x, int y, float scale, boolean fancy) {
		render(context, x, y, scale, fancy, Integer.MIN_VALUE, Integer.MIN_VALUE, null);
	}

	/**
	 * @return the {@link UUID} of the hovered player head, or null if no player head is hovered.
	 */
	public static @Nullable UUID render(GuiGraphics context, int x, int y, float scale, boolean fancy, int mouseX, int mouseY, @Nullable UUID enlarge) {
		Minecraft client = Minecraft.getInstance();
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		if (client.player == null || client.level == null) return null;

		MapId mapId = getMapIdComponent(client.player.getInventory().getNonEquipmentItems().get(8));
		MapItemSavedData state = MapItem.getSavedData(mapId, client.level);
		if (state == null) return null;

		context.pose().pushMatrix();
		context.pose().translate(x, y);
		context.pose().scale(scale, scale);

		if (dungeonMap.backgroundBlur) HudHelper.submitBlurredRectangle(context, 0, 0, 128, 128, 5);
		if (dungeonMap.showOutline) HudHelper.drawBorder(context, 0, 0, 128, 128, CommonColors.LIGHT_GRAY);

		DungeonMapTexture.blitMap(context);
		DungeonMapLabels.renderRoomNames(context);

		UUID hoveredHead = null;
		if (fancy) hoveredHead = renderPlayerHeads(context, client.level, state, mouseX / scale, mouseY / scale, enlarge);
		context.pose().popMatrix();
		return hoveredHead;
	}

	public static MapId getMapIdComponent(@Nullable ItemStack stack) {
		if (stack != null && stack.is(Items.FILLED_MAP) && stack.has(DataComponents.MAP_ID)) {
			MapId mapIdComponent = stack.get(DataComponents.MAP_ID);
			cachedMapIdComponent = mapIdComponent;
			return mapIdComponent;
		} else return cachedMapIdComponent != null ? cachedMapIdComponent : DEFAULT_MAP_ID_COMPONENT;
	}

	private static @Nullable UUID renderPlayerHeads(GuiGraphics context, Level world, MapItemSavedData state, double mouseX, double mouseY, @Nullable UUID enlarge) {
		if (!DungeonManager.isClearingDungeon()) return null;

		// Used to index through the player list to find which dungeon player corresponds to which map decoration.
		// Start at 1 because the first entry in the player list is the self player.
		int i = 1;
		UUID hovered = null;
		for (Map.Entry<String, MapDecoration> mapDecoration : ((MapItemSavedDataAccessor) state).getDecorations().entrySet()) {
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
				dungeonPlayerError(mapDecoration.getKey(), "not found", i - 1, DungeonPlayerManager.getPlayers(), ((MapItemSavedDataAccessor) state).getDecorations());
				continue;
			} else if (!dungeonPlayer.alive()) {
				dungeonPlayerError(mapDecoration.getKey(), "not alive", i - 1, DungeonPlayerManager.getPlayers(), ((MapItemSavedDataAccessor) state).getDecorations());
				continue;
			} else if (dungeonPlayer.uuid() == null) {
				dungeonPlayerError(mapDecoration.getKey(), "has null uuid", i - 1, DungeonPlayerManager.getPlayers(), ((MapItemSavedDataAccessor) state).getDecorations());
				continue;
			}
			PlayerRenderState player = PlayerRenderState.of(world, dungeonPlayer, mapDecoration.getValue());

			// Actually render the player head
			context.pose().pushMatrix();
			context.pose().translate((float) player.mapPos().x(), (float) player.mapPos().y());
			context.pose().rotate((float) Math.toRadians(player.deg() + 180f));

			if (player.uuid().equals(enlarge)) {
				// Enlarge the player head when the corresponding button is hovered
				context.pose().scale(2, 2);
			} else if (hovered == null && isPlayerHovered(player, mouseX, mouseY)) {
				// Enlarge the player head when hovered
				context.pose().scale(2, 2);
				hovered = player.uuid();
			}
			HudHelper.drawPlayerHead(context, -4, -4, 8, player.uuid());
			HudHelper.drawBorder(context, -5, -5, 10, 10, dungeonPlayer.dungeonClass().color());
			context.fill(-1, -7, 1, -5, dungeonPlayer.dungeonClass().color());
			context.pose().popMatrix();
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
		public static PlayerRenderState of(Level world, DungeonPlayerManager.DungeonPlayer dungeonPlayer, MapDecoration mapDecoration) {
			// Use the player entity if it exists, since it gives the most accurate position and rotation
			Player playerEntity = world.getPlayerByUUID(dungeonPlayer.uuid());
			Vector2dc mapPos = playerEntity != null ? DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), playerEntity.position()) : new Vector2d(mapDecoration.x() / 2d + 64, mapDecoration.y() / 2d + 64);
			float deg = playerEntity != null ? playerEntity.getYRot() : mapDecoration.rot() * 360 / 16.0F;

			return new PlayerRenderState(dungeonPlayer.uuid(), dungeonPlayer.name(), mapPos, deg);
		}
	}
}
