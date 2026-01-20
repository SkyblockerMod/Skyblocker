package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import static de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils.numLetterFormat;

public class Pet {
	private static final Pattern statsMatcher = Pattern.compile("\\{[A-Za-z_]+}");
	private static final Pattern numberMatcher = Pattern.compile("\\{\\d+}");

	private final String name;
	private final double xp;
	private final SkyblockItemRarity tier;
	private final Optional<String> heldItem;
	private final Optional<String> skin;
	private final Optional<ResolvableProfile> skinTexture;
	private final int level;
	private final double perecentageToLevel;
	private final long levelXP;
	private final long nextLevelXP;
	private final ItemStack icon;

	public Pet(PetInfo petData) {
		LevelFinder.LevelInfo info = LevelFinder.getLevelInfo(petData.type().equals("GOLDEN_DRAGON") ? "PET_GREG" : "PET_" + petData.tier(), (long) petData.exp());
		this.name = petData.type();
		this.xp = petData.exp();
		this.heldItem = petData.item();
		this.skin = petData.skin();
		this.skinTexture = calculateSkinTexture();
		this.tier = petData.tier();
		this.level = info.level;
		this.perecentageToLevel = info.fill;
		this.levelXP = info.levelXP;
		this.nextLevelXP = info.nextLevelXP;
		this.icon = createIcon();
	}

	private String getName() {
		return name;
	}

	public long getXP() {
		return (long) xp;
	}

	public SkyblockItemRarity getRarity() {
		return tier;
	}

	public int getTier() {
		return tier.ordinal();
	}

	private Optional<ResolvableProfile> calculateSkinTexture() {
		if (this.skin.isPresent()) {
			ItemStack item = ItemRepository.getItemStack("PET_SKIN_" + this.skin.get());

			if (item == null || item.isEmpty()) return Optional.empty();

			ResolvableProfile profile = item.get(DataComponents.PROFILE);

			return profile != null ? Optional.of(profile) : Optional.empty();
		}
		return Optional.empty();
	}

	public int getLevel() { return level; }
	public ItemStack getIcon() { return icon; }


	private ItemStack createIcon() {
		if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return Ico.BARRIER;

		String targetItemId = this.getName() + ";" + (this.getTier() + (heldItem.isPresent() && heldItem.get().equals("PET_ITEM_TIER_BOOST") ? 1 : 0));
		NEUItem item = NEURepoManager.getItemByNeuId(targetItemId);

		// For cases life RIFT_FERRET Where it can be tier boosted into a pet that otherwise can't exist
		if (item == null && heldItem.isPresent() && heldItem.get().equals("PET_ITEM_TIER_BOOST")) {
			item = NEURepoManager.getItemByNeuId(getName() + ";" + getTier());
		}

		return item == null ? Ico.BARRIER : fromNEUItem(item, this.heldItem.map(ItemRepository::getItemStack).orElse(null));
	}

	/**
	 * Converts NEU item data into an ItemStack.
	 * <p> This method converts NEU item data into a Pet by using the placeholder
	 * information from NEU-REPO and injecting the player's calculated pet stats into the lore and transforming
	 * the NBT Data into modern DataComponentTypes before returning the final ItemStack.
	 *
	 * @param item The NEUItem representing the pet.
	 * @param heldItem The ItemStack of the pet's held item, if any.
	 * @return The ItemStack representing the pet with all its properties set.
	 */
	private ItemStack fromNEUItem(NEUItem item, ItemStack heldItem) {
		if (item == null) return getErrorStack();

		ItemStack petStack = ItemRepository.getItemStack(item.getSkyblockItemId());

		if (petStack == null || petStack.isEmpty()) return getErrorStack();

		// Copy to avoid mutating the original stack
		petStack = petStack.copy();

		List<Component> formattedLore = !(name.equals("GOLDEN_DRAGON") && level < 101) ?  processLore(item.getLore(), heldItem) : buildGoldenDragonEggLore(item.getLore());

		// Calculate and display XP for level
		Style style = Style.EMPTY.withItalic(false);
		if (level != 100 && level != 200) {
			String progress = "Progress to Level " + this.level + ": §e" + fixDecimals(this.perecentageToLevel * 100, true) + "%";
			formattedLore.add(formattedLore.size() - 1, Component.literal(progress).setStyle(style).withStyle(ChatFormatting.GRAY));
			String string = "§2§m ".repeat((int) Math.round(perecentageToLevel * 30)) + "§f§m ".repeat(30 - (int) Math.round(perecentageToLevel * 30));
			formattedLore.add(formattedLore.size() - 1, Component.literal(string + "§r§e " + numLetterFormat(levelXP) + "§6/§e" + numLetterFormat(nextLevelXP)).setStyle(style));
			formattedLore.add(formattedLore.size() - 1, Component.empty());
		} else {
			formattedLore.add(formattedLore.size() - 1, Component.literal("MAX LEVEL").setStyle(style).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
			formattedLore.add(formattedLore.size() - 1, Component.literal("▸ " + Formatters.INTEGER_NUMBERS.format((long) xp) + " XP").setStyle(style).withStyle(ChatFormatting.DARK_GRAY));
			formattedLore.add(formattedLore.size() - 1, Component.empty());
		}

		// Skin Head Texture
		if (skinTexture.isPresent() && skin.isPresent()) {
			NEUItem skinItem = NEURepoManager.getItemByNeuId("PET_SKIN_" + skin.get());
			if (skinItem != null) formattedLore.set(0, Component.nullToEmpty(formattedLore.getFirst().getString() + ", " + ChatFormatting.stripFormatting(skinItem.getDisplayName())));
			petStack.set(DataComponents.PROFILE, skinTexture.get());
		}

		if ((boosted())) formattedLore.set(formattedLore.size() - 1, Component.literal(getRarity().next().toString()).setStyle(style).withStyle(ChatFormatting.BOLD, getRarity().next().formatting));

		// Update the lore and name
		petStack.set(DataComponents.LORE, new ItemLore(formattedLore));
		String displayName = ChatFormatting.stripFormatting(item.getDisplayName()).replace("[Lvl {LVL}]", "§7[Lvl " + this.level + "]§r");
		petStack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName).setStyle(style).withStyle((boosted() ? getRarity().next() : getRarity()).formatting));
		return petStack;
	}

	/**
	 * Iterates through a Pet's lore injecting interpolated stat numbers based on pet level
	 *
	 * @param lore the raw lore data stored in NEU Repo
	 * @param heldItem the pet's held item, if any
	 * @return Formatted lore with injected stats inserted into the tooltip
	 */
	private List<Component> processLore(List<String> lore, ItemStack heldItem) {
		Map<String, Map<Rarity, PetNumbers>> petNums = NEURepoManager.getConstants().getPetNumbers();
		Rarity rarity = Rarity.values()[getTier()];
		PetNumbers data = petNums.get(getName()).get(rarity);
		List<Component> formattedLore = new ArrayList<>();

		for (String line : lore) {
			if (line.contains("Right-click to add this") || line.contains("pet menu!")) continue;

			String formattedLine = line;

			Matcher stats = statsMatcher.matcher(formattedLine);
			Matcher other = numberMatcher.matcher(formattedLine);

			while (stats.find()) {
				String placeholder = stats.group();
				String statKey = placeholder.substring(1, placeholder.length() - 1);
				String statValue = String.valueOf(fixDecimals(data.interpolatedStatsAtLevel(this.level).getStatNumbers().get(statKey), true));
				formattedLine = formattedLine.replace(placeholder, statValue);
			}

			while (other.find()) {
				String placeholder = other.group();
				int numberKey = Integer.parseInt(placeholder.substring(1, placeholder.length() - 1));
				String statValue = String.valueOf(fixDecimals(data.interpolatedStatsAtLevel(this.level).getOtherNumbers().get(numberKey), false));
				formattedLine = formattedLine.replace(placeholder, statValue);
			}

			formattedLore.add(Component.nullToEmpty(formattedLine));
		}


		if (heldItem != null) {
			formattedLore.set(formattedLore.size() - 2, Component.nullToEmpty("§r§6Held Item: " + heldItem.getHoverName().getString()));
			formattedLore.add(formattedLore.size() - 1, Component.empty());
		}

		return formattedLore;
	}

	/**
	 * NEU Repo doesn't distinguish between the Egg and the hatched GoldenDragon pet so hardcoded lore :eues:
	 * @param lore the existing lore
	 * @return Fully formatted GoldenDragonEgg Lore
	 */
	private List<Component> buildGoldenDragonEggLore(List<String> lore) {
		List<Component> formattedLore = new ArrayList<>();
		Style style = Style.EMPTY.withItalic(false);

		formattedLore.add(Component.nullToEmpty(lore.getFirst()));
		formattedLore.add(Component.empty());
		formattedLore.add(Component.literal("Perks:").setStyle(style).withStyle(ChatFormatting.GRAY));
		formattedLore.add(Component.literal("???").setStyle(style).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		formattedLore.add(Component.empty());
		formattedLore.add(Component.literal("Hatches at level §b100").setStyle(style).withStyle(ChatFormatting.GRAY));
		formattedLore.add(Component.empty());
		formattedLore.add(Component.nullToEmpty(lore.getLast()));

		return formattedLore;
	}

	private String fixDecimals(double num, boolean truncate) {
		if (num % 1 == 0) return String.valueOf((int) (num));
		BigDecimal roundedNum = new BigDecimal(num).setScale(truncate ? 1 : 3, RoundingMode.HALF_UP);
		return roundedNum.stripTrailingZeros().toPlainString();
	}

	private boolean boosted() {
		return this.heldItem.isPresent() && this.heldItem.get().equals("PET_ITEM_TIER_BOOST");
	}

	private ItemStack getErrorStack() {
		ItemStack errIcon = new ItemStack(Items.BARRIER);
		errIcon.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(this.getName()));
		return errIcon;
	}
}
