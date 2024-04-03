package de.hysky.skyblocker.skyblock.item.tooltip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AccessoriesHelper {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("collected_accessories.json");
	private static final Pattern ACCESSORY_BAG_TITLE = Pattern.compile("Accessory Bag \\((?<page>\\d+)/\\d+\\)");
	//UUID -> Profile Id & Data
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileAccessoryData>> COLLECTED_ACCESSORIES = new Object2ObjectOpenHashMap<>();
	private static final Predicate<String> NON_EMPTY = s -> !s.isEmpty();
	private static final Predicate<Accessory> HAS_FAMILY = Accessory::hasFamily;
	private static final ToIntFunction<Accessory> ACCESSORY_TIER = Accessory::tier;

	private static Map<String, Accessory> ACCESSORY_DATA = new Object2ObjectOpenHashMap<>();
	//remove??
	private static CompletableFuture<Void> loaded;

	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register((_client) -> load());
		ClientLifecycleEvents.CLIENT_STOPPING.register((_client) -> save());
		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && TooltipInfoType.ACCESSORIES.isTooltipEnabled() && !Utils.getProfileId().isEmpty() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Matcher matcher = ACCESSORY_BAG_TITLE.matcher(genericContainerScreen.getTitle().getString());

				if (matcher.matches()) {
					ScreenEvents.afterTick(screen).register(_screen -> {
						GenericContainerScreenHandler handler = genericContainerScreen.getScreenHandler();

						collectAccessories(handler.slots.subList(0, handler.getRows() * 9), Integer.parseInt(matcher.group("page")));
					});
				}
			}
		});
	}

	//Note: JsonOps.COMPRESSED must be used if you're using maps with non-string keys
	private static void load() {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				COLLECTED_ACCESSORIES.putAll(ProfileAccessoryData.SERIALIZATION_CODEC.parse(JsonOps.COMPRESSED, JsonParser.parseReader(reader)).result().orElseThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Accessory Helper] Failed to load accessory file!", e);
			}
		});
	}

	private static void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(ProfileAccessoryData.SERIALIZATION_CODEC.encodeStart(JsonOps.COMPRESSED, COLLECTED_ACCESSORIES).result().orElseThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to save accessory file!", e);
		}
	}

	private static void collectAccessories(List<Slot> slots, int page) {	
		//Is this even needed?
		if (!loaded.isDone()) return;

		List<String> accessoryIds = slots.stream()
				.map(Slot::getStack)
				.map(ItemUtils::getItemId)
				.filter(NON_EMPTY)
				.toList();

		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());

        COLLECTED_ACCESSORIES.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(Utils.getProfileId(), profileId -> ProfileAccessoryData.createDefault()).pages()
				.put(page, new ObjectOpenHashSet<>(accessoryIds));
	}

	static Pair<AccessoryReport, String> calculateReport4Accessory(String accessoryId) {
		if (!ACCESSORY_DATA.containsKey(accessoryId) || Utils.getProfileId().isEmpty()) return Pair.of(AccessoryReport.INELIGIBLE, null);

		Accessory accessory = ACCESSORY_DATA.get(accessoryId);		
		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());
		Set<Accessory> collectedAccessories = COLLECTED_ACCESSORIES.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>()).computeIfAbsent(Utils.getProfileId(), profileId -> ProfileAccessoryData.createDefault()).pages().values().stream()
				.flatMap(ObjectOpenHashSet::stream)
				.filter(ACCESSORY_DATA::containsKey)
				.map(ACCESSORY_DATA::get)
				.collect(Collectors.toSet());

		//If the player has this accessory, and it doesn't belong to a family
		if (collectedAccessories.contains(accessory) && accessory.family().isEmpty()) return Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null);

		Predicate<Accessory> HAS_SAME_FAMILY = accessory::hasSameFamily;
		Set<Accessory> collectedAccessoriesInTheSameFamily = collectedAccessories.stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		//If the player doesn't have any collected accessories with same family
		if (collectedAccessoriesInTheSameFamily.isEmpty()) return Pair.of(AccessoryReport.MISSING, null);

		Set<Accessory> accessoriesInTheSameFamily = ACCESSORY_DATA.values().stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		///If the player has the highest tier accessory in this family
		//Take the accessories in the same family as {@code accessory}, then get the one with the highest tier
		Optional<Accessory> highestTierOfFamily = accessoriesInTheSameFamily.stream()
				.max(Comparator.comparingInt(ACCESSORY_TIER));
		int maxTierInFamily = highestTierOfFamily.orElse(Accessory.EMPTY).tier();

		if (collectedAccessoriesInTheSameFamily.stream().anyMatch(ca -> ca.tier() == maxTierInFamily)) return Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null);

		//If this accessory is a higher tier than all the other collected accessories in the same family
		OptionalInt highestTierOfAllCollectedInFamily = collectedAccessoriesInTheSameFamily.stream()
				.mapToInt(ACCESSORY_TIER)
				.max();

		if (accessory.tier() > highestTierOfAllCollectedInFamily.getAsInt()) return Pair.of(AccessoryReport.IS_GREATER_TIER, String.format("(%d/%d)", accessory.tier(), maxTierInFamily));

		//If this accessory is a lower tier than one already obtained from same family
		if (accessory.tier() < highestTierOfAllCollectedInFamily.getAsInt()) return Pair.of(AccessoryReport.OWNS_BETTER_TIER, String.format("(%d/%d)", highestTierOfAllCollectedInFamily.orElse(0), maxTierInFamily));

		//If there is an accessory in the same family that has a higher tier
		//Take the accessories in the same family, then check if there is an accessory whose tier is greater than {@code accessory}
		boolean hasGreaterTierInFamily = accessoriesInTheSameFamily.stream()
				.anyMatch(ca -> ca.tier() > accessory.tier());

		if (hasGreaterTierInFamily) return Pair.of(AccessoryReport.HAS_GREATER_TIER, String.format("(%d/%d)", highestTierOfAllCollectedInFamily.orElse(0), maxTierInFamily));

		return Pair.of(AccessoryReport.MISSING, null);
	}

	static void refreshData(JsonObject data) {
		try {
            ACCESSORY_DATA = Accessory.MAP_CODEC.parse(JsonOps.INSTANCE, data).result().orElseThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to parse data!", e);
		}
	}

	private record ProfileAccessoryData(Int2ObjectOpenHashMap<ObjectOpenHashSet<String>> pages) {
		private static final Codec<ProfileAccessoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.INT, Codec.STRING.listOf().xmap(ObjectOpenHashSet::new, ObjectArrayList::new))
				.xmap(Int2ObjectOpenHashMap::new, Int2ObjectOpenHashMap::new).fieldOf("pages").forGetter(ProfileAccessoryData::pages))
				.apply(instance, ProfileAccessoryData::new));
		private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileAccessoryData>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, CODEC)
				.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new))
		.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

		private static ProfileAccessoryData createDefault() {
			return new ProfileAccessoryData(new Int2ObjectOpenHashMap<>());
		}
	}

	/**
	 * @author AzureAaron
	 * @implSpec <a href="https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/commands/MagicalPowerCommand.java#L475">Aaron's Mod</a>
	 */
	private record Accessory(String id, Optional<String> family, int tier) {
		private static final Codec<Accessory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Accessory::id),
				Codec.STRING.optionalFieldOf("family").forGetter(Accessory::family),
				Codec.INT.optionalFieldOf("tier", 0).forGetter(Accessory::tier))
				.apply(instance, Accessory::new));
		private static final Codec<Map<String, Accessory>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
		private static final Accessory EMPTY = new Accessory("", Optional.empty(), 0);
		
		private boolean hasFamily() {
			return family.isPresent();
		}

		private boolean hasSameFamily(Accessory other) {
			return other.family().equals(this.family);
		}
	}

	enum AccessoryReport {
		HAS_HIGHEST_TIER, //You've collected the highest tier - Collected
		IS_GREATER_TIER, //This accessory is an upgrade from the one in the same family that you already have - Upgrade -- Shows you what tier this accessory is in its family
		HAS_GREATER_TIER, //This accessory has a higher tier upgrade - Upgradable -- Shows you the highest tier accessory you've collected in that family
		OWNS_BETTER_TIER, //You've collected an accessory in this family with a higher tier - Downgrade -- Shows you the highest tier accessory you've collected in that family
		MISSING, //You don't have any accessories in this family - Missing
		INELIGIBLE
	}
}
