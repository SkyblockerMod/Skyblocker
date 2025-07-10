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
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.*;

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

		VertexConsumerProvider.Immediate vertices = client.getBufferBuilders().getEffectVertexConsumers();
		MapRenderer mapRenderer = client.getMapRenderer();

		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 0f);
		mapRenderer.update(mapId, state, MAP_RENDER_STATE);
		mapRenderer.draw(MAP_RENDER_STATE, context.getMatrices(), vertices, fancy, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		vertices.draw();

		UUID hoveredHead = null;
		if (fancy) hoveredHead = renderPlayerHeads(context, client.world, state, mouseX / scale, mouseY / scale, client.player.getUuid(), enlarge);
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

	private static UUID renderPlayerHeads(DrawContext context, World world, MapState state, double mouseX, double mouseY, UUID selfUuid, @Nullable UUID enlarge) {
		if (!DungeonManager.isClearingDungeon()) return null;

		System.out.println(((MapStateAccessor) state).getDecorations()); // FIXME: debug

		int i = 0;
		UUID hovered = null;
		for (@Nullable DungeonPlayerManager.DungeonPlayer dungeonPlayer : DungeonPlayerManager.getPlayers()) {
			if (dungeonPlayer == null || !dungeonPlayer.alive()) continue; // The only reason to skip i++ is if the player is dead and therefore doesn't have a corresponding map decoration

			PlayerRenderState player = PlayerRenderState.of(world, dungeonPlayer, ((MapStateAccessor) state).getDecorations().get("frame-" + i)); // todo: find pattern for id
			i++;

			if (player.uuid().equals(selfUuid) && !SkyblockerConfigManager.get().dungeons.dungeonMap.showSelfHead) continue;

			context.getMatrices().push();
			context.getMatrices().translate(player.mapPos().x(), player.mapPos().y(), 0);
			context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(player.deg() + 180));

			if (player.uuid().equals(enlarge)) {
				// Enlarge the player head when the corresponding button is hovered
				context.getMatrices().scale(2, 2, 1);
			} else if (hovered == null && isPlayerHovered(player, mouseX, mouseY)) {
				// Enlarge the player head when hovered
				context.getMatrices().scale(2, 2, 1);
				hovered = player.uuid();
			}
			HudHelper.drawPlayerHead(context, -4, -4, 8, player.uuid());
			context.drawBorder(-5, -5, 10, 10, dungeonPlayer.dungeonClass().color());
			context.fill(-1, -7, 1, -5, dungeonPlayer.dungeonClass().color());
			context.getMatrices().pop();
		}
		return hovered;
	}

	public static boolean isPlayerHovered(PlayerRenderState player, double mouseX, double mouseY) {
		return player.mapPos().distanceSquared(mouseX, mouseY) < 16;
	}

	private static void reset() {
		cachedMapIdComponent = null;
	}

	public record PlayerRenderState(UUID uuid, String name, Vector2dc mapPos, float deg) {
		public static PlayerRenderState of(World world, DungeonPlayerManager.DungeonPlayer dungeonPlayer, MapDecoration mapDecoration) {
			// Use the player entity if it exists, since it gives the most accurate position and rotation
			PlayerEntity playerEntity = world.getPlayerByUuid(dungeonPlayer.uuid());
			Vector2dc mapPos = playerEntity != null ? DungeonMapUtils.getMapPosFromPhysical(DungeonManager.getPhysicalEntrancePos(), DungeonManager.getMapEntrancePos(), DungeonManager.getMapRoomSize(), playerEntity.getPos()) : new Vector2d(mapDecoration.x() / 2d + 64, mapDecoration.z() / 2d + 64);
			float deg = playerEntity != null ? playerEntity.getYaw() : mapDecoration.rotation() * 360 / 16.0F;

			return new PlayerRenderState(dungeonPlayer.uuid(), dungeonPlayer.name(), mapPos, deg);
		}
	}
}
