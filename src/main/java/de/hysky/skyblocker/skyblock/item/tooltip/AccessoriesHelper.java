package de.hysky.skyblocker.skyblock.item.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AccessoriesHelper {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("collected_accessories.json");
	private static final Pattern ACCESSORY_BAG_TITLE = Pattern.compile("Accessory Bag(?: \\((?<page>\\d+)\\/\\d+\\))?");
	//UUID -> Profile Id & Data
	private static final ProfiledData<ProfileAccessoryData> COLLECTED_ACCESSORIES = new ProfiledData<>(FILE, ProfileAccessoryData.CODEC, true);
	private static final Predicate<String> NON_EMPTY = s -> !s.isEmpty();
	private static final Predicate<Accessory> HAS_FAMILY = Accessory::hasFamily;
	private static final ToIntFunction<Accessory> ACCESSORY_TIER = Accessory::tier;

	private static Map<String, Accessory> ACCESSORY_DATA = new Object2ObjectOpenHashMap<>();
	//remove??
	private static CompletableFuture<Void> loaded;

	@Init
	public static void init() {
		loaded = COLLECTED_ACCESSORIES.init();
		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && TooltipInfoType.ACCESSORIES.isTooltipEnabled() && !Utils.getProfileId().isEmpty() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Matcher matcher = ACCESSORY_BAG_TITLE.matcher(genericContainerScreen.getTitle().getString());

				if (matcher.matches()) {
					ScreenEvents.afterTick(screen).register(_screen -> {
						GenericContainerScreenHandler handler = genericContainerScreen.getScreenHandler();
						int page = matcher.group("page") != null ? Integer.parseInt(matcher.group("page")) : 1;

						collectAccessories(handler.slots.subList(0, handler.getRows() * 9), page);
					});
				}
			}
		});
	}

	private static void collectAccessories(List<Slot> slots, int page) {
		//Is this even needed?
		if (!loaded.isDone()) return;

		List<String> accessoryIds = slots.stream()
				.map(Slot::getStack)
				.map(ItemUtils::getItemId)
				.filter(NON_EMPTY)
				.toList();

		COLLECTED_ACCESSORIES.computeIfAbsent(ProfileAccessoryData::createDefault).pages()
				.put(page, new ObjectOpenHashSet<>(accessoryIds));
	}

	public static Pair<AccessoryReport, String> calculateReport4Accessory(String accessoryId) {
		if (!ACCESSORY_DATA.containsKey(accessoryId) || Utils.getProfileId().isEmpty()) return Pair.of(AccessoryReport.INELIGIBLE, null);

		Accessory accessory = ACCESSORY_DATA.get(accessoryId);
		Set<Accessory> collectedAccessories = COLLECTED_ACCESSORIES.computeIfAbsent(ProfileAccessoryData::createDefault).pages().values().stream()
				.flatMap(ObjectOpenHashSet::stream)
				.filter(ACCESSORY_DATA::containsKey)
				.map(ACCESSORY_DATA::get)
				.collect(Collectors.toSet());

		// If the accessory doesn't belong to a family
		if (accessory.family().isEmpty()) {
			//If the player has this accessory or player doesn't have this accessory
			return collectedAccessories.contains(accessory) ? Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null) : Pair.of(AccessoryReport.MISSING, "");
		}

		Predicate<Accessory> HAS_SAME_FAMILY = accessory::hasSameFamily;
		Set<Accessory> collectedAccessoriesInTheSameFamily = collectedAccessories.stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		Set<Accessory> accessoriesInTheSameFamily = ACCESSORY_DATA.values().stream()
				.filter(HAS_FAMILY)
				.filter(HAS_SAME_FAMILY)
				.collect(Collectors.toSet());

		int highestTierInFamily = accessoriesInTheSameFamily.stream()
				.mapToInt(ACCESSORY_TIER)
				.max()
				.orElse(0);

		//If the player hasn't collected any accessory in same family
		if (collectedAccessoriesInTheSameFamily.isEmpty()) return Pair.of(AccessoryReport.MISSING, String.format("(%d/%d)", accessory.tier(), highestTierInFamily));

		int highestTierCollectedInFamily = collectedAccessoriesInTheSameFamily.stream()
				.mapToInt(ACCESSORY_TIER)
				.max()
				.getAsInt();

		//If this accessory is the highest tier, and the player has the highest tier accessory in this family
		//This accounts for multiple accessories with the highest tier
		if (accessory.tier() == highestTierInFamily && highestTierCollectedInFamily == highestTierInFamily) return Pair.of(AccessoryReport.HAS_HIGHEST_TIER, null);

		//If this accessory is a higher tier than all the other collected accessories in the same family
		if (accessory.tier() > highestTierCollectedInFamily) return Pair.of(AccessoryReport.IS_GREATER_TIER, String.format("(%d→%d/%d)", highestTierCollectedInFamily, accessory.tier(), highestTierInFamily));

		//If this accessory is a lower tier than one already obtained from same family
		if (accessory.tier() < highestTierCollectedInFamily) return Pair.of(AccessoryReport.OWNS_BETTER_TIER, String.format("(%d→%d/%d)", highestTierCollectedInFamily, accessory.tier(), highestTierInFamily));

		//If there is an accessory in the same family that has a higher tier
		if (accessory.tier() < highestTierInFamily) return Pair.of(AccessoryReport.HAS_GREATER_TIER, String.format("(%d/%d)", highestTierCollectedInFamily, highestTierInFamily));

		return Pair.of(AccessoryReport.MISSING, String.format("(%d/%d)", accessory.tier(), highestTierInFamily));
	}

	public static void refreshData(Map<String, Accessory> data) {
		ACCESSORY_DATA = data;
	}

	private record ProfileAccessoryData(Int2ObjectOpenHashMap<ObjectOpenHashSet<String>> pages) {
		private static final Codec<ProfileAccessoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.INT, Codec.STRING.listOf().xmap(ObjectOpenHashSet::new, ObjectArrayList::new))
						.xmap(Int2ObjectOpenHashMap::new, Int2ObjectOpenHashMap::new).fieldOf("pages").forGetter(ProfileAccessoryData::pages)
		).apply(instance, ProfileAccessoryData::new));

		private static ProfileAccessoryData createDefault() {
			return new ProfileAccessoryData(new Int2ObjectOpenHashMap<>());
		}
	}

	/**
	 * @author AzureAaron
	 * @implSpec <a href="https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/commands/MagicalPowerCommand.java#L475">Aaron's Mod</a>
	 */
	public record Accessory(String id, Optional<String> family, int tier) {
		private static final Codec<Accessory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Accessory::id),
				Codec.STRING.optionalFieldOf("family").forGetter(Accessory::family),
				Codec.INT.optionalFieldOf("tier", 0).forGetter(Accessory::tier)
		).apply(instance, Accessory::new));
		public static final Codec<Map<String, Accessory>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

		private boolean hasFamily() {
			return family.isPresent();
		}

		private boolean hasSameFamily(Accessory other) {
			return other.family().equals(this.family);
		}
	}

	public enum AccessoryReport {
		HAS_HIGHEST_TIER, //You've collected the highest tier - Collected
		IS_GREATER_TIER, //This accessory is an upgrade from the one in the same family that you already have - Upgrade -- Shows you what tier this accessory is in its family
		HAS_GREATER_TIER, //This accessory has a higher tier upgrade - Upgradable -- Shows you the highest tier accessory you've collected in that family
		OWNS_BETTER_TIER, //You've collected an accessory in this family with a higher tier - Downgrade -- Shows you the highest tier accessory you've collected in that family
		MISSING, //You don't have any accessories in this family - Missing
		INELIGIBLE
	}
}
