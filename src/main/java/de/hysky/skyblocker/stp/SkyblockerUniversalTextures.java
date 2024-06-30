package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.stp.matchers.RegexMatcher;
import de.hysky.skyblocker.stp.matchers.StringMatcher;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import de.hysky.skyblocker.utils.MultithreadedTaskHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.OnLoad;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class SkyblockerUniversalTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String UNIVERSAL_MODELS_FOLDER = "universal";
	private static final Object2ObjectMap<UniversalTextureRule, Identifier> ALL_UNIVERSAL_TEXTURES = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<UniversalTextureRule, Identifier> APPLICABLE_UNIVERSAL_TEXTURES = new Object2ObjectOpenHashMap<>();
	public static final Int2ObjectMap<Identifier> UNIVERSAL_TEXTURE_CACHE = new Int2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			apply(screen);
			ScreenEvents.remove(screen).register(_screen1 -> apply(null));
		});
		PreparableModelLoadingPlugin.register(SkyblockerUniversalTextures::prepareUniversalModels, (data, pluginContext) -> {
			pluginContext.addModels(data);
			pluginContext.modifyModelOnLoad().register(SkyblockerUniversalTextures::onModelLoad);
		});
		//Clear cache every 2 minutes
		Scheduler.INSTANCE.scheduleCyclic(UNIVERSAL_TEXTURE_CACHE::clear, 20 * 60 * 2);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> UNIVERSAL_TEXTURE_CACHE.clear());
	}

	private static CompletableFuture<Collection<Identifier>> prepareUniversalModels(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				ALL_UNIVERSAL_TEXTURES.clear();
				APPLICABLE_UNIVERSAL_TEXTURES.clear();

				Map<Identifier, Resource> modelFiles = manager.findResources(ModelHelper.MODELS_PREFIX + UNIVERSAL_MODELS_FOLDER, ModelHelper::isValidJsonPath);
				Set<Identifier> models2Load = new ObjectOpenHashSet<>();
				MultithreadedTaskHelper<Pair<UniversalTextureRule, Identifier>> taskHelper = MultithreadedTaskHelper.create();

				for (Map.Entry<Identifier, Resource> entry : modelFiles.entrySet()) {
					Identifier normalized = ModelHelper.normalizeModelId(entry.getKey());

					taskHelper.addTask(() -> loadUniversalTexture(normalized, entry.getValue()), Executors.newVirtualThreadPerTaskExecutor());
					models2Load.add(normalized);
				}

				taskHelper.complete(pair -> {
					if (pair != null) ALL_UNIVERSAL_TEXTURES.put(pair.left(), pair.right());
				});
				apply(null);

				return models2Load;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Universal Textures] Failed to prepare models for baking!", e);
			}

			return Set.of();
		}, executor);
	}

	private static Pair<UniversalTextureRule, Identifier> loadUniversalTexture(Identifier modelId, Resource resource) {
		try (BufferedReader reader = resource.getReader()) {
			MapLike<JsonElement> model = JsonOps.INSTANCE.getMap(JsonParser.parseReader(reader)).getOrThrow();
			Optional<UniversalTextureRule> rule = UniversalTextureRule.FIELD_CODEC.decode(JsonOps.INSTANCE, model).getOrThrow();

			if (rule.isPresent()) return Pair.of(rule.get(), modelId);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Universal Textures] Failed to load universal texture!", e);
		}

		return null;
	}

	private static UnbakedModel onModelLoad(UnbakedModel model, OnLoad.Context context) {
		ModelHelper.applyTextureSugar(model, context.resourceId());

		return model;
	}

	private static void apply(@Nullable Screen screen) {
		APPLICABLE_UNIVERSAL_TEXTURES.clear();

		for (Map.Entry<UniversalTextureRule, Identifier> entry : Object2ObjectMaps.fastIterable(ALL_UNIVERSAL_TEXTURES))  {
			UniversalTextureRule rule = entry.getKey();
			Identifier modelId = entry.getValue();

			if (screen != null && rule.containerNames().isPresent()) {
				for (ContainerNameMatcher nameMatcher : rule.containerNames().get()) {
					if (nameMatcher.test(screen)) APPLICABLE_UNIVERSAL_TEXTURES.put(rule, modelId);
				}
			} else {
				APPLICABLE_UNIVERSAL_TEXTURES.put(rule, modelId);
			}
		}

		UNIVERSAL_TEXTURE_CACHE.clear();
	}

	public static Identifier getUniversalModel(ItemStack stack) {
		for (Map.Entry<UniversalTextureRule, Identifier> entry : Object2ObjectMaps.fastIterable(APPLICABLE_UNIVERSAL_TEXTURES)) {
			if (entry.getKey().test(stack)) return entry.getValue();
		}

		return null;
	}

	private record UniversalTextureRule(Optional<List<ContainerNameMatcher>> containerNames, SkyblockerTexturePredicate[] predicate) implements Predicate<ItemStack> {
		private static final Codec<UniversalTextureRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ContainerNameMatcher.CODEC.listOf().optionalFieldOf("containerNames").forGetter(UniversalTextureRule::containerNames),
				SkyblockerTexturePredicates.CODEC.fieldOf("predicate").forGetter(UniversalTextureRule::predicate))
				.apply(instance, UniversalTextureRule::new));
		private static final MapCodec<Optional<UniversalTextureRule>> FIELD_CODEC = CODEC.optionalFieldOf("skyblocker");

		/**
		 * @implNote The screen matchers are tested ahead-of-time.
		 */
		@Override
		public boolean test(ItemStack stack) {
			for (SkyblockerTexturePredicate texPredicate : predicate) {
				if (!texPredicate.test(stack)) return false;
			}

			return true;
		}
	}

	private record ContainerNameMatcher(Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher) implements Predicate<Screen> {
		private static final Codec<ContainerNameMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				StringMatcher.CODEC.optionalFieldOf("stringMatcher").forGetter(ContainerNameMatcher::stringMatcher),
				RegexMatcher.CODEC.optionalFieldOf("regexMatcher").forGetter(ContainerNameMatcher::regexMatcher))
				.apply(instance, ContainerNameMatcher::new));

		@Override
		public boolean test(Screen screen) {
			//Also acts as an implicit null check
			if (screen instanceof GenericContainerScreen) {
				String title = screen.getTitle().getString();

				if (stringMatcher.isPresent() && stringMatcher.get().test(title)) return true;
				if (regexMatcher.isPresent() && regexMatcher.get().test(title)) return true;
			}

			return false;
		}
	}
}
