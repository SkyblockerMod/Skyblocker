package de.hysky.skyblocker.skyblock.garden.visitor;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class VisitorHelper {
	private static final Set<Visitor> activeVisitors = new HashSet<>();
	private static final Map<String, ItemStack> cachedItems = new HashMap<>();
	// Map of grouped items with their total amount and associated visitors
	private static final Object2IntMap<Text> groupedItems = new Object2IntOpenHashMap<>();
	private static final Map<Text, List<Visitor>> visitorsByItem = new LinkedHashMap<>();
	private static final int X_OFFSET = 4;
	private static final int Y_OFFSET = 4;
	private static final int ICON_SIZE = 16;
	private static final int LINE_HEIGHT = 3;

	@Init
	public static void initialize() {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen) || !shouldRender()) return;

			ScreenEvents.afterTick(screen).register(_screen -> updateVisitors(handledScreen.getScreenHandler()));
			ScreenEvents.afterRender(screen).register((_screen, context, _x, _y, _d) -> renderVisitorHelper(context, client.textRenderer));
		});
	}

	public static boolean shouldRender() {
		boolean isHelperEnabled = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelper;
		boolean isGardenMode = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelperGardenOnly;
		return isHelperEnabled && (!isGardenMode || Utils.isInGarden() || Utils.getIslandArea().contains("Bazaar"));
	}

	/**
	 * Updates the current visitors and their required items.
	 */
	private static void updateVisitors(ScreenHandler handler) {
		ItemStack visitorHead = handler.getSlot(13).getStack();
		if (visitorHead == null || !visitorHead.contains(DataComponentTypes.LORE) || ItemUtils.getLoreLineIf(visitorHead, t -> t.contains("Times Visited")) == null) return;

		Text visitorName = visitorHead.getName();
		if (activeVisitors.stream().map(Visitor::name).anyMatch(visitorName::equals)) return;

		Visitor newVisitor = new Visitor(visitorName, visitorHead.copy());
		extractRequiredItems(handler, newVisitor);

		if (!newVisitor.requiredItems().isEmpty()) {
			activeVisitors.add(newVisitor);
		}

		updateItems();
	}

	/**
	 * Extracts the required items for the given visitor.
	 */
	private static void extractRequiredItems(ScreenHandler handler, Visitor visitor) {
		ItemStack acceptButton = handler.getSlot(29).getStack();
		if (acceptButton == null || ItemUtils.getLoreLineIf(acceptButton, t -> t.contains("Items Required")) == null) return;

		ItemUtils.getLore(acceptButton).stream()
				.map(Text::getString)
				.map(String::trim)
				.takeWhile(lore -> !lore.contains("Rewards"))
				.filter(lore -> lore.contains(" x"))
				.map(lore -> lore.split(" x"))
				.forEach(parts -> visitor.addRequiredItem(Text.literal(parts[0].trim()), Integer.parseInt(parts[1].trim())));
	}

	private static void updateItems() {
		groupedItems.clear();
		visitorsByItem.clear();

		// Group items by their name and accumulate their counts
		for (Visitor visitor : activeVisitors) {
			for (Object2IntMap.Entry<Text> entry : visitor.requiredItems().object2IntEntrySet()) {
				Text itemName = entry.getKey();
				int amount = entry.getIntValue();

				groupedItems.put(itemName, groupedItems.getOrDefault(itemName, 0) + amount);
				visitorsByItem.computeIfAbsent(itemName, k -> new LinkedList<>()).add(visitor);
			}
		}
	}

	/**
	 * Retrieves a cached ItemStack or fetches it if not already cached.
	 */
	private static ItemStack getCachedItem(String itemName) {
		String cleanName = Formatting.strip(itemName);
		return cachedItems.computeIfAbsent(cleanName, name -> {
			if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return null;

			return NEURepoManager.NEU_REPO.getItems().getItems()
					.values().stream()
					.filter(item -> Formatting.strip(item.getDisplayName()).equals(name))
					.findFirst()
					.map(NEUItem::getSkyblockItemId)
					.map(ItemRepository::getItemStack)
					.orElse(null);
		});
	}

	/**
	 * Draws the visitor items and their associated information.
	 */
	private static void renderVisitorHelper(DrawContext context, TextRenderer textRenderer) {
		int index = 0;

		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 500);

		for (Object2IntMap.Entry<Text> entry : groupedItems.object2IntEntrySet()) {
			Text itemName = entry.getKey();
			int totalAmount = entry.getIntValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors == null || visitors.isEmpty()) continue;

			// Render visitors' heads for the shared item
			for (Visitor visitor : visitors) {
				int yPosition = Y_OFFSET + index * (LINE_HEIGHT + textRenderer.fontHeight);

				context.getMatrices().push();
				context.getMatrices().translate(X_OFFSET, yPosition + (float) textRenderer.fontHeight / 2 - ICON_SIZE * 0.95f / 2, 0);
				context.getMatrices().scale(0.95f, 0.95f, 1.0f);
				context.drawItem(visitor.head(), 0, 0);
				context.getMatrices().pop();

				context.drawText(textRenderer, visitor.name(), X_OFFSET + (int) (ICON_SIZE * 0.95f) + 4, yPosition, -1, true);

				index++;
			}

			// Render the shared item with the total amount
			int iconX = X_OFFSET + 12;
			int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
			int yPosition = Y_OFFSET + index * (LINE_HEIGHT + textRenderer.fontHeight);

			ItemStack cachedStack = getCachedItem(itemName.getString());
			if (cachedStack != null) {
				context.getMatrices().push();
				context.getMatrices().translate(iconX, yPosition + (float) textRenderer.fontHeight / 2 - ICON_SIZE * 0.95f / 2, 0);
				context.getMatrices().scale(0.95f, 0.95f, 1.0f);
				context.drawItem(cachedStack, 0, 0);
				context.getMatrices().pop();
			}

			Text itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper
					? cachedStack.getName().copy()
					.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
					: cachedStack.getName().copy()
					.append(" x" + totalAmount);

			int itemTextWidth = textRenderer.getWidth(itemText);
			int copyTextX = textX + itemTextWidth;

			context.drawText(textRenderer, itemText, textX, yPosition, -1, true);
			context.drawText(textRenderer, Text.literal(" [Copy Amount]").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), copyTextX, yPosition, -1, true);

			index++;
		}

		context.getMatrices().pop();
	}


	/**
	 * Handles mouse click events on the visitor UI.
	 */
	public static void handleMouseClick(double mouseX, double mouseY, int mouseButton, TextRenderer textRenderer) {
		if (mouseButton != 0) return;

		int index = 0;
		int yOffsetAdjustment = -5;

		for (Map.Entry<Text, Integer> entry : groupedItems.entrySet()) {
			Text itemName = entry.getKey();
			int totalAmount = entry.getValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors != null && !visitors.isEmpty()) {
				for (Visitor ignored : visitors) {
					index++;
				}

				int iconX = X_OFFSET + 12;
				int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
				int yPosition = Y_OFFSET + index * (LINE_HEIGHT + textRenderer.fontHeight) -
						(int) ((float) textRenderer.fontHeight / 2 - ICON_SIZE * 0.95f / 2) + yOffsetAdjustment;

				Text itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper
						? itemName.copy()
						.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
						: itemName.copy()
						.append(" x" + totalAmount);

				int itemTextWidth = textRenderer.getWidth(itemText);
				int copyTextX = textX + itemTextWidth;

				if (isMouseOverText(mouseX, mouseY, textX, yPosition, itemTextWidth, textRenderer.fontHeight)) {
					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName.getString(), true);
					return;
				}

				if (isMouseOverText(mouseX, mouseY, copyTextX, yPosition, textRenderer.getWidth(" [Copy Amount]"), textRenderer.fontHeight)) {
					MinecraftClient.getInstance().keyboard.setClipboard(String.valueOf(totalAmount));
					MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append("Copied amount successfully"), false);
					return;
				}

				index++;
			}
		}
	}

	/**
	 * Handles slot clicks to remove a visitor when certain conditions are met.
	 *
	 * @param title The visitor's name to match for removal.
	 */
	public static void onSlotClick(Slot slot, int slotId, String title) {
		if ((slotId == 29 || slotId == 13 || slotId == 33) && slot.hasStack() &&
				ItemUtils.getLoreLineIf(slot.getStack(), s -> s.equals("Click to give!") || s.equals("Click to refuse!")) != null) {
			activeVisitors.removeIf(entry -> entry.name().getString().equals(title));
		}

		updateItems();
	}

	/**
	 * Checks if the mouse is over a specific rectangular region.
	 */
	private static boolean isMouseOverText(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
