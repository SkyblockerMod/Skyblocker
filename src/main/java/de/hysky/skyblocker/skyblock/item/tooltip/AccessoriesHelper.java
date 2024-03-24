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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AccessoriesHelper {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("collected_accessories.json");
	private static final Pattern ACCESSORY_BAG_TITLE = Pattern.compile("Accessory Bag \\(\\d+\\/\\d+\\)");
	//UUID -> Profile Id & Data
	private static final Map<String, Map<String, ProfileAccessoryData>> COLLECTED_ACCESSORIES = new Object2ObjectOpenHashMap<>();
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
				if (ACCESSORY_BAG_TITLE.matcher(genericContainerScreen.getTitle().getString()).matches()) {
					ScreenEvents.afterRender(screen).register((_screen, _context, _mouseX, _mouseY, _delta) -> {
						GenericContainerScreenHandler handler = genericContainerScreen.getScreenHandler();

						collectAccessories(handler.slots.subList(0, handler.getRows() * 9));
					});
				}
			}
		});
	}

	private static void load() {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				COLLECTED_ACCESSORIES.putAll(ProfileAccessoryData.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result().orElseThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Accessory Helper] Failed to load accessory file!", e);
			}
		});
	}

	private static void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(ProfileAccessoryData.SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, COLLECTED_ACCESSORIES).result().orElseThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to save accessory file!", e);
		}
	}

	private static void collectAccessories(List<Slot> slots) {	
		//Is this even needed?
		if (!loaded.isDone()) return;

		List<String> accessoryIds = slots.stream()
				.map(Slot::getStack)
				.map(ItemUtils::getItemId)
				.filter(NON_EMPTY)
				.collect(Collectors.toUnmodifiableList());

		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());

		Map<String, ProfileAccessoryData> playerData = COLLECTED_ACCESSORIES.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
		playerData.putIfAbsent(Utils.getProfileId(), ProfileAccessoryData.createDefault());

		ProfileAccessoryData profileData = playerData.get(Utils.getProfileId());

		profileData.accessoryIds().addAll(accessoryIds);
	}

	static AccessoryReport calculateReport4Accessory(String accessoryId) {
		if (!ACCESSORY_DATA.containsKey(accessoryId) || Utils.getProfileId().isEmpty()) return AccessoryReport.INELIGIBLE;

		Accessory accessory = ACCESSORY_DATA.get(accessoryId);		
		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());
		Set<Accessory> collectedAccessories = COLLECTED_ACCESSORIES.get(uuid).get(Utils.getProfileId()).accessoryIds().stream()
				.filter(ACCESSORY_DATA::containsKey)
				.map(ACCESSORY_DATA::get)
				.collect(Collectors.toSet());

		//If the player has this accessory and it doesn't belong to a family
		if (collectedAccessories.contains(accessory) && accessory.family().isEmpty()) return AccessoryReport.HAS_HIGHEST_TIER;

		Predicate<Accessory> HAS_SAME_FAMILY = accessory::hasSameFamily;
		Set<Accessory> collectedAccessoriesInTheSameFamily = collectedAccessories.stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		//If the player doesn't have any collected accessories with same family
		if (collectedAccessoriesInTheSameFamily.isEmpty()) return AccessoryReport.MISSING;

		Set<Accessory> accessoriesInTheSameFamily = ACCESSORY_DATA.values().stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		///If the player has the highest tier accessory in this family
		//Take the the accessories in the same family as {@code accessory}, then get the one with the highest tier
		Optional<Accessory> highestTierOfFamily = accessoriesInTheSameFamily.stream()
				.max(Comparator.comparingInt(ACCESSORY_TIER));

		if (highestTierOfFamily.isPresent()) {
			Accessory highestTier = highestTierOfFamily.orElseThrow();

			if (collectedAccessoriesInTheSameFamily.contains(highestTier)) return AccessoryReport.HAS_HIGHEST_TIER;

			//For when the highest tier is tied
			if (highestTier.hasSameFamily(accessory) && collectedAccessoriesInTheSameFamily.stream().allMatch(ca -> ca.tier() == highestTier.tier())) return AccessoryReport.HAS_HIGHEST_TIER;
		}

		//If this accessory is a higher tier than all of other collected accessories in the same family
		OptionalInt highestTierOfAllCollectedInFamily = collectedAccessoriesInTheSameFamily.stream()
				.mapToInt(ACCESSORY_TIER)
				.max();

		if (highestTierOfAllCollectedInFamily.isPresent() && accessory.tier() > highestTierOfAllCollectedInFamily.orElseThrow()) return AccessoryReport.IS_GREATER_TIER;

		//If this accessory is a lower tier than one already obtained from same family
		if (highestTierOfAllCollectedInFamily.isPresent() && accessory.tier() < highestTierOfAllCollectedInFamily.orElseThrow()) return AccessoryReport.OWNS_BETTER_TIER;

		//If there is an accessory in the same family that has a higher tier
		//Take the accessories in the same family, then check if there is an accessory whose tier is greater than {@code accessory}
		boolean hasGreaterTierInFamily = accessoriesInTheSameFamily.stream()
				.anyMatch(ca -> ca.tier() > accessory.tier());

		if (hasGreaterTierInFamily) return AccessoryReport.HAS_GREATER_TIER;

		return AccessoryReport.MISSING;
	}

	static void refreshData(JsonObject data) {
		try {
			Map<String, Accessory> accessoryData = Accessory.MAP_CODEC.parse(JsonOps.INSTANCE, data).result().orElseThrow();

			ACCESSORY_DATA = accessoryData;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Accessory Helper] Failed to parse data!", e);
		}
	}

	private record ProfileAccessoryData(Set<String> accessoryIds) {
		private static final Codec<ProfileAccessoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf()
				.xmap(ObjectOpenHashSet::new, ObjectArrayList::new)
				.fieldOf("accessoryIds")
				.forGetter(i -> new ObjectOpenHashSet<String>(i.accessoryIds())))
				.apply(instance, ProfileAccessoryData::new));
		//Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static final Codec<Map<String, Map<String, ProfileAccessoryData>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, CODEC)
				.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new))
		.xmap(Object2ObjectOpenHashMap::new, m -> (Map) new Object2ObjectOpenHashMap(m));

		private static ProfileAccessoryData createDefault() {
			return new ProfileAccessoryData(new ObjectOpenHashSet<>());
		}
	}

	/**
	 * @author AzureAaron
	 * @implSpec <a href="https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/commands/MagicalPowerCommand.java#L393">Aaron's Mod</a>
	 */
	private record Accessory(String id, Optional<String> family, int tier) {
		private static final Codec<Accessory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Accessory::id),
				Codec.STRING.optionalFieldOf("family").forGetter(Accessory::family),
				Codec.INT.optionalFieldOf("tier", 0).forGetter(Accessory::tier))
				.apply(instance, Accessory::new));
		private static final Codec<Map<String, Accessory>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
		
		private boolean hasFamily() {
			return family.isPresent();
		}

		private boolean hasSameFamily(Accessory other) {
			return other.family().equals(this.family);
		}
	}

	enum AccessoryReport {
		HAS_HIGHEST_TIER, //You've collected the highest tier - Collected
		IS_GREATER_TIER, //This accessory is an upgrade from the one in the same family that you already have - Upgrade
		HAS_GREATER_TIER, //This accessory has a higher tier upgrade - Upgradable
		OWNS_BETTER_TIER, //You've collected an accessory in this family with a higher tier - Downgrade
		MISSING, //You don't have any accessories in this family - Missing
		INELIGIBLE;
	}
}
