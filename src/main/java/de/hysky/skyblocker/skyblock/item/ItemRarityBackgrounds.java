package de.hysky.skyblocker.skyblock.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ItemRarityBackgrounds {
	private static final GeneralConfig.ItemInfoDisplay CONFIG = SkyblockerConfigManager.get().general.itemInfoDisplay;
	private static final Supplier<Sprite> SPRITE = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(CONFIG.itemRarityBackgroundStyle.tex);
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
			Map.entry("COMMON", SkyblockItemRarity.COMMON));
	private static final Int2ReferenceOpenHashMap<SkyblockItemRarity> CACHE = new Int2ReferenceOpenHashMap<>();

	@Init
	public static void init() {
		//Clear the cache every 5 minutes, ints are very compact!
		Scheduler.INSTANCE.scheduleCyclic(CACHE::clear, 4800);

		//Clear cache after a screen where items can be upgraded in rarity closes
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			String title = screen.getTitle().getString();

			if (Utils.isOnSkyblock() && (title.contains("The Hex") || title.equals("Craft Item") || title.equals("Anvil") || title.equals("Reforge Anvil"))) {
				ScreenEvents.remove(screen).register(screen1 -> CACHE.clear());
			}
		});
	}

	public static void tryDraw(ItemStack stack, DrawContext context, int x, int y) {
		SkyblockItemRarity itemRarity = getItemRarity(stack);
		if (itemRarity != null)
			draw(context, x, y, itemRarity);
	}

	private static SkyblockItemRarity getItemRarity(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;

		String itemUuid = stack.getUuid();

		//If the item has an uuid, then use the hash code of the uuid otherwise use the identity hash code of the stack
		int hashCode = itemUuid.isEmpty() ? System.identityHashCode(stack) : itemUuid.hashCode();

		if (CACHE.containsKey(hashCode)) return CACHE.get(hashCode);

		//For regular items check the lore, for pets we use the rarity in the petInfo so that the rarity background work in the pets menu
		if (!stack.getSkyblockId().equals("PET")) {
			List<Text> lore = ItemUtils.getLore(stack);
			String[] stringifiedTooltip = lore.stream().map(Text::getString).toArray(String[]::new);

			for (String rarityString : LORE_RARITIES.keySet()) {
				if (Arrays.stream(stringifiedTooltip).anyMatch(line -> line.contains(rarityString))) {
					SkyblockItemRarity rarity = LORE_RARITIES.get(rarityString);

					CACHE.put(hashCode, rarity);
					return rarity;
				}
			}
		} else {
			PetInfo info = stack.getPetInfo();
			if (!info.isEmpty()) {
				SkyblockItemRarity rarity = info.rarity();
				CACHE.put(hashCode, rarity);
				return rarity;
			}
		}

		CACHE.put(hashCode, null);
		return null;
	}

	private static void draw(DrawContext context, int x, int y, SkyblockItemRarity rarity) {
		//Enable blending to handle HUD translucency
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		context.drawSpriteStretched(RenderLayer::getGuiTextured, SPRITE.get(), x, y, 16, 16, ColorHelper.fromFloats(SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgroundsOpacity, rarity.r, rarity.g, rarity.b));

		RenderSystem.disableBlend();
	}
}
