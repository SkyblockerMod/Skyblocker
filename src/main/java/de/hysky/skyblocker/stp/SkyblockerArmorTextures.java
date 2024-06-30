package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import de.hysky.skyblocker.utils.MultithreadedTaskHelper;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Custom Armor Textures!
 */
public class SkyblockerArmorTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String ARMOR_OVERRIDES_PATH = "overrides/armor";
	private static final Object2ObjectOpenHashMap<String, ArmorModelOverride> ARMOR_OVERRIDES = new Object2ObjectOpenHashMap<>();
	private static final Int2ReferenceOpenHashMap<List<ArmorMaterial.Layer>> CACHE = new Int2ReferenceOpenHashMap<>();
	public static final List<ArmorMaterial.Layer> NO_CUSTOM_TEXTURES = List.of();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(CACHE::clear, 4800);
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of(SkyblockerMod.NAMESPACE, "custom_armor_textures");
			}

			@Override
			public void reload(ResourceManager manager) {
				loadArmorPredicates(manager);
			}
		});
	}

	private static void loadArmorPredicates(ResourceManager manager) {
		try {
			ARMOR_OVERRIDES.clear();
			//Load armour textures from our namespace in the overrides folder
			Map<Identifier, Resource> overrides = manager.findResources(ARMOR_OVERRIDES_PATH, ModelHelper::isValidJsonPath);
			MultithreadedTaskHelper<Pair<List<String>, ArmorModelOverride>> taskHelper = MultithreadedTaskHelper.create();

			for (Map.Entry<Identifier, Resource> entry : overrides.entrySet()) {
				taskHelper.addTask(() -> computeOverride(entry.getKey(), entry.getValue()), Executors.newVirtualThreadPerTaskExecutor());
			}

			//Block thread until all armour texture overrides have been loaded
			taskHelper.complete(override -> {
				if (override != null) {
					for (String id : override.left()) {
						ARMOR_OVERRIDES.put(id, override.right());
					}
				}
			});

			CACHE.clear();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Armor Textures] Failed to load armor textures!", e);
		}
	}

	private static Pair<List<String>, ArmorModelOverride> computeOverride(Identifier id, Resource resource) {
		try (BufferedReader reader = resource.getReader()) {
			ArmorModelOverride override = ArmorModelOverride.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();

			return Pair.of(override.itemIds(), override);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Armor Textures] Failed to load armor override {}!", id, e);
		}

		return null;
	}

	/**
	 * The result of this method is cached until textures reload and cleared every 5 minutes because armor model overrides with overrides could
	 * get expensive if they are tested constantly.
	 */
	public static List<ArmorMaterial.Layer> getCustomArmorTextureLayers(ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			int hashCode = getHashCode(stack);

			if (CACHE.containsKey(hashCode)) return CACHE.get(hashCode);

			String id = stack.getSkyblockId();

			if (ARMOR_OVERRIDES.containsKey(id)) {
				ArmorModelOverride modelOverride = ARMOR_OVERRIDES.get(id);

				overrideLoop: for (ArmorModelOverride.ArmorModelSubOverride override : modelOverride.overrides()) {
					for (SkyblockerTexturePredicate predicate : override.predicates()) {
						if (!predicate.test(stack)) continue overrideLoop;
					}

					CACHE.put(hashCode, override.layerOverride());

					return override.layerOverride();
				}

				CACHE.put(hashCode, modelOverride.layers());

				return modelOverride.layers();
			}
		}

		return NO_CUSTOM_TEXTURES;
	}

	/**
	 * Caching is done based on the identity hash code as that won't change unless the item stack instance does.
	 * This method is the most efficient while maintaining accuracy and has a net-zero performance impact as you would expect from the mod.
	 */
	private static int getHashCode(ItemStack stack) {
		return System.identityHashCode(stack);
	}

	private record ArmorModelOverride(List<String> itemIds, List<ArmorMaterial.Layer> layers, ArmorModelSubOverride[] overrides) {
		private static final Codec<ArmorMaterial.Layer> ARMOR_LAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("id").forGetter(i -> Identifier.of(SkyblockerMod.NAMESPACE, "placeholder")),
				Codec.STRING.optionalFieldOf("suffix", "").forGetter(i -> ""),
				Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ArmorMaterial.Layer::isDyeable))
				.apply(instance, ArmorMaterial.Layer::new));
		private static final Codec<List<ArmorMaterial.Layer>> ARMOR_LAYER_LIST_CODEC = ARMOR_LAYER_CODEC.listOf();
		private static final Codec<ArmorModelOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().fieldOf("itemIds").forGetter(ArmorModelOverride::itemIds),
				ARMOR_LAYER_LIST_CODEC.fieldOf("layers").forGetter(ArmorModelOverride::layers),
				ArmorModelSubOverride.CODEC.listOf().xmap(list -> list.toArray(ArmorModelSubOverride[]::new), List::of).optionalFieldOf("overrides", new ArmorModelSubOverride[0]).forGetter(ArmorModelOverride::overrides))
				.apply(instance, ArmorModelOverride::new));

		private record ArmorModelSubOverride(SkyblockerTexturePredicate[] predicates, List<ArmorMaterial.Layer> layerOverride) {
			private static final Codec<ArmorModelSubOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					SkyblockerTexturePredicates.CODEC.fieldOf("predicate").forGetter(ArmorModelSubOverride::predicates),
					ARMOR_LAYER_LIST_CODEC.fieldOf("layers").forGetter(ArmorModelSubOverride::layerOverride))
					.apply(instance, ArmorModelSubOverride::new));
		}
	}
}
