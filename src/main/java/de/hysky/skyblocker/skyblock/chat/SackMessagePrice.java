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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
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
	private static Component onMessage(Component original, boolean overlay) {
		if (overlay) return original;

		String string = original.getString();
		if (!string.startsWith("[Sacks] ")) return original;

		MutableComponent copy = deepCopy(original); // We need to copy the original since it's completely immutable when constructed from a packet.

		ObjectArrayList<List<Component>> listList = getHoverEventSiblings(copy); // We use the copied one here so that any changes to the lists do not mutate the original
		if (listList.isEmpty()) return original;

		for (List<Component> textList : listList) {
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
			textList.add(CommonComponents.NEW_LINE);
			textList.add(LineSmoothener.createSmoothLine());
			textList.add(CommonComponents.NEW_LINE);

			textList.add(Component.empty()
							.append(Component.literal("NPC Sell Price: ").withStyle(ChatFormatting.YELLOW))
							.append(npcPrice > 0
									? ItemTooltip.getCoinsMessage(npcPrice, 1)
									: Component.literal("No data").withStyle(ChatFormatting.RED)));
			textList.add(CommonComponents.NEW_LINE);
			textList.add(Component.empty()
							.append(Component.literal("Bazaar Buy Price: ").withStyle(ChatFormatting.GOLD))
							.append(bazaarBuyPrice > 0
									? ItemTooltip.getCoinsMessage(bazaarBuyPrice, 1)
									: Component.literal("No data").withStyle(ChatFormatting.RED)));
			textList.add(CommonComponents.NEW_LINE);
			textList.add(Component.empty()
							.append(Component.literal("Bazaar Sell Price: ").withStyle(ChatFormatting.GOLD))
							.append(bazaarSellPrice > 0
									? ItemTooltip.getCoinsMessage(bazaarSellPrice, 1)
									: Component.literal("No data").withStyle(ChatFormatting.RED)));
		}

		return copy;
	}

	/**
	 * Recursively creates a deep copy of a {@link Component} object.
	 *
	 * @param text The text to copy
	 * @return A deep copy of the text, with the same content and style, but no references to the original text
	 * @implNote Technically, this is not a deep <i>deep</i> copy, as it does not clone the underlying objects in the style.
	 * 		However, there's a special case for hover events, which are cloned to ensure that the hover text is also a deep copy, and that's enough for our use case.
	 * 		If you need a true deep copy, do not copy this directly and expect it to work.
	 */
	private static MutableComponent deepCopy(Component text) {
		MutableComponent copy = text.plainCopy();

		if (text.getStyle().getHoverEvent() instanceof ShowText(Component showText)) {
			// DO NOT simplify to `text.getStyle().withHoverEvent(new ShowText(showText);`,
			// Style.withHoverEvent(hoverEvent) checks if the given value is equal to the current one, and since our text clone is equal by value, it will not create a new style.
			// This means the original hover event will still be used which is immutable, and our list modifications will fail with an UnsupportedOperationException in #onMessage.
			copy.setStyle(Style.EMPTY.withHoverEvent(new ShowText(deepCopy(showText))).applyTo(text.getStyle()));
		} else copy.setStyle(text.getStyle());

		for (Component sibling : text.getSiblings()) {
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

	private static ObjectArrayList<List<Component>> getHoverEventSiblings(Component text) {
		ObjectArrayList<List<Component>> listList = new ObjectArrayList<>();
		for (Component sibling : text.getSiblings()) {
			if (sibling.getStyle().getHoverEvent() instanceof ShowText(Component hoverText)
					&& hoverText.getContents() instanceof LiteralContents(String rootContent) // Only match the root content since we only need the root content.
					&& StringUtils.startsWithAny(rootContent, "Added items:", "Removed items:")) {
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
	private static Object2IntArrayMap<String> parseItems(List<Component> texts) {
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
		for (Component text : texts) {
			if (text.getContents() instanceof LiteralContents(String content)) { // Only match the root content since we only need the root content.
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
