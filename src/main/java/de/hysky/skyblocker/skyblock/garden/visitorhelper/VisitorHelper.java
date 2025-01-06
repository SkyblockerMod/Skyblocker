package de.hysky.skyblocker.skyblock.garden.visitorhelper;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
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

	private static final Map<Visitor, Boolean> activeVisitors = new LinkedHashMap<>();
	private static final Map<String, ItemStack> cachedItems = new HashMap<>();
	private static final int X_OFFSET = 4;
	private static final int Y_OFFSET = 4;
	private static final int ICON_SIZE = 16;
	private static final int LINE_HEIGHT = 3;

	@Init
	public static void initialize() {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen)) return;

			boolean isHelperEnabled = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelper;
			boolean isGardenMode = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelperGardenOnly;
			boolean canRenderScreen = isHelperEnabled && (!isGardenMode || Utils.isOnGarden() || Utils.getIslandArea().contains("Bazaar"));

			if (canRenderScreen) {
				ScreenEvents.afterRender(screen).register((screen_, context, mouseX, mouseY, delta) ->
						renderVisitorUI(context, client.textRenderer, handledScreen.getScreenHandler()));
			}
		});
	}

	/**
	 * Renders the visitor UI on the screen.
	 */
	public static void renderVisitorUI(DrawContext context, TextRenderer textRenderer, ScreenHandler handler) {
		updateVisitors(handler);
		drawVisitorItems(context, textRenderer);
	}

	/**
	 * Updates the current visitors and their required items.
	 */
	private static void updateVisitors(ScreenHandler handler) {
		ItemStack visitorHead = handler.getSlot(13).getStack();
		if (visitorHead == null || !visitorHead.contains(DataComponentTypes.LORE) || ItemUtils.getLoreLineIf(visitorHead, t -> t.contains("Times Visited")) == null) return;

		Text visitorName = visitorHead.getName();
		if (activeVisitors.keySet().stream().anyMatch(visitor -> visitor.name().equals(visitorName))) return;

		Visitor newVisitor = new Visitor(visitorName, visitorHead.copy());
		extractRequiredItems(handler, newVisitor);

		if (!newVisitor.requiredItems().isEmpty()) {
			activeVisitors.put(newVisitor, true);
		}
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
				.filter(lore -> !lore.isEmpty() && !lore.contains("Rewards"))
				.filter(lore -> lore.contains(" x"))
				.forEach(lore -> {
					String[] parts = lore.split(" x");
					visitor.addRequiredItem(Text.literal(parts[0].trim()), Integer.parseInt(parts[1].trim()));
				});
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
	private static void drawVisitorItems(DrawContext context, TextRenderer textRenderer) {
		int index = 0;
		Map<Text, Integer> groupedItems = new LinkedHashMap<>();
		Map<Text, List<Visitor>> visitorsByItem = new LinkedHashMap<>();

		activeVisitors.keySet().forEach(visitor ->
				visitor.requiredItems().forEach((itemName, amount) -> {
					groupedItems.put(itemName, groupedItems.getOrDefault(itemName, 0) + amount);
					visitorsByItem.computeIfAbsent(itemName, k -> new LinkedList<>()).add(visitor);
				})
		);

		context.getMatrices().push();
		context.getMatrices().translate(0, 0, 200);

		for (Map.Entry<Text, Integer> entry : groupedItems.entrySet()) {
			Text itemName = entry.getKey();
			int totalAmount = entry.getValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors == null || visitors.isEmpty()) continue;

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

		Map<Text, Integer> groupedItems = new LinkedHashMap<>();
		Map<Text, List<Visitor>> visitorsByItem = new LinkedHashMap<>();

		for (Visitor visitor : activeVisitors.keySet()) {
			for (Map.Entry<Text, Integer> entry : visitor.requiredItems().entrySet()) {
				Text itemName = entry.getKey();
				int amount = entry.getValue();

				groupedItems.put(itemName, groupedItems.getOrDefault(itemName, 0) + amount);
				visitorsByItem.computeIfAbsent(itemName, k -> new LinkedList<>()).add(visitor);
			}
		}

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
	 * @param title  The visitor's name to match for removal.
	 */
	public static void onSlotClick(Slot slot, int slotId, String title) {
		if ((slotId == 29 || slotId == 13 || slotId == 33) && slot.hasStack() &&
				ItemUtils.getLoreLineIf(slot.getStack(), s -> s.equals("Click to give!") || s.equals("Click to refuse!")) != null) {

			Visitor visitorToRemove = null;

			for (Visitor visitor : activeVisitors.keySet()) {
				if (visitor.name().getString().equals(title)) {
					visitorToRemove = visitor;
					break;
				}
			}

			if (visitorToRemove != null) {
				activeVisitors.remove(visitorToRemove);
			}
		}
	}

	/**
	 * Checks if the mouse is over a specific rectangular region.
	 */
	private static boolean isMouseOverText(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
