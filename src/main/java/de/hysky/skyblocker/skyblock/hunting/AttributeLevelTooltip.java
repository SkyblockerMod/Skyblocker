package de.hysky.skyblocker.skyblock.hunting;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.container.ContainerUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeLevelTooltip extends SimpleTooltipAdder {
	private static final String ATTRIBUTE_MENU = "Attribute Menu";
	private static final String HUNTING_BOX = "Hunting Box";

	static final Pattern SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-Za-z ]+?) Shard \\((?<id>[CUREL]\\d+)\\)");
	private static final Pattern RARITY_PATTERN = Pattern.compile("Rarity: (COMMON|UNCOMMON|RARE|EPIC|LEGENDARY)");
	private static final Pattern LEVEL_PATTERN = Pattern.compile("Level: (\\d+)");
	private static final Pattern RARITY_AND_ID_PATTERN = Pattern.compile("(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY).*?SHARD \\(ID ([CUREL]\\d+)\\)");
	private static final Pattern NAME_AND_LEVEL_PATTERN = Pattern.compile(".*? ([IVX]+) \\(.*\\)");

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");

	public AttributeLevelTooltip(int priority) {
		super("(" + ATTRIBUTE_MENU + "|" + HUNTING_BOX + ")", priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (focusedSlot == null || ContainerUtil.isEdgeSlot(focusedSlot.id, 6) || lines.isEmpty()) return;
		String title = groups[0];
		switch (title) {
			case ATTRIBUTE_MENU -> handleAttributeMenu(lines);
			case HUNTING_BOX -> handleHuntingBox(lines);
			case null, default -> {}
		}
	}

	private static void handleAttributeMenu(List<Text> lines) {
		String id = null; // Always exists
		String rarity = null; // Always exists
		String levelStr = null; // Decimal, doesn't exist if maxed
		String syphonCountStr = null; // Decimal, doesn't exist if maxed
		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");
		for (Text line : lines) {
			String lineText = line.getString();
			if (lineText.isEmpty()) continue;

			matcher.reset(lineText);

			if (id == null && matcher.usePattern(SOURCE_PATTERN).matches()) {
				id = matcher.group("id");
			} else if (rarity == null && matcher.usePattern(RARITY_PATTERN).matches()) {
				rarity = matcher.group(1);
			} else if (levelStr == null && matcher.usePattern(LEVEL_PATTERN).matches()) {
				levelStr = matcher.group(1);
			} else if (matcher.usePattern(HuntingBoxHelper.SYPHON_PATTERN).matches()) {
				syphonCountStr = matcher.group(1);
				break; // This is the last line.
			}
		}
		if (id == null || rarity == null || levelStr == null || syphonCountStr == null) return;
		int level = NumberUtils.toInt(levelStr, -1);
		if (level < 0) return;
		int syphonCount = NumberUtils.toInt(syphonCountStr, -1);
		if (syphonCount < 0) return;

		SkyblockItemRarity itemRarity = SkyblockItemRarity.valueOf(rarity.toUpperCase(Locale.ROOT)); // Should already be uppercase, but just in case
		int shardsUntilMax = AttributeLevel.getShardsUntilMax(itemRarity, level + 1); // +1 because we already know how many for the next level
		if (shardsUntilMax < 0) return;
		int required = shardsUntilMax + syphonCount;
		addRequiredShardCount(lines, required, 0); // Owned count isn't available in the attribute menu, so we assume 0

		Attribute attribute = Attributes.getAttributeFromId(id);
		if (attribute == null) return;
		addBuyPriceOfRequired(lines, required, attribute.apiId());
	}

	private static void handleHuntingBox(List<Text> lines) {
		String levelStr = null; // Roman, always exists
		String ownedStr = null; // Decimal, always exists
		String syphonCountStr = null; // Decimal, doesn't exist if maxed
		String rarity = null; // Always exists
		String id = null; // Always exists

		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");
		for (Text line : lines) {
			String lineText = line.getString();
			if (lineText.isEmpty()) continue;

			matcher.reset(lineText);
			if (levelStr == null && matcher.usePattern(NAME_AND_LEVEL_PATTERN).matches()) {
				levelStr = matcher.group(1);
			} else if (ownedStr == null && matcher.usePattern(HuntingBoxHelper.OWNED_PATTERN).matches()) {
				ownedStr = matcher.group(1).replace(",", "");
			} else if (syphonCountStr == null && matcher.usePattern(HuntingBoxHelper.SYPHON_PATTERN).matches()) {
				syphonCountStr = matcher.group(1);
			} else if (matcher.usePattern(RARITY_AND_ID_PATTERN).matches()) {
				rarity = matcher.group(1);
				id = matcher.group(2);
				break;
			}
		}
		if (levelStr == null || ownedStr == null || rarity == null || id == null) return;

		int level = RomanNumerals.romanToDecimal(levelStr);
		if (level < 0 || level > AttributeLevel.MAX_LEVEL) return;

		int owned = NumberUtils.toInt(ownedStr, -1);
		if (owned < 0) return;

		SkyblockItemRarity itemRarity = SkyblockItemRarity.valueOf(rarity.toUpperCase(Locale.ROOT)); // Should already be uppercase, but just in case
		int shardsUntilMax = AttributeLevel.getShardsUntilMax(itemRarity, level + 1); // +1 because we already know how many for the next level
		if (shardsUntilMax < 0 || syphonCountStr == null) return;

		int syphonCount = NumberUtils.toInt(syphonCountStr, -1);
		if (syphonCount < 0) return;

		int required = shardsUntilMax + syphonCount;
		addRequiredShardCount(lines, required, owned);
		Attribute attribute = Attributes.getAttributeFromId(id);
		if (attribute == null) return;
		addBuyPriceOfRequired(lines, required - owned, attribute.apiId());
	}

	private static void addBuyPriceOfRequired(List<Text> lines, int required, String apiId) {
		Object2ObjectMap<String, BazaarProduct> data = TooltipInfoType.BAZAAR.getData();
		if (data == null) return;

		BazaarProduct product = data.get(apiId);

		Text costText;
		if (product == null || product.sellPrice().isEmpty()) {
			costText = Text.literal("No data").formatted(Formatting.RED);
		} else {
			double price = product.sellPrice().getAsDouble() * required;
			boolean holdingShift = Screen.hasShiftDown();
			costText = ItemTooltip.getCoinsMessage(price * required, holdingShift ? required : 1, true);
		}

		lines.add(Text.empty()
					  .append(Text.literal("Bazaar Cost To Max: ").formatted(Formatting.LIGHT_PURPLE))
					  .append(costText));
	}

	private static void addRequiredShardCount(List<Text> lines, int required, int owned) {
		lines.add(Text.empty()
					  .append(Text.literal("Shards Until Maxed: ").formatted(Formatting.LIGHT_PURPLE))
					  .append(Text.literal(String.valueOf(required)).formatted(Formatting.GOLD))
					  .append(owned > required ? Text.literal(" (Can afford)").formatted(Formatting.GREEN)
											   : Text.literal(" (" + (required - owned) + " more)").formatted(Formatting.GRAY)));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
