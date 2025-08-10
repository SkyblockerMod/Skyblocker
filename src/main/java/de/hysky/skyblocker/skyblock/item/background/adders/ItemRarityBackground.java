package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectReferencePair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.function.Predicate;

public class ItemRarityBackground extends ColoredItemBackground<SkyblockItemRarity> {
	public static final List<ObjectReferencePair<String, SkyblockItemRarity>> LORE_RARITIES = List.of(
			ObjectReferencePair.of("ADMIN", SkyblockItemRarity.ADMIN),
			ObjectReferencePair.of("ULTIMATE", SkyblockItemRarity.ULTIMATE),
			ObjectReferencePair.of("SPECIAL", SkyblockItemRarity.SPECIAL), //Very special is the same color so this will cover it
			ObjectReferencePair.of("DIVINE", SkyblockItemRarity.DIVINE),
			ObjectReferencePair.of("MYTHIC", SkyblockItemRarity.MYTHIC),
			ObjectReferencePair.of("LEGENDARY", SkyblockItemRarity.LEGENDARY),
			ObjectReferencePair.of("LEGENJERRY", SkyblockItemRarity.LEGENDARY),
			ObjectReferencePair.of("EPIC", SkyblockItemRarity.EPIC),
			ObjectReferencePair.of("RARE", SkyblockItemRarity.RARE),
			ObjectReferencePair.of("UNCOMMON", SkyblockItemRarity.UNCOMMON),
			ObjectReferencePair.of("COMMON", SkyblockItemRarity.COMMON)
	);

	private static final List<Predicate<String>> INVENTORY_TITLES = List.of(
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
			for (ObjectReferencePair<String, SkyblockItemRarity> key : LORE_RARITIES) {
				if (tooltip.stream().anyMatch(line -> line.contains(key.left()))) {
					SkyblockItemRarity rarity = key.right();
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
		context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, getSprite(), x, y, 16, 16,
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
