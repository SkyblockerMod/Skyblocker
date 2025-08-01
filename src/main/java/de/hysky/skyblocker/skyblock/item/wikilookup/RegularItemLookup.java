package de.hysky.skyblocker.skyblock.item.wikilookup;

import org.jetbrains.annotations.NotNull;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class RegularItemLookup implements WikiLookup {
	public static final RegularItemLookup INSTANCE = new RegularItemLookup();

	private RegularItemLookup() {}

	@Override
	public void open(@NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial) {
		either.ifLeft(slot -> openWiki(slot.getStack(), player, useOfficial));
		either.ifRight(itemStack -> openWiki(itemStack, player, useOfficial));
	}

	private static void openWiki(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		ItemUtils.getItemIdOptional(itemStack)
				.map(neuId -> ItemRepository.getWikiLink(neuId, useOfficial))
				.ifPresentOrElse(wikiLink -> WikiLookup.openWikiLink(wikiLink, player),
						() -> player.sendMessage(Constants.PREFIX.get().append(useOfficial ?
								Text.translatable("skyblocker.wikiLookup.noArticleFound.official") :
								Text.translatable("skyblocker.wikiLookup.noArticleFound.fandom")), false));
	}
}
