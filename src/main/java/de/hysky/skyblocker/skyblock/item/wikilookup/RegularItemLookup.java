package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class RegularItemLookup implements WikiLookup {
	private static final Pattern PET_ITEM_NAME = Pattern.compile("^\\[Lvl \\d+] (?<name>.+)$");
	public static final RegularItemLookup INSTANCE = new RegularItemLookup();

	private RegularItemLookup() {}

	@Override
	public void open(@NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial) {
		either.ifLeft(slot -> openWiki(slot.getStack(), player, useOfficial));
		either.ifRight(itemStack -> openWiki(itemStack, player, useOfficial));
	}

	private static void openWiki(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		PetInfo petInfo = ItemUtils.getPetInfo(itemStack);

		if (petInfo.isEmpty()) {
			ItemUtils.getItemIdOptional(itemStack)
					.map(neuId -> ItemRepository.getWikiLink(neuId, useOfficial))
					.ifPresentOrElse(wikiLink -> WikiLookup.openWikiLink(wikiLink, player),
							() -> player.sendMessage(Constants.PREFIX.get().append(useOfficial ?
									Text.translatable("skyblocker.wikiLookup.noArticleFound.official") :
									Text.translatable("skyblocker.wikiLookup.noArticleFound.fandom")), false));
		} else {
			String itemName = petInfo.name().get() + " Pet"; // Add Pet to the end of string for precise lookup
			Matcher matcher = PET_ITEM_NAME.matcher(itemName);

			if (matcher.matches()) {
				String petName = REPLACING_FUNCTION.apply(matcher.group("name").trim());
				String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + petName;
				WikiLookup.openWikiLink(wikiLink, player);
			}
		}
	}
}
