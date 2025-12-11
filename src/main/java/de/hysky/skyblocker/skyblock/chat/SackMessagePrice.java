package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.RegexUtils;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.HoverEvent.ShowText;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds npc/bazaar prices to the sack messages sent by the server when items are added or removed from a sack.
 */
public class SackMessagePrice {
	private static final Pattern ITEM_COUNT_PATTERN = Pattern.compile("([-+][\\d,]+)");
	private static final Logger LOGGER = LoggerFactory.getLogger(SackMessagePrice.class);

	@Init
	public static void init() {
		ClientReceiveMessageEvents.MODIFY_GAME.register(SackMessagePrice::onMessage);
	}

	// This can probably be split into a few methods, but it'll require so much argument passing that it's going to be a mess anyhow.
	private static Text onMessage(Text original, boolean overlay) {
		if (overlay) return original;

		String string = original.getString();
		if (!string.startsWith("[Sacks] ")) return original;

		MutableText copy = deepCopy(original); // We need to copy the original since it's completely immutable when constructed from a packet.

		ObjectArrayList<List<Text>> listList = getHoverEventSiblings(copy); // We use the copied one here so that any changes to the lists do not mutate the original
		if (listList.isEmpty()) return original;

		for (List<Text> textList : listList) {
			Object2IntMap<String> items = parseItems(textList);
			if (items.isEmpty()) {
				LOGGER.warn("No items found in sack message: `{}`", original.getString());
				return original; // If we couldn't parse any items, we return the original text
			}
			double npcPrice = 0;
			double bazaarBuyPrice = 0;
			double bazaarSellPrice = 0;
			for (var entry : items.object2IntEntrySet()) {
				String itemName = entry.getKey();
				int count = entry.getIntValue();

				String neuId = getNeuId(itemName);
				if (neuId == null) {
					LOGGER.warn("Failed to find NEU ID for item: `{}`. This item will not be priced.", itemName);
					continue; // If we couldn't find the item ID, we skip this item
				}

				Object2DoubleMap<String> npcData = TooltipInfoType.NPC.getData();
				if (npcData != null) npcPrice += npcData.getOrDefault(neuId, 0) * count;
				else LOGGER.warn("No NPC data found for item: `{}`", neuId);

				Object2ObjectMap<String, BazaarProduct> bazaarData = TooltipInfoType.BAZAAR.getData();
				if (bazaarData != null) {
					BazaarProduct itemData = bazaarData.get(neuId);
					if (itemData != null) {
						OptionalDouble buyPrice = itemData.buyPrice();
						if (buyPrice.isPresent()) bazaarBuyPrice += buyPrice.getAsDouble() * count;
						OptionalDouble sellPrice = itemData.sellPrice();
						if (sellPrice.isPresent()) bazaarSellPrice += sellPrice.getAsDouble() * count;
					} else {
						LOGGER.warn("No item data found for item `{}` in bazaar price data.", neuId);
					}
				}
			}
			textList.add(ScreenTexts.LINE_BREAK);
			textList.add(LineSmoothener.createSmoothLine());
			textList.add(ScreenTexts.LINE_BREAK);

			textList.add(Text.empty()
							.append(Text.literal("NPC Sell Price: ").formatted(Formatting.YELLOW))
							.append(npcPrice > 0
									? ItemTooltip.getCoinsMessage(npcPrice, 1)
									: Text.literal("No data").formatted(Formatting.RED)));
			textList.add(ScreenTexts.LINE_BREAK);
			textList.add(Text.empty()
							.append(Text.literal("Bazaar Buy Price: ").formatted(Formatting.GOLD))
							.append(bazaarBuyPrice > 0
									? ItemTooltip.getCoinsMessage(bazaarBuyPrice, 1)
									: Text.literal("No data").formatted(Formatting.RED)));
			textList.add(ScreenTexts.LINE_BREAK);
			textList.add(Text.empty()
							.append(Text.literal("Bazaar Sell Price: ").formatted(Formatting.GOLD))
							.append(bazaarSellPrice > 0
									? ItemTooltip.getCoinsMessage(bazaarSellPrice, 1)
									: Text.literal("No data").formatted(Formatting.RED)));
		}

		return copy;
	}

	/**
	 * Recursively creates a deep copy of a {@link Text} object.
	 *
	 * @param text The text to copy
	 * @return A deep copy of the text, with the same content and style, but no references to the original text
	 * @implNote Technically, this is not a deep <i>deep</i> copy, as it does not clone the underlying objects in the style.
	 * 		However, there's a special case for hover events, which are cloned to ensure that the hover text is also a deep copy, and that's enough for our use case.
	 * 		If you need a true deep copy, do not copy this directly and expect it to work.
	 */
	private static MutableText deepCopy(Text text) {
		MutableText copy = text.copyContentOnly();

		if (text.getStyle().getHoverEvent() instanceof ShowText(Text showText)) {
			// DO NOT simplify to `text.getStyle().withHoverEvent(new ShowText(showText);`,
			// Style.withHoverEvent(hoverEvent) checks if the given value is equal to the current one, and since our text clone is equal by value, it will not create a new style.
			// This means the original hover event will still be used which is immutable, and our list modifications will fail with an UnsupportedOperationException in #onMessage.
			copy.setStyle(Style.EMPTY.withHoverEvent(new ShowText(deepCopy(showText))).withParent(text.getStyle()));
		} else copy.setStyle(text.getStyle());

		for (Text sibling : text.getSiblings()) {
			copy.append(deepCopy(sibling));
		}

		return copy;
	}

	private static @Nullable String getNeuId(String itemName) {
		return NEURepoManager.getItemByName(itemName)
				.stream()
				.findFirst()
				.map(NEUItem::getSkyblockItemId)
				.orElseGet(() -> {
					LOGGER.warn("Failed to find the NEU item ID for item: {}", itemName);
					return null; // This won't be used to calculate the price
				});
	}

	private static ObjectArrayList<List<Text>> getHoverEventSiblings(Text text) {
		ObjectArrayList<List<Text>> listList = new ObjectArrayList<>();
		for (Text sibling : text.getSiblings()) {
			if (sibling.getStyle().getHoverEvent() instanceof ShowText(Text hoverText)
					&& hoverText.getContent() instanceof Literal(String rootContent) // Only match the root content since we only need the root content.
					&& Strings.CS.startsWithAny(rootContent, "Added items:", "Removed items:")) {
				listList.add(hoverText.getSiblings());
			}
		}
		return listList;
	}

	/**
	 * Parses the items and their counts from the siblings of the hover text.
	 *
	 * @return A map of item names to their counts.
	 */
	@SuppressWarnings("ConstantValue") // It's much easier to read this way.
	private static Object2IntArrayMap<String> parseItems(List<Text> texts) {
		/*
			The hover message's structure is as follows:
			- Added items:
			- item count
			- item name
			- sack name
			(for any other item, repeat the above three lines with a newline in between)
			- \n\n
			- `This message can be toggled off in the settings` warning

			We only care about the groups of three lines that make up each item entry.
		 */
		Object2IntArrayMap<String> items = new Object2IntArrayMap<>();
		Integer lastCount = null;
		String lastItemName = null;
		for (Text text : texts) {
			if (text.getContent() instanceof Literal(String content)) { // Only match the root content since we only need the root content.
				if (content.equals("\n\n")) break; // End of items list, we can stop parsing here - NOTE: This has to come before the isBlank check, otherwise it will be skipped.
				if (content.isBlank()) continue; // This includes \n lines, which we don't want to try and parse as item names or counts

				String trimmed = content.trim();
				if (lastCount == null && lastItemName == null) { // Initial state, item count comes first
					Matcher matcher = ITEM_COUNT_PATTERN.matcher(trimmed);
					OptionalInt count = RegexUtils.findIntFromMatcher(matcher);
					if (count.isEmpty()) {
						LOGGER.error("Failed to parse item count from text content: `{}`", trimmed);
						return new Object2IntArrayMap<>(); // Something went wrong, so we panic and not modify the text.
					}
					lastCount = count.getAsInt();
				} else if (lastCount != null && lastItemName == null) { // The item name comes next
					lastItemName = trimmed;
				} else if (lastCount != null && lastItemName != null) { // Then comes the sack name, which we can ignore but this iteration can still be used for finalizing the cycle
					items.put(lastItemName, lastCount.intValue());
					lastCount = null;
					lastItemName = null;
				}
			}
		}
		return items;
	}
}
