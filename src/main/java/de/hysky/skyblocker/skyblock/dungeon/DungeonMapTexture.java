package de.hysky.skyblocker.skyblock.dungeon;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.MapRendererInvoker;
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

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
			dungeonMapTexture = new DynamicTexture(() -> "Skyblocker Dungeon Map", MAP_TEXTURE_SIZE, MAP_TEXTURE_SIZE, true);
			minecraft.getTextureManager().register(ID, dungeonMapTexture);
		});
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> clearMapImage());
		WorldRenderEvents.START_MAIN.register(DungeonMapTexture::uploadMapTexture);
	}

	public static void onMapItemDataUpdate(MapId mapId) {
		if (DungeonMap.shouldProcess() && mapId.equals(DungeonMap.getMapIdComponent(null))) {
			updateMapImage(mapId);
			updateMapDecorations(mapId);
		}
	}

	/**
	 * Copies the vanilla map image.
	 */
	private static void updateMapImage(MapId mapId) {
		if (dungeonMapTexture == null) return;
		MapItemSavedData state = MapItem.getSavedData(mapId, Minecraft.getInstance().level);
		NativeImage pixels = dungeonMapTexture.getPixels();

		if (pixels != null) {
			for (int y = 0; y < MAP_TEXTURE_SIZE; y++) {
				for (int x = 0; x < MAP_TEXTURE_SIZE; x++) {
					int i = x + y * MAP_TEXTURE_SIZE;

					pixels.setPixel(x, y, MapColor.getColorFromPackedId(state.colors[i]));
				}
			}
		}

		requiresUpload = true;
	}

	private static void updateMapDecorations(MapId mapId) {
		Minecraft minecraft = Minecraft.getInstance();
		MapRenderer mapRenderer = minecraft.getMapRenderer();
		MapItemSavedData mapData = MapItem.getSavedData(mapId, minecraft.level);

		// In the future with the thread split the map render state probably shouldn't be reused
		MAP_RENDER_STATE.decorations.clear();

		for (MapDecoration decoration : mapData.getDecorations()) {
			MAP_RENDER_STATE.decorations.add(((MapRendererInvoker) mapRenderer).invokeExtractDecorationRenderState(decoration));
		}
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
