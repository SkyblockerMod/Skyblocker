package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;

public class RegularItemLookup implements WikiLookup {
	private static final Pattern PET_ITEM_NAME = Pattern.compile("^\\[Lvl \\d+] (?<name>.+)$");
	private static final Function<String, Matcher> PET_MATCHER = itemName -> PET_ITEM_NAME.matcher(itemName + " Pet"); // // Add Pet to the end of string for precise lookup
	public static final RegularItemLookup INSTANCE = new RegularItemLookup();

	private RegularItemLookup() {}

	@Override
	public void open(@NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial) {
		either.ifLeft(slot -> openWiki(slot.getStack(), player, useOfficial));
		either.ifRight(itemStack -> openWiki(itemStack, player, useOfficial));
	}

	private static void openWiki(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		String itemName = itemStack.getName().getString();
		PetInfo petInfo = ItemUtils.getPetInfo(itemStack);

		// Regular item
		if (petInfo.isEmpty()) {
			NbtCompound nbt = ItemUtils.getCustomData(itemStack);

			ItemUtils.getItemIdOptional(itemStack)
					.map(neuId -> ItemRepository.getWikiLink(neuId, useOfficial))
					.ifPresentOrElse(wikiLink -> WikiLookup.openWikiLink(wikiLink, player),
							() -> {
								// For an item name that start with [Lvl 100] PET_NAME but doesn't have PetInfo stored
								if (itemName.matches(PET_ITEM_NAME.pattern())) {
									lookupPetItem(PET_MATCHER.apply(itemName), player, useOfficial);
								}
								// For enchanted book with a single enchantment
								else if (itemStack.isOf(Items.ENCHANTED_BOOK) && nbt.contains("enchantments")) {
									NbtCompound enchantments = nbt.getCompoundOrEmpty("enchantments");

									if (enchantments.getKeys().size() == 1) {
										String firstEnchantment = Iterables.getFirst(enchantments.getKeys(), null)
												.replace("ultimate_", "") // Stripped out ultimate prefix
												.replace("_", " ").trim();

										String enchantment = REPLACING_FUNCTION.apply(WordUtils.capitalizeFully(firstEnchantment + " enchantment"));
										String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + enchantment;
										WikiLookup.openWikiLink(wikiLink, player);
									}
								}
								// Otherwise, no article found
								else {
									noArticleFound(player, useOfficial);
								}
							});
		}
		// Pet item
		else {
			lookupPetItem(PET_MATCHER.apply(petInfo.name().get()), player, useOfficial);
		}
	}

	private static void lookupPetItem(Matcher matcher, @NotNull PlayerEntity player, boolean useOfficial) {
		if (matcher.matches()) {
			String petName = REPLACING_FUNCTION.apply(matcher.group("name").trim());
			String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + petName;
			WikiLookup.openWikiLink(wikiLink, player);
		}
	}

	private static void noArticleFound(@NotNull PlayerEntity player, boolean useOfficial) {
		player.sendMessage(Constants.PREFIX.get().append(useOfficial ?
				Text.translatable("skyblocker.wikiLookup.noArticleFound.official") :
				Text.translatable("skyblocker.wikiLookup.noArticleFound.fandom")), false);
	}
}
