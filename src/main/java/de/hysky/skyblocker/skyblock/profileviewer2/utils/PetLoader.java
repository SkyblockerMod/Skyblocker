package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer2.model.PetsData;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import io.github.moulberry.repo.constants.PetLevelingBehaviourOverride;
import io.github.moulberry.repo.constants.PetLevelingData;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import io.github.moulberry.repo.util.PetId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

public class PetLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Comparator<PetsData.Pet> PETS_COMPARATOR = Comparator.comparing((PetsData.Pet pet) -> SkyblockItemRarity.valueOf(pet.tier))
			.reversed()
			.thenComparing(pet -> pet.type);
	private static final Pattern STATS_NUMBER_PATTERN = Pattern.compile("\\{(?<name>[A-Za-z_]+)\\}");
	private static final Pattern OTHER_NUMBER_PATTERN = Pattern.compile("\\{(?<index>\\d+)\\}");

	public static List<ItemStack> parsePets(List<PetsData.Pet> pets) {
		// Allows us to add pets to the list in the order that they should display in their widget
		List<PetsData.Pet> petsSorted = new ArrayList<>(pets);
		petsSorted.sort(PETS_COMPARATOR);

		List<ItemStack> petItems = new ArrayList<>();

		for (PetsData.Pet pet : petsSorted) {
			try {
				petItems.add(parsePet(pet));
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profile Viewer Pet Loader] Failed to load pet: {}.", pet, e);
			}
		}

		return List.copyOf(petItems);
	}

	private static ItemStack parsePet(PetsData.Pet pet) {
		if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) {
			return ItemUtils.getNamedPlaceholder("NEU Repo / Items not loaded").getStackOrThrow();
		}

		SkyblockItemRarity petRarity = getPetRarity(pet);
		Rarity neuPetRarity = petRarity.toNeuRarity();
		String neuPetId = pet.type + ";" + getRarityIndex(petRarity);
		FlexibleItemStack baseStack = ItemRepository.getItemStack(neuPetId);
		NEUItem petItem = NEURepoManager.getItemByNeuId(neuPetId);

		// If the base stack is null then return since we can't guess what the pet looks like
		if (baseStack == null || petItem == null) {
			LOGGER.warn("[Skyblocker Profile Viewer Pet Loader] Failed to get pet data for {}.", neuPetId);
			return ItemUtils.getItemIdPlaceholder(neuPetId).getStackOrThrow();
		}

		PetLevelingData petLevellingData = NEURepoManager.getConstants().getPetLevelingData();
		Map<@PetId String, Map<Rarity, PetNumbers>> petNumbersData = NEURepoManager.getConstants().getPetNumbers();

		// Ensure the pet has the numbers data for the given rarity
		if (!petNumbersData.containsKey(pet.type) || !petNumbersData.get(pet.type).containsKey(neuPetRarity)) {
			LOGGER.warn("[Skyblocker Profile Viewer Pet Loader] Mising pet numbers for {}.", pet.type);
			return ItemUtils.getItemIdPlaceholder(pet.type).getStackOrThrow();
		}

		int petLevel = calculatePetLevel(pet, pet.type, neuPetRarity, petLevellingData);
		PetNumbers petNumbers = petNumbersData.get(pet.type).get(neuPetRarity);

		Component name = formatName(petItem, petLevel);
		List<Component> lore = formatLore(petItem, petNumbers, petLevel);
		ResolvableProfile head = getHeadIcon(pet, baseStack.get(DataComponents.PROFILE));

		FlexibleItemStack petStack = baseStack.copy();
		petStack.set(DataComponents.CUSTOM_NAME, name);
		petStack.set(DataComponents.LORE, new ItemLore(lore));
		petStack.set(DataComponents.PROFILE, head);

		return petStack.getStackOrThrow();
	}

	private static int calculatePetLevel(PetsData.Pet pet, String petId, Rarity neuPetRarity, PetLevelingData petLevellingData) {
		int maxLevel = 100;
		int xpCostsOffset = petLevellingData.getPetLevelStartOffset().get(neuPetRarity);
		List<Integer> allXpCosts = new ArrayList<>(petLevellingData.getPetExpCostForLevel());

		if (petLevellingData.getPetLevelingBehaviourOverrides().containsKey(petId)) {
			PetLevelingBehaviourOverride override = petLevellingData.getPetLevelingBehaviourOverrides().get(petId);

			// Adjust max level
			if (override.getMaxLevel() != null) {
				maxLevel = override.getMaxLevel().intValue();
			}

			// Apply allXpCosts override
			if (override.getPetExpCostModifierType() != null && override.getPetExpCostModifier() != null) {
				switch (override.getPetExpCostModifierType()) {
				case PetLevelingBehaviourOverride.PetExpModifierType.APPEND -> allXpCosts.addAll(override.getPetExpCostModifier());
				case PetLevelingBehaviourOverride.PetExpModifierType.REPLACE -> allXpCosts = override.getPetExpCostModifier();
				}
			}

			// Apply xpCostsOffset override
			if (override.getPetLevelStartOffset() != null) {
				xpCostsOffset = override.getPetLevelStartOffset().get(neuPetRarity);
			}
		}

		List<Integer> xpCosts = allXpCosts.subList(xpCostsOffset, xpCostsOffset + maxLevel - 1);
		int level = 1;
		int totalXp = 0;

		for (int lvlXp : xpCosts) {
			totalXp += lvlXp;

			if (totalXp > pet.exp) {
				totalXp -= lvlXp;

				break;
			}

			level++;
		}

		return Math.min(level, maxLevel);
	}

	private static Component formatName(NEUItem petItem, int petLevel) {
		String name = petItem.getDisplayName();
		name = name.replace("{LVL}", String.valueOf(petLevel));

		return TextTransformer.fromLegacy(name);
	}

	private static List<Component> formatLore(NEUItem petItem, PetNumbers petNumbers, int petLevel) {
		List<String> unformattedLore = petItem.getLore();
		List<String> formattedLore = new ArrayList<>();
		PetNumbers.Stats stats = petNumbers.interpolatedStatsAtLevel(petLevel);

		// Lines may have a {SEA_CREATURE_CHANCE} (or some other stat) variable which corresponds to a statNumbers entry.
		// Lines may have a {0} variable and that corresponds to the index in the otherNumbers list where the right value is.

		for (String line : unformattedLore) {
			// Filter out garbage lines
			if (line.contains("Right-click to add this") || line.contains("pet menu!")) {
				continue;
			}

			Matcher statsNumberMatcher = STATS_NUMBER_PATTERN.matcher(line);
			Set<String> statsReplacements = new HashSet<>();

			// Collect the statNumbers names in this line for replacement
			while (statsNumberMatcher.find()) {
				String statName = statsNumberMatcher.group("name");
				statsReplacements.add(statName);
			}

			Matcher otherNumberMatcher = OTHER_NUMBER_PATTERN.matcher(line);
			Set<Integer> otherNumberReplacements = new HashSet<>();

			// Collect the otherNumbers indices in this line for replacement
			while (otherNumberMatcher.find()) {
				int index = Integer.parseInt(otherNumberMatcher.group("index"));
				otherNumberReplacements.add(index);
			}

			// Replace lore number variables
			String formattedLine = line;

			for (String statName : statsReplacements) {
				double replacement = stats.getStatNumbers().get(statName).doubleValue();
				formattedLine = formattedLine.replace("{" + statName + "}", Formatters.TRIPLE_NUMBERS.format(replacement));
			}

			for (int index : otherNumberReplacements) {
				double replacement = stats.getOtherNumbers().get(index).doubleValue();
				formattedLine = formattedLine.replace("{" + index + "}", Formatters.TRIPLE_NUMBERS.format(replacement));
			}

			formattedLore.add(formattedLine);
		}

		List<Component> componentLore = new ArrayList<>();
		formattedLore.forEach(line -> componentLore.add(TextTransformer.fromLegacy(line)));

		return componentLore;
	}

	@SuppressWarnings("unused")
	private static void appendLoreExtras(PetsData.Pet pet, List<Component> lore) {
		// Add pet skin name to first line if applicable
		if (pet.skin != null) {
			String petSkinName = ItemRepository.getItemStack("PET_SKIN_" + pet.skin, FlexibleItemStack.EMPTY)
					.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty())
					.getString();
			Component newFirstLine = Component.empty()
					.append(lore.getFirst())
					.append(Component.literal(", " + petSkinName).withStyle(ChatFormatting.DARK_GRAY));

			lore.set(0, newFirstLine);
		}

		// Add held item text
		if (pet.heldItem != null) {
			FlexibleItemStack heldItem = ItemRepository.getItemStack(pet.heldItem);

			if (heldItem != null && heldItem.get(DataComponents.CUSTOM_NAME) != null) {
				Component text = Component.empty()
						.append(Component.literal("Held Item: ").withStyle(ChatFormatting.GOLD))
						.append(heldItem.get(DataComponents.CUSTOM_NAME));

				lore.add(lore.size(), text);
				lore.add(lore.size(), Component.empty());
			}
		}


	}

	private static ResolvableProfile getHeadIcon(PetsData.Pet pet, ResolvableProfile original) {
		if (pet.skin != null) {
			FlexibleItemStack petSkin = ItemRepository.getItemStack("PET_SKIN_" + pet.skin);

			if (petSkin != null && petSkin.get(DataComponents.PROFILE) != null) {
				return petSkin.get(DataComponents.PROFILE);
			}
		}

		return original;
	}

	private static SkyblockItemRarity getPetRarity(PetsData.Pet pet) {
		SkyblockItemRarity rarity = SkyblockItemRarity.valueOf(pet.tier);

		// Apply tier boost to pets whose base rarity is less than legendary
		if ("PET_ITEM_TIER_BOOST".equals(pet.heldItem) && rarity.compareTo(SkyblockItemRarity.LEGENDARY) > 0) {
			rarity = rarity.next();
		}

		return rarity;
	}

	// TODO more gracefully fail since this will blow up all item loading
	private static int getRarityIndex(SkyblockItemRarity rarity) {
		return switch (rarity) {
		case SkyblockItemRarity.COMMON -> 0;
		case SkyblockItemRarity.UNCOMMON -> 1;
		case SkyblockItemRarity.RARE -> 2;
		case SkyblockItemRarity.EPIC -> 3;
		case SkyblockItemRarity.LEGENDARY -> 4;
		case SkyblockItemRarity.MYTHIC -> 5;
		default -> throw new IllegalArgumentException(String.format("Rarity %s is not supported!", rarity));
		};
	}
}
