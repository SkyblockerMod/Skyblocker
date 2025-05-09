package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.SpriteContentsAccessor;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MaterialPlateTextures {
	private static final Identifier RELOAD_LISTENER_ID = Identifier.of(SkyblockerMod.NAMESPACE, "material_plates");
	private static final ResourceFinder RESOURCE_FINDER = new ResourceFinder("textures", ".png");

	private static final Identifier MATERIAL_PLATE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "armor_customization_screen/material_plate");
	private static final Identifier BASE_PALETTE_TEXTURE = Identifier.ofVanilla("trims/color_palettes/trim_palette");
	public static final Identifier TEXTURE_PREFIX = Identifier.of(SkyblockerMod.NAMESPACE, "generated/material_plate_");

	private static final Logger LOGGER = LogUtils.getLogger();

	private static CompletableFuture<Void> texturesFuture = null;
	private static boolean errored = false;

	private static final Map<String, NativeImageBackedTexture> SUFFIX_TO_TEXTURE = new Object2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		// Reset the generated textures when the resource pack is reloaded
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new Listener());

		SkyblockEvents.LEAVE.register(() -> MinecraftClient.getInstance().executeSync(MaterialPlateTextures::closeTextures)); // Needs to be run on render thread but LEAVE is on network thread
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> closeTextures()); // close textures on client stop in case the previous event doesn't get ran for some reason

	}

	private static int[] openPalette(ArmorTrimMaterial material, String namespace) {
		return openPalette(Identifier.of(namespace,"trims/color_palettes/" + material.assets().base().suffix()));
	}

	/**
	 * Opens a palette
	 * @param texture the identifier of the palette
	 * @return an array of ints representing each color
	 */
	private static int[] openPalette(Identifier texture) {
		Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(
				RESOURCE_FINDER.toResourcePath(texture)
		);
		if (resource.isEmpty()) throw new IllegalArgumentException("Can't find texture: " + texture);
		int[] pixels = null;
		try (InputStream stream = resource.get().getInputStream()) {
			try (NativeImage palette = NativeImage.read(stream)) {
				pixels = palette.copyPixelsArgb();
			} catch (IOException e) {
				LOGGER.error("Failed to load color palette", e);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read color palette {}", texture, e);
		}
		return pixels;
	}

	/**
	 * @return A map of all created textures with their suffix as a key
	 * @implNote Taken from {@link net.minecraft.client.texture.atlas.PalettedPermutationsAtlasSource}
	 */
	private static Map<String, NativeImageBackedTexture> createTextures() {
		Map<String, NativeImageBackedTexture> map = new HashMap<>();
		int[] basePalette = openPalette(BASE_PALETTE_TEXTURE);
		if (basePalette == null) {
			LOGGER.error("Failed to load the base palette, see error above");
			return map;
		}

		NativeImage image = ((SpriteContentsAccessor) MinecraftClient.getInstance().getGuiAtlasManager().getSprite(MATERIAL_PLATE_TEXTURE).getContents()).getImage();

		RegistryWrapper<ArmorTrimMaterial> materials = Utils.getRegistryWrapperLookup().getOrThrow(RegistryKeys.TRIM_MATERIAL);
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		materials.streamEntries().forEach(entry -> {
			ArmorTrimMaterial material = entry.value();
			String suffix = material.assets().base().suffix();
			int[] materialPalette = openPalette(material, entry.registryKey().getValue().getNamespace());
			if (materialPalette == null) return;

			// Run on main thread or else NativeImageBackedTexture is mad
			futures.add(CompletableFuture.runAsync(() -> {
				NativeImageBackedTexture texture = new NativeImageBackedTexture("SkyblockerMaterialPlate_" + suffix, image.getWidth(), image.getHeight(), false);
				if (texture.getImage() == null) throw new RuntimeException("Failed to create NativeImageBackedTexture");


				// create map to convert color to palette
				Int2IntMap palette = new Int2IntOpenHashMap(materialPalette.length);
				for (int i = 0; i < basePalette.length; i++) {
					int color = basePalette[i];
					if (ColorHelper.getAlpha(color) != 0) {
						palette.put(ColorHelper.zeroAlpha(color), materialPalette[i]);
					}
				}
				for (int i = 0; i < texture.getImage().getWidth(); i++) {
					for (int j = 0; j < texture.getImage().getHeight(); j++) {
						int baseColor = image.getColorArgb(i, j);
						int alpha = ColorHelper.getAlpha(baseColor);
						int color = ColorHelper.withAlpha(alpha, palette.getOrDefault(ColorHelper.zeroAlpha(baseColor), baseColor));
						texture.getImage().setColorArgb(
								i,
								j,
								color
						);
					}
				}
				texture.upload();
				map.put(suffix, texture);
			}, MinecraftClient.getInstance())); // Use client executor to run on main thread

		});
		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		return map;
	}

	private static void createTexturesAsync() {
		errored = false;
		texturesFuture = CompletableFuture
				.supplyAsync(MaterialPlateTextures::createTextures)
				.thenAccept(map -> {
					SUFFIX_TO_TEXTURE.putAll(map);
					TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
					for (Map.Entry<String, NativeImageBackedTexture> entry : map.entrySet()) {
						textureManager.registerTexture(TEXTURE_PREFIX.withSuffixedPath(entry.getKey()), entry.getValue());
					}
				})
				.exceptionally(throwable -> {
					errored = true;
					LOGGER.error("Failed to create textures", throwable);
					return null;
				});
	}

	private static void closeTextures() {
		if (SUFFIX_TO_TEXTURE.isEmpty()) return;
		TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		for (Map.Entry<String, NativeImageBackedTexture> entry : SUFFIX_TO_TEXTURE.entrySet()) {
			textureManager.destroyTexture(TEXTURE_PREFIX.withSuffixedPath(entry.getKey()));
		}
		SUFFIX_TO_TEXTURE.clear();
	}

	/**
	 * @return true if the textures are available, creates the textures in another thread if they aren't created
	 */
	public static boolean isAvailable() {
		if (texturesFuture == null) {
			createTexturesAsync();
			return false;
		}
		return texturesFuture.isDone() && !errored;
	}

	private static final class Listener implements IdentifiableResourceReloadListener {

		@Override
		public Identifier getFabricId() {
			return RELOAD_LISTENER_ID;
		}

		@Override
		public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
			return CompletableFuture.completedFuture(null)
					.thenCompose(synchronizer::whenPrepared) // Tell the reload listener that we finished "preparing"
					.thenAcceptAsync(o -> {
						texturesFuture = null;
						closeTextures();
			}, applyExecutor);
		}

		@Override
		public Collection<Identifier> getFabricDependencies() {
			return Collections.singletonList(ResourceReloadListenerKeys.MODELS);
		}
	}
}
