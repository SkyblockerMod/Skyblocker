package me.xmrvizzy.skyblocker.skyblock.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemRarityBackgrounds {
	private static final Identifier RARITY_BG_TEX = new Identifier(SkyblockerMod.NAMESPACE, "item_rarity_background");
	private static final Supplier<Sprite> SPRITE = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(RARITY_BG_TEX);
	private static final ImmutableMap<String, SkyblockItemRarity> LORE_RARITIES = ImmutableMap.ofEntries(
			Map.entry("ADMIN", SkyblockItemRarity.ADMIN),
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
	
	public static void init() {
		//Clear the cache every 5 minutes, ints are very compact!
		Scheduler.INSTANCE.scheduleCyclic(() -> CACHE.clear(), 4800);
	}
	
	public static void tryDraw(ItemStack stack, DrawContext context, int x, int y) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		if (client.player != null) {
			SkyblockItemRarity itemRarity = getItemRarity(stack, client.player);
			
			if (itemRarity != null) draw(context, x, y, itemRarity);
		}
	}
	
	private static SkyblockItemRarity getItemRarity(ItemStack stack, ClientPlayerEntity player) {
		if (stack == null || stack.isEmpty()) return null;
		
		int hashCode = System.identityHashCode(stack);
		if (CACHE.containsKey(hashCode)) return CACHE.get(hashCode);
				
		List<Text> tooltip = stack.getTooltip(player, TooltipContext.BASIC);
		String[] stringifiedTooltip = tooltip.stream().map(Text::getString).toArray(String[]::new);
		
		for (String rarity : LORE_RARITIES.keySet()) {
			if (Arrays.stream(stringifiedTooltip).anyMatch(line -> line.contains(rarity))) {
				SkyblockItemRarity foundRarity = LORE_RARITIES.get(rarity);
				
				CACHE.put(hashCode, foundRarity);
				return LORE_RARITIES.get(rarity);
			}
		}
		
		CACHE.put(hashCode, null);
		return null;
	}
	
	private static void draw(DrawContext context, int x, int y, SkyblockItemRarity rarity) {
		//Enable blending to handle HUD translucency
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		context.drawSprite(x, y, 0, 16, 16, SPRITE.get(), rarity.r, rarity.g, rarity.b, 1f);
		
		RenderSystem.disableBlend();
	}
}
