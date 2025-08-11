package de.hysky.skyblocker.skyblock.item.background.adders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ItemRarityBackground extends ColoredItemBackground<SkyblockItemRarity> {

	public static final ImmutableMap<String, SkyblockItemRarity> LORE_RARITIES = ImmutableMap.ofEntries(
			Map.entry("ADMIN", SkyblockItemRarity.ADMIN),
			Map.entry("ULTIMATE", SkyblockItemRarity.ULTIMATE),
			Map.entry("SPECIAL", SkyblockItemRarity.SPECIAL), //Very special is the same color so this will cover it
			Map.entry("DIVINE", SkyblockItemRarity.DIVINE),
			Map.entry("MYTHIC", SkyblockItemRarity.MYTHIC),
			Map.entry("LEGENDARY", SkyblockItemRarity.LEGENDARY),
			Map.entry("LEGENJERRY", SkyblockItemRarity.LEGENDARY),
			Map.entry("EPIC", SkyblockItemRarity.EPIC),
			Map.entry("RARE", SkyblockItemRarity.RARE),
			Map.entry("UNCOMMON", SkyblockItemRarity.UNCOMMON),
			Map.entry("COMMON", SkyblockItemRarity.COMMON)
	);

	private static final ImmutableList<Predicate<String>> INVENTORY_TITLES = ImmutableList.of(
			title -> title.contains("The Hex"),
			title -> title.equals("Craft Item"),
			title -> title.equals("Anvil"),
			title -> title.equals("Reforge Anvil")
	);

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds;
	}

	@Override
	protected SkyblockItemRarity getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<SkyblockItemRarity> cache) {
		if (stack == null || stack.isEmpty()) return null;

		int hashCode = stack.getUuid().isEmpty() ? System.identityHashCode(stack) : stack.getUuid().hashCode();
		if (cache.containsKey(hashCode)) return cache.get(hashCode);

		if (!stack.getSkyblockId().equals("PET")) {
			List<Text> lore = ItemUtils.getLore(stack);
			List<String> tooltip = lore.stream().map(Text::getString).toList();
			for (String key : LORE_RARITIES.keySet()) {
				if (tooltip.stream().anyMatch(line -> line.contains(key))) {
					SkyblockItemRarity rarity = LORE_RARITIES.get(key);
					cache.put(hashCode, rarity);
					return rarity;
				}
			}
		} else {
			PetInfo info = stack.getPetInfo();
			if (!info.isEmpty()) {
				SkyblockItemRarity rarity = info.item().isPresent() && info.item().get().equals("PET_ITEM_TIER_BOOST") ? info.rarity().next() : info.rarity();
				cache.put(hashCode, rarity);
				return rarity;
			}
		}

		cache.put(hashCode, null);
		return null;
	}

	@Override
	protected void draw(DrawContext context, int x, int y, SkyblockItemRarity rarity) {
		context.drawSpriteStretched(RenderLayer::getGuiTextured, getSprite(), x, y, 16, 16,
				ColorHelper.fromFloats(
						SkyblockerConfigManager.get().general.itemInfoDisplay.itemBackgroundOpacity,
						rarity.r, rarity.g, rarity.b
				)
		);
	}

	@Override
	protected void onScreenChange(String title, Screen screen) {
		if (Utils.isOnSkyblock() && INVENTORY_TITLES.stream().anyMatch(predicate -> predicate.test(title))) {
			ScreenEvents.remove(screen).register(s -> clearCache());
		}
	}
}
