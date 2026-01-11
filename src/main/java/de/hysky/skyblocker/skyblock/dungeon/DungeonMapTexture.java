package de.hysky.skyblocker.skyblock.dungeon;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.mixins.accessors.MapRendererInvoker;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class DungeonMapTexture {
	private static final int MAP_TEXTURE_SIZE = 128;
	private static final Identifier ID = SkyblockerMod.id("dungeon_map_tex");
	private static @Nullable DynamicTexture dungeonMapTexture = null;
	private static boolean requiresUpload = true;
	private static final MapRenderState MAP_RENDER_STATE = Util.make(new MapRenderState(), state -> state.texture = ID);
	private static final Set<Room.Type> IGNORED_ROOMS_FOR_CHECKMARK_HIDING = Set.of(Room.Type.ENTRANCE, Room.Type.BLOOD, Room.Type.FAIRY, Room.Type.UNKNOWN);
	private static final Vector2ic[] NEIGHBOUR_OFFSETS = new Vector2ic[] {
			new Vector2i(0, 1), new Vector2i(0, -1), new Vector2i(1, 0), new Vector2i(-1, 0)
	};

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
			dungeonMapTexture = new DynamicTexture(() -> "Skyblocker Dungeon Map", MAP_TEXTURE_SIZE, MAP_TEXTURE_SIZE, true);
			minecraft.getTextureManager().register(ID, dungeonMapTexture);
		});
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> clearMapImage());
		DungeonEvents.ROOM_MATCHED.register((_room) -> onMapItemDataUpdate(DungeonMap.getMapIdComponent(null), true));
		WorldRenderEvents.START_MAIN.register(DungeonMapTexture::uploadMapTexture);
	}

	public static void onMapItemDataUpdate(MapId mapId, boolean updateMapTexture) {
		if (Utils.isInDungeons() && !DungeonManager.isInBoss() && mapId.equals(DungeonMap.getMapIdComponent(null))) {
			MapItemSavedData state = MapItem.getSavedData(mapId, Minecraft.getInstance().level);

			// Only update the map texture when it changes (Hypixel only updates the texture when absolutely needed)
			if (updateMapTexture) {
				updateMapImage(state);
				hideCheckmarks(state);
			}

			updateMapDecorations(state);
		}
	}

	/**
	 * Copies the vanilla map image.
	 */
	private static void updateMapImage(MapItemSavedData mapData) {
		if (dungeonMapTexture == null) return;
		NativeImage pixels = dungeonMapTexture.getPixels();

		if (pixels != null) {
			for (int y = 0; y < MAP_TEXTURE_SIZE; y++) {
				for (int x = 0; x < MAP_TEXTURE_SIZE; x++) {
					int i = x + y * MAP_TEXTURE_SIZE;

					pixels.setPixel(x, y, MapColor.getColorFromPackedId(mapData.colors[i]));
				}
			}
		}

		requiresUpload = true;
	}

	private static void updateMapDecorations(MapItemSavedData mapData) {
		MapRenderer mapRenderer = Minecraft.getInstance().getMapRenderer();

		// In the future with the thread split the map render state probably shouldn't be reused
		MAP_RENDER_STATE.decorations.clear();

		for (MapDecoration decoration : mapData.getDecorations()) {
			MAP_RENDER_STATE.decorations.add(((MapRendererInvoker) mapRenderer).invokeExtractDecorationRenderState(decoration));
		}
	}

	/**
	 * Removes checkmark icons from known rooms
	 */
	private static void hideCheckmarks(MapItemSavedData mapData) {
		DungeonsConfig.DungeonMap mapConfig = SkyblockerConfigManager.get().dungeons.dungeonMap;

		// Dependent on room labels since removing checkmarks and having no indicator as to a room's completeness is very wrong
		if (!mapConfig.showRoomLabels || !mapConfig.hideCheckmarks) return;

		// Only hide checkmarks from known rooms since we have coloured labels for them
		DungeonManager.getRoomsStream()
				.filter(Room::isMatched)
				.filter(room -> !IGNORED_ROOMS_FOR_CHECKMARK_HIDING.contains(room.getType()))
				.forEach(room -> {
					DungeonManager.getRoomCheckmarkColour(mapData, room, checkmarkPixel -> {
						Set<Vector2ic> checkmarkPixels = new HashSet<>();
						Set<Vector2ic> visited = new HashSet<>();
						Queue<Vector2ic> queue = new ArrayDeque<>();
						queue.add(checkmarkPixel);

						// Flood fill using Breadth-First Searching to find checkmark pixels
						while (!queue.isEmpty()) {
							Vector2ic current = queue.remove();
							checkmarkPixels.add(current);

							for (Vector2ic offset : NEIGHBOUR_OFFSETS) {
								Vector2ic neighbour = new Vector2i(current).add(offset);

								if (visited.contains(neighbour) || queue.contains(neighbour)) {
									continue;
								} else if (neighbour.x() >= 0 && neighbour.x() < MAP_TEXTURE_SIZE && neighbour.y() >= 0 && neighbour.y() < MAP_TEXTURE_SIZE && DungeonManager.matchesCheckmarkColour(DungeonMapUtils.getColor(mapData, neighbour))) {
									visited.add(neighbour);
									queue.add(neighbour);
								}
							}
						}

						setPixels(checkmarkPixels, MapColor.getColorFromPackedId(room.getType().color));
					});
				});
	}

	private static void setPixels(Set<Vector2ic> checkmarkPixels, int colour) {
		if (dungeonMapTexture == null) return;
		NativeImage pixels = dungeonMapTexture.getPixels();

		if (pixels != null) {
			for (Vector2ic pixel : checkmarkPixels) {
				pixels.setPixel(pixel.x(), pixel.y(), colour);
			}
		}

		requiresUpload = true;
	}

	/**
	 * Clears the map image by making all of its pixels transparent. Triggered when the player swaps worlds.
	 */
	private static void clearMapImage() {
		if (dungeonMapTexture == null) return;
		NativeImage pixels = dungeonMapTexture.getPixels();

		if (pixels != null) {
			for (int y = 0; y < MAP_TEXTURE_SIZE; y++) {
				for (int x = 0; x < MAP_TEXTURE_SIZE; x++) {
					// Make pixels transparent
					pixels.setPixel(x, y, ARGB.transparent(CommonColors.BLACK));
				}
			}
		}

		requiresUpload = true;
	}

	/**
	 * Upload the map texture to the GPU at the start of the game, this is to ensure this runs on the GPU
	 * for the thread split.
	 */
	private static void uploadMapTexture(WorldTerrainRenderContext context) {
		if (dungeonMapTexture != null && requiresUpload) {
			dungeonMapTexture.upload();
			requiresUpload = false;
		}
	}

	protected static void blitMap(GuiGraphics graphics) {
		graphics.submitMapRenderState(MAP_RENDER_STATE);
	}

	public static void close() {
		if (dungeonMapTexture != null) {
			dungeonMapTexture.close();
		}
	}
}
