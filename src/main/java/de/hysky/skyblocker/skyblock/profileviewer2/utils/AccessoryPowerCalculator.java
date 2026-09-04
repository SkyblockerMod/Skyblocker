package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.accessories.AccessoriesHelper.Accessory;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.profileviewer2.ProfileViewer;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.SkyBlockIcons;

public class AccessoryPowerCalculator {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEBUG_AP_CALCULATION = false;
	private static final List<String> STAT_ORDER = List.of(
			"health", "defence", "true_defence", "strength", "critical_chance",
			"critical_damage", "attack_speed", "ferocity", "intelligence", "walk_speed",
			"combat_wisdom");
	private static Map<String, Power> powers = Map.of();

	@Init
	public static void init() {
		if (ProfileViewer.ENABLED) {
			CompletableFuture.supplyAsync(AccessoryPowerCalculator::loadPowers, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR)
			.thenAcceptAsync(data -> powers = data, Minecraft.getInstance());
		}
	}

	private static Map<String, Power> loadPowers() {
		try {
			String response = Http.sendGetRequest("https://api.azureaaron.net/skyblock/magicalpowers");

			return Power.MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getOrThrow();
		} catch (Exception e) {
			LOGGER.info("[Skyblocker Profile Viewer] Failed to load accessory powers.", e);
		}

		return Map.of();
	}

	public static int calculateAccessoryPower(ProfileMember member, List<ItemStack> accessories) {
		boolean consumedRiftPrism = member.rift.access.consumedPrism;

		// Step 1: Find the highest rarity of each individual accessory in the bag (also deduplicates them)
		Map<String, ItemStack> highestRarities = new HashMap<>();

		for (ItemStack accessory : accessories) {
			String id = accessory.getSkyblockId();

			// Ignore items without a skyblock id - otherwise it tries to put air items into the calculation :(
			if (id.isEmpty()) {
				continue;
			}

			ItemStack mapping = highestRarities.get(id);

			// If the map doesn't have the accessory yet add it, otherwise if it does and this accessory has a higher
			// rarity then it gets replaced
			if (mapping == null || accessory.getSkyblockRarity().compareTo(mapping.getSkyblockRarity()) > 0) {
				highestRarities.put(id, accessory);
			}
		}

		// If the player has consumed a rift prism then ignore any in their bag
		if (consumedRiftPrism) {
			highestRarities.remove("RIFT_PRISM");
		}

		// Step 2: Find the highest tier in each accessory family and remove all lower tiers
		Map<String, Accessory> accessoryData = TooltipInfoType.ACCESSORIES.getData();
		List<String> accessoriesToDrop = new ArrayList<>();

		// If the accessory has a family and is the highest tier, remove all lower tiers
		// Accessories without a family fall through this
		for (String id : highestRarities.keySet()) {
			Accessory data = accessoryData.get(id);

			// Only check accessories with data & a family
			// Also ensure that we aren't checking accessories that are to be dropped otherwise
			// for accessories with multiple of the same tier in the bag it will result in none counting since
			// the two would cancel each other out
			if (data != null && data.family().isPresent() && !accessoriesToDrop.contains(id)) {
				// Find all accessories in the same family
				List<Accessory> familyMembers = accessoryData.values().stream()
						.filter(Accessory::hasFamily)
						.filter(data::hasSameFamily)
						.toList();
				// Find the highest tier in the family
				int highestTier = familyMembers.stream()
						.max(Comparator.comparingInt(Accessory::tier))
						.orElseThrow()
						.tier();

				// Drop all other accessories (siblings) in the family if this accessory is the highest tier
				if (data.tier() == highestTier) {
					List<String> siblings = familyMembers.stream()
							.filter(Predicate.not(data::equals))
							.map(Accessory::id)
							.toList();

					accessoriesToDrop.addAll(siblings);
				}
			}
		}

		// Remove all accessories from our map that needed it
		accessoriesToDrop.forEach(highestRarities::remove);

		// Step 3: Now that we have all the player's accessories of both the highest rarity & highest tier in family (where applicable)
		// we can add up the accessory power given by each!
		int accessoryPower = highestRarities.values().stream()
				.map(ItemStack::getSkyblockRarity)
				.mapToInt(SkyblockItemRarity::getMP)
				.sum();

		// Step 4: Apply AP bonuses

		// Consuming a Rift Prism grants 11 AP
		if (consumedRiftPrism) {
			accessoryPower += 11;
		}

		// Abiphone Contacts Bonus (via Abicase)
		if (accessories.stream().anyMatch(stack -> stack.getSkyblockId().equals("ABICASE"))) {
			double acquiredContacts = member.netherIslandPlayerData.abiphone.activeContacts.size();

			accessoryPower += Math.floor(acquiredContacts / 2d);
		}

		// Hegemony Artifact counts for double the AP so we simply add its AP value to the total for the second time
		ItemStack hegemony = highestRarities.get("HEGEMONY_ARTIFACT");

		if (hegemony != null) {
			accessoryPower += hegemony.getSkyblockRarity().getMP();
		}

		if (DEBUG_AP_CALCULATION) {
			// Rarity -> Accessories
			IO.println(highestRarities.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getValue().getSkyblockRarity(), Collectors.mapping(Map.Entry::getKey, Collectors.toList()))));
			// Rarity totals
			IO.println(highestRarities.values().stream().map(ItemStack::getSkyblockRarity).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
		}

		return accessoryPower;
	}

	private static double calculateStatMultiplier(double accessoryPower) {
		return 29.97d * Math.pow(Math.log((0.0019d * accessoryPower) + 1d), 1.2d);
	}

	private static double coerce0(@Nullable Double number) {
		return number != null ? number : 0;
	}

	public static List<Component> getPowerStats(int accessoryPower, String selectedPower) {
		List<Component> lines = new ArrayList<>();
		Power power = powers.getOrDefault(selectedPower, Power.EMPTY);
		double statMultiplier = calculateStatMultiplier(accessoryPower);

		for (String statId : STAT_ORDER) {
			Double amount = null;

			// Normal stats from the power
			if (power.stats().containsKey(statId)) {
				amount = coerce0(amount) + power.stats().get(statId) * statMultiplier;
			}

			// Unique Power Bonus
			if (power.bonus().containsKey(statId)) {
				amount = coerce0(amount) + power.bonus().get(statId);
			}

			// Add line if the stat exists
			if (amount != null) {
				lines.add(formatStatById(statId, amount));
			}
		}

		if (lines.isEmpty()) {
			lines.add(Component.literal("Unknown stats...").withStyle(ChatFormatting.GRAY));
		}

		return List.copyOf(lines);
	}

	private static Component formatStatById(String id, double amount) {
		return switch (id) {
			case "health" -> formatHealth(amount);
			case "defence" -> formatDefence(amount);
			case "true_defence" -> formatTrueDefence(amount);
			case "strength" -> formatStrength(amount);
			case "critical_chance" -> formatCritChance(amount);
			case "critical_damage" -> formatCritDamage(amount);
			case "attack_speed" -> formatAttackSpeed(amount);
			case "ferocity" -> formatFerocity(amount);
			case "intelligence" -> formatIntelligence(amount);
			case "ability_damage" -> formatAbilityDamage(amount);
			case "walk_speed" -> formatWalkSpeed(amount);
			case "combat_wisdom" -> formatCombatWisdom(amount);
			default -> Component.literal(formatStat('?', amount)).withStyle(ChatFormatting.GRAY);
		};
	}

	public static Component formatHealth(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.HEALTH, amount)).withStyle(ChatFormatting.RED);
	}

	public static Component formatDefence(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.DEFENSE, amount)).withStyle(ChatFormatting.GREEN);
	}

	public static Component formatTrueDefence(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.TRUE_DEFENSE, amount)).withStyle(ChatFormatting.WHITE);
	}

	public static Component formatStrength(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.STRENGTH, amount)).withStyle(ChatFormatting.RED);
	}

	public static Component formatCritChance(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.CRIT_CHANCE, amount)).withStyle(ChatFormatting.BLUE);
	}

	public static Component formatCritDamage(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.CRIT_DAMAGE, amount)).withStyle(ChatFormatting.BLUE);
	}

	public static Component formatAttackSpeed(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.ATTACK_SPEED, amount)).withStyle(ChatFormatting.YELLOW);
	}

	public static Component formatFerocity(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.FEROCITY, amount)).withStyle(ChatFormatting.RED);
	}

	public static Component formatIntelligence(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.INTELLIGENCE, amount)).withStyle(ChatFormatting.AQUA);
	}

	public static Component formatAbilityDamage(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.ABILITY_DAMAGE, amount)).withStyle(ChatFormatting.RED);
	}

	public static Component formatWalkSpeed(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.SPEED, amount)).withStyle(ChatFormatting.WHITE);
	}

	public static Component formatCombatWisdom(double amount) {
		return Component.literal(formatStat(SkyBlockIcons.COMBAT_WISDOM, amount)).withStyle(ChatFormatting.DARK_AQUA);
	}

	private static String formatStat(char symbol, double amount) {
		String sign = amount >= 0 ? "+" : "";
		return String.format(Locale.ENGLISH, "%s%s%c", sign, Formatters.DOUBLE_NUMBERS.format(amount), symbol);
	}

	private record Power(Map<String, Double> stats, Map<String, Double> bonus) {
		private static final Codec<Power> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("stats").forGetter(Power::stats),
				Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("bonus", Map.of()).forGetter(Power::bonus)
				).apply(instance, Power::new));
		private static final Codec<Map<String, Power>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
		private static final Power EMPTY = new Power(Map.of(), Map.of());
	}
}
