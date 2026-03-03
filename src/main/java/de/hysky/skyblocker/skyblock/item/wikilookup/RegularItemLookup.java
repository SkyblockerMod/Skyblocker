package de.hysky.skyblocker.skyblock.item.wikilookup;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

public class RegularItemLookup implements WikiLookup {
	public static final RegularItemLookup INSTANCE = new RegularItemLookup();

	private RegularItemLookup() {}

	@Override
	public void open(ItemStack itemStack, Player player, boolean useOfficial) {
		String neuId = itemStack.getNeuName();

		if (StringUtils.isNotEmpty(neuId)) {
			String wikiLink = ItemRepository.getWikiLink(neuId, useOfficial);
			if (wikiLink != null) {
				WikiLookupManager.openWikiLink(wikiLink, player);
				return;
			}
		}
		noArticleFound(player, useOfficial);
	}

	private static void noArticleFound(Player player, boolean useOfficial) {
		player.displayClientMessage(Constants.PREFIX.get().append(useOfficial ?
				Component.translatable("skyblocker.wikiLookup.noArticleFound.official") :
				Component.translatable("skyblocker.wikiLookup.noArticleFound.fandom")), false);
	}
}
