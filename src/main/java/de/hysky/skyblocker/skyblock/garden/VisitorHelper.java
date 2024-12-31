package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;

//TODO: check inventory items, sum all repeated items into one (should work)
//TODO: Get visitors "rarity" and apply it to their name in the helper list
public class VisitorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Visitor Helper");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    // The pair contains the name of the visitor and the texture if the icon is a player head
    private static final Map<Pair<String, String>, Object2IntMap<String>> itemMap = new HashMap<>();
    private static final Map<String, ItemStack> itemCache = new HashMap<>();
    private static final int TEXT_START_X = 4;
    private static final int TEXT_START_Y = 4;
    private static final int ENTRY_INDENT = 8;
    private static final int ITEM_INDENT = 20;
    private static final int LINE_SPACING = 3;

    private static boolean shouldProcessVisitorItems = true;

	@Init
	public static void init() {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

			String title = screen.getTitle().getString();
			boolean isGardenLocation = Utils.getLocationRaw().equals("garden");

			if (SkyblockerConfigManager.get().farming.garden.visitorHelper &&
					(!SkyblockerConfigManager.get().farming.garden.visitorHelperGardenOnly || isGardenLocation) &&
					screen instanceof HandledScreen<?> handledScreen &&
					(!title.contains("Logbook") || title.startsWith("Bazaar"))) {

				ScreenEvents.afterRender(screen).register((screen_, context, mouseX, mouseY, delta) ->
						renderScreen(title, context, client.textRenderer, handledScreen.getScreenHandler(), mouseX, mouseY));

				ScreenEvents.remove(screen).register(screen_ -> shouldProcessVisitorItems = true);
			}
		});
	}

    public static void renderScreen(String title, DrawContext context, TextRenderer textRenderer, ScreenHandler handler, int mouseX, int mouseY) {
        if (handler.getCursorStack() == ItemStack.EMPTY && shouldProcessVisitorItems) processVisitorItem(title, handler);
        drawScreenItems(context, textRenderer, mouseX, mouseY);
    }

	// The location of copy amount, and item text seem to be overlapping so clicking on part of the itemText run copy amount.
	public static void onMouseClicked(double mouseX, double mouseY, int mouseButton, TextRenderer textRenderer) {

		int yPosition = TEXT_START_Y;
		boolean showStacks = SkyblockerConfigManager.get().farming.garden.showStacksInVisitorHelper;

		// Group visitors and items like in drawScreenItems
		Map<String, List<String>> itemToVisitorsMap = new LinkedHashMap<>();
		Map<String, Integer> itemToTotalAmountMap = new LinkedHashMap<>();

		for (Map.Entry<Pair<String, String>, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
			Pair<String, String> visitorName = visitorEntry.getKey();
			Object2IntMap<String> visitorItems = visitorEntry.getValue();

			for (Object2IntMap.Entry<String> itemEntry : visitorItems.object2IntEntrySet()) {
				String itemName = itemEntry.getKey();
				int amount = itemEntry.getIntValue();

				itemToVisitorsMap.computeIfAbsent(itemName, k -> new ArrayList<>()).add(visitorName.left());
				itemToTotalAmountMap.put(itemName, itemToTotalAmountMap.getOrDefault(itemName, 0) + amount);
			}
		}

		Set<String> processedVisitors = new HashSet<>();
		for (Map.Entry<String, List<String>> groupedEntry : itemToVisitorsMap.entrySet()) {
			String itemName = groupedEntry.getKey();
			List<String> visitors = groupedEntry.getValue();
			int totalAmount = itemToTotalAmountMap.get(itemName);

			// Check grouped visitor names
			for (String visitor : visitors) {
				if (!processedVisitors.contains(visitor)) {
					yPosition += LINE_SPACING + textRenderer.fontHeight;
					processedVisitors.add(visitor);
				}
			}

			// Adjust itemText and copy amount positions for stack-based display
			String amountText = showStacks && totalAmount >= 64
					? (totalAmount / 64) + " stacks" + (totalAmount % 64 > 0 ? " + " + (totalAmount % 64) : "")
					: "" + totalAmount;

			String combinedText = itemName + " x" + amountText;
			int itemTextWidth = textRenderer.getWidth(combinedText);
			int copyAmountX = TEXT_START_X + ENTRY_INDENT + itemTextWidth;
			int copyAmountWidth = textRenderer.getWidth(" [Copy Amount]");

			if (isMouseOverText(mouseX, mouseY, TEXT_START_X + ENTRY_INDENT, yPosition, itemTextWidth, textRenderer.fontHeight)) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName, true);
				return;
			}
			if (isMouseOverText(mouseX, mouseY, copyAmountX, yPosition, copyAmountWidth, textRenderer.fontHeight)) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (client.player != null) {
					client.keyboard.setClipboard(String.valueOf(totalAmount));
					client.player.sendMessage(Constants.PREFIX.get().append("Copied amount successfully"), false);
				}
				return;
			}

			yPosition += LINE_SPACING + textRenderer.fontHeight;
		}

		// Check remaining visitors with unshared items
		for (Map.Entry<Pair<String, String>, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
			Pair<String, String> visitorName = visitorEntry.getKey();
			if (processedVisitors.contains(visitorName.left())) continue;

			yPosition += LINE_SPACING + textRenderer.fontHeight;

			for (Object2IntMap.Entry<String> itemEntry : visitorEntry.getValue().object2IntEntrySet()) {
				String itemName = itemEntry.getKey();
				int amount = itemEntry.getIntValue();

				String amountText = showStacks && amount >= 64
						? (amount / 64) + " stacks" + (amount % 64 > 0 ? " + " + (amount % 64) : "")
						: "" + amount;

				String combinedText = itemName + " x" + amountText;
				int itemTextX = TEXT_START_X + ENTRY_INDENT;
				int itemTextWidth = textRenderer.getWidth(combinedText);
				int copyAmountX = itemTextX + itemTextWidth;
				int copyAmountWidth = textRenderer.getWidth(" [Copy Amount]");

				if (isMouseOverText(mouseX, mouseY, itemTextX, yPosition, itemTextWidth, textRenderer.fontHeight)) {
					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName, true);
					return;
				}

				if (isMouseOverText(mouseX, mouseY, copyAmountX, yPosition, copyAmountWidth, textRenderer.fontHeight)) {
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player != null) {
						client.keyboard.setClipboard(amountText);
						client.player.sendMessage(Constants.PREFIX.get().append("Copied amount successfully"), false);
					}
					return;
				}
				yPosition += LINE_SPACING + textRenderer.fontHeight;
			}
		}
	}

	public static void onSlotClick(Slot slot, int slotId, String title, ItemStack visitorHeadStack) {
        if ((slotId == 29 || slotId == 13 || slotId == 33) && slot.hasStack() && ItemUtils.getLoreLineIf(slot.getStack(), s -> s.equals("Click to give!") || s.equals("Click to refuse!")) != null) {
            itemMap.remove(new ObjectObjectImmutablePair<>(title, getTextureOrNull(visitorHeadStack)));
            shouldProcessVisitorItems = false;
        }
    }

    private static void processVisitorItem(String visitorName, ScreenHandler handler) {
        ItemStack visitorItem = handler.getSlot(13).getStack();
        if (visitorItem == null || !visitorItem.contains(DataComponentTypes.LORE) || ItemUtils.getLoreLineIf(visitorItem, t -> t.contains("Times Visited")) == null) return;
        ItemStack acceptButton = handler.getSlot(29).getStack();
        if (acceptButton == null) return;
        processLore(visitorName, getTextureOrNull(visitorItem), ItemUtils.getLore(acceptButton));
    }

    private static @Nullable String getTextureOrNull(ItemStack stack) {
        String texture = ItemUtils.getHeadTexture(stack);

        return texture.isEmpty() ? null : texture;
    }

    private static void processLore(String visitorName, @Nullable String visitorTexture, List<Text> loreList) {
        boolean saveRequiredItems = false;
        for (Text text : loreList) {
            String lore = text.getString();
            if (lore.contains("Items Required"))
                saveRequiredItems = true;
            else if (lore.contains("Rewards"))
                break;
            else if (saveRequiredItems)
                updateItemMap(visitorName, visitorTexture, text);
        }
    }

	private static void updateItemMap(String visitorName, @Nullable String visitorTexture, Text lore) {
		String[] splitItemText = lore.getString().split(" x");
		String itemName = splitItemText[0].trim();
		if (itemName.isEmpty()) return;
		try {
			int amount = splitItemText.length == 2 ? NUMBER_FORMAT.parse(splitItemText[1].trim()).intValue() : 1;

			Pair<String, String> visitorKey = Pair.of(visitorName, visitorTexture);
			Object2IntMap<String> visitorMap = itemMap.computeIfAbsent(visitorKey, _key -> new Object2IntOpenHashMap<>());

			visitorMap.put(itemName, amount); // Replace instead of accumulating repeatedly
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Visitor Helper] Failed to parse item: {}", lore.getString(), e);
		}
	}

	private static void drawScreenItems(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 200);

		int index = 0;
		boolean showStacks = SkyblockerConfigManager.get().farming.garden.showStacksInVisitorHelper;

		Map<String, List<String>> itemToVisitorsMap = new LinkedHashMap<>();
		Map<String, Integer> itemToTotalAmountMap = new LinkedHashMap<>();

		for (Map.Entry<Pair<String, String>, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
			Pair<String, String> visitorName = visitorEntry.getKey();
			Object2IntMap<String> visitorItems = visitorEntry.getValue();

			for (Object2IntMap.Entry<String> itemEntry : visitorItems.object2IntEntrySet()) {
				String itemName = itemEntry.getKey();
				int amount = itemEntry.getIntValue();

				itemToVisitorsMap.computeIfAbsent(itemName, k -> new ArrayList<>()).add(visitorName.left());
				itemToTotalAmountMap.put(itemName, itemToTotalAmountMap.getOrDefault(itemName, 0) + amount);
			}
		}

		Set<String> processedVisitors = new HashSet<>();
		for (Map.Entry<String, List<String>> groupedEntry : itemToVisitorsMap.entrySet()) {
			String itemName = groupedEntry.getKey();
			List<String> visitors = groupedEntry.getValue();
			int totalAmount = itemToTotalAmountMap.get(itemName);

			for (String visitor : visitors) {
				if (!processedVisitors.contains(visitor)) {
					drawTextWithOptionalUnderline(context, textRenderer, Text.literal(visitor), TEXT_START_X, TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight), mouseX, mouseY);
					index++;
					processedVisitors.add(visitor);
				}
			}

			String amountText = showStacks && totalAmount >= 64
					? (totalAmount / 64) + " stacks" + (totalAmount % 64 > 0 ? " + " + (totalAmount % 64) : "")
					: "" + totalAmount;

			Text combinedText = Text.literal("  ")
					.append(Text.literal(itemName + " x" + amountText))
					.append(Text.literal(" [Copy Amount]").formatted(Formatting.YELLOW));

			drawTextWithOptionalUnderline(context, textRenderer, combinedText, TEXT_START_X + ENTRY_INDENT, TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight), mouseX, mouseY);
			index++;
		}

		for (Map.Entry<Pair<String, String>, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
			Pair<String, String> visitorName = visitorEntry.getKey();
			if (processedVisitors.contains(visitorName.left())) continue;

			drawTextWithOptionalUnderline(context, textRenderer, Text.literal(visitorName.left()), TEXT_START_X, TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight), mouseX, mouseY);
			index++;

			for (Object2IntMap.Entry<String> itemEntry : visitorEntry.getValue().object2IntEntrySet()) {
				String itemName = itemEntry.getKey();
				int amount = itemEntry.getIntValue();

				String amountText = showStacks && amount >= 64
						? (amount / 64) + " stacks" + (amount % 64 > 0 ? " + " + (amount % 64) : "")
						: "" + amount;

				Text itemText = Text.literal("  ")
						.append(Text.literal(itemName + " x" + amountText))
						.append(Text.literal(" [Copy Amount]").formatted(Formatting.YELLOW));

				drawTextWithOptionalUnderline(context, textRenderer, itemText, TEXT_START_X + ENTRY_INDENT, TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight), mouseX, mouseY);
				index++;
			}
		}

		context.getMatrices().pop();
	}

	private static int drawItemEntryWithHover(DrawContext context, TextRenderer textRenderer, Object2IntMap.Entry<String> itemEntry, int index, int mouseX, int mouseY) {
        String itemName = itemEntry.getKey();
        int amount = itemEntry.getIntValue();
        ItemStack stack = getCachedItem(itemName);
        drawItemEntryWithHover(context, textRenderer, stack, itemName, amount, index, mouseX, mouseY);
        return index + 1;
    }

    private static ItemStack getCachedItem(String displayName) {
        String strippedName = Formatting.strip(displayName);
        ItemStack cachedStack = itemCache.get(strippedName);
        if (cachedStack != null) return cachedStack;
        if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return null; // Item repo might be taking its sweet time doing things and cause concurrent modification error
        Map<String, NEUItem> items = NEURepoManager.NEU_REPO.getItems().getItems();
        if (items == null) return null;
        ItemStack stack = items.values().stream()
                .filter(i -> Formatting.strip(i.getDisplayName()).equals(strippedName))
                .findFirst()
                .map(NEUItem::getSkyblockItemId)
                .map(ItemRepository::getItemStack)
                .orElse(null);
        if (stack == null) return null;
        itemCache.put(strippedName, stack);
        return stack;
    }

    /**
     * Draws the item entry, amount, and copy amount text with optional underline and the item icon
     */
    private static void drawItemEntryWithHover(DrawContext context, TextRenderer textRenderer, @Nullable ItemStack stack, String itemName, int amount, int index, int mouseX, int mouseY) {
        Text text = stack != null ? stack.getName().copy().append(" x" + amount) : Text.literal(itemName + " x" + amount);
        Text copyAmount = Text.literal(" [Copy Amount]");

        // Calculate the y position of the text with index as the line number
        int y = TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight);
        // Draw the item and amount text
        drawTextWithOptionalUnderline(context, textRenderer, text, TEXT_START_X + ENTRY_INDENT + ITEM_INDENT, y, mouseX, mouseY);
        // Draw the copy amount text separately after the item and amount text
        drawTextWithOptionalUnderline(context, textRenderer, copyAmount, TEXT_START_X + ENTRY_INDENT + ITEM_INDENT + textRenderer.getWidth(text), y, mouseX, mouseY);

        // drawItem adds 150 to the z, which puts our z at 350, above the item in the slot (250) and their text (300) and below the cursor stack (382) and their text (432)
        if (stack != null) {
            context.drawItem(stack, TEXT_START_X + ENTRY_INDENT, y - textRenderer.fontHeight + 5);
        }
    }

    private static void drawTextWithOptionalUnderline(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 150); // This also puts our z at 350
        context.drawText(textRenderer, text, x, y, -1, true);
        if (isMouseOverText(mouseX, mouseY, x, y, textRenderer.getWidth(text), textRenderer.fontHeight)) {
            context.drawHorizontalLine(x, x + textRenderer.getWidth(text), y + textRenderer.fontHeight, -1);
        }
        context.getMatrices().pop();
    }

    private static boolean isMouseOverText(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
