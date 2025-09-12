package de.hysky.skyblocker.skyblock.garden.visitor;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.shedaniel.math.Rectangle;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.*;

public class VisitorHelper extends ClickableWidget {
	private static final Set<Visitor> activeVisitors = new HashSet<>();
	private static final Map<String, ItemStack> cachedItems = new HashMap<>();
	// Map of grouped items with their total amount and associated visitors
	private static final Object2IntMap<Text> groupedItems = new Object2IntOpenHashMap<>();
	private static final Map<Text, List<Visitor>> visitorsByItem = new LinkedHashMap<>();
	private static int xOffset = 4;
	private static int yOffset = 4;
	private static int exclusionZoneWidth = 215;
	private static int exclusionZoneHeight = 215;
	private static final int ICON_SIZE = 16;
	private static final int LINE_HEIGHT = 3;
	private static final int PADDING = 4;
	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
	private static final Object2LongMap<Text> copiedTimestamps = new Object2LongOpenHashMap<>();

	// Used to prevent adding the visitor again after the player clicks accept or refuse.
	private static boolean processVisitor = false;

	private int dragStartX, dragStartY;

	public VisitorHelper(int x, int y) {
		super(x, y, 0, 0, Text.literal("Visitor Helper"));
	}

	@Init
	public static void initialize() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen) || !shouldRender()) return;

			processVisitor = true;
			ScreenEvents.afterTick(screen).register(_screen -> updateVisitors(handledScreen.getScreenHandler()));
			Screens.getButtons(screen).add(new VisitorHelper(xOffset, yOffset));
		});
	}

	public static boolean shouldRender() {
		boolean isHelperEnabled = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelper;
		boolean isGardenMode = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelperGardenOnly;
		return isHelperEnabled && (!isGardenMode || Utils.isInGarden() || Utils.getIslandArea().contains("Bazaar"));
	}

	public static List<Rectangle> getExclusionZones() {
		if (activeVisitors.isEmpty()) return List.of();

		return List.of(new Rectangle(xOffset, yOffset, exclusionZoneWidth, exclusionZoneHeight));
	}

	/**
	 * Updates the current visitors and their required items.
	 */
	private static void updateVisitors(ScreenHandler handler) {
		if (!processVisitor) return;
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
				.dropWhile(lore -> !lore.contains("Items Required")) // All lines before Items Required (shouldn't be any, but you never know)
				.skip(1) // skip the Items Required line
				.takeWhile(lore -> !lore.isEmpty()) // All lines until the blank line before Rewards
				.forEach(requirement -> {
					String[] split = requirement.split(" x");
					Text item = Text.of(split[0].trim());
					if (split.length == 1) visitor.addRequiredItem(item, 1);
					else visitor.addRequiredItem(item, Formatters.parseNumber(split[1].trim()).intValue());
				});
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

			return NEURepoManager.getItemByName(itemName)
					.stream()
					.findFirst()
					.map(NEUItem::getSkyblockItemId)
					.map(ItemRepository::getItemStack)
					.orElse(BARRIER);
		});
	}

	/**
	 * Draws the visitor items and their associated information.
	 */
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int index = 0;
		int newWidth = 0;
		int x = getX() + PADDING;
		int y = getY() - (int) (textRenderer.fontHeight / 2f - ICON_SIZE * 0.95f / 2) + PADDING;
		context.fill(getX(), getY(), getRight(), getBottom(), 0x18_80_80_80);

		for (Object2IntMap.Entry<Text> entry : groupedItems.object2IntEntrySet()) {
			Text itemName = entry.getKey();
			int totalAmount = entry.getIntValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors == null || visitors.isEmpty()) continue;

			// Render visitors' heads for the shared item
			for (Visitor visitor : visitors) {
				int yPosition = y + index * (LINE_HEIGHT + textRenderer.fontHeight);

				context.getMatrices().pushMatrix();
				context.getMatrices().translate(x, yPosition + (float) textRenderer.fontHeight / 2 - ICON_SIZE * 0.95f / 2);
				context.getMatrices().scale(0.95f, 0.95f);
				context.drawItem(visitor.head(), 0, 0);
				context.getMatrices().popMatrix();

				context.drawText(textRenderer, visitor.name(), x + (int) (ICON_SIZE * 0.95f) + 4, yPosition, Colors.WHITE, true);

				index++;
			}

			// Render the shared item with the total amount
			int iconX = x + 12;
			int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
			int yPosition = y + index * (LINE_HEIGHT + textRenderer.fontHeight);

			ItemStack cachedStack = getCachedItem(itemName.getString());
			if (cachedStack != null) {
				context.getMatrices().pushMatrix();
				context.getMatrices().translate(iconX, yPosition + (float) textRenderer.fontHeight / 2 - ICON_SIZE * 0.95f / 2);
				context.getMatrices().scale(0.95f, 0.95f);
				context.drawItem(cachedStack, 0, 0);
				context.getMatrices().popMatrix();
			}

			MutableText name = cachedStack != null ? cachedStack.getName().copy() : itemName.copy();
			MutableText itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper && totalAmount >= 64
					? name.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
					: name.append(" x" + totalAmount);

			if (copiedTimestamps.containsKey(itemName)) {
				long timeSinceCopy = System.currentTimeMillis() - copiedTimestamps.getLong(itemName);
				if (timeSinceCopy < 1000) {
					itemText.append(Text.literal(" ✔ ").formatted(Formatting.GREEN));
				} else {
					copiedTimestamps.removeLong(itemName);
				}
			}
			newWidth = Math.max(newWidth, textX + textRenderer.getWidth(itemText) - x);

			drawTextWithHoverUnderline(context, textRenderer, itemText, textX, yPosition, mouseX, mouseY);

			index++;
		}
		setHeight((groupedItems.size() + activeVisitors.size()) * (LINE_HEIGHT + MinecraftClient.getInstance().textRenderer.fontHeight) + PADDING * 2);
		setWidth(newWidth + PADDING * 2);
		exclusionZoneWidth = getWidth();
		exclusionZoneHeight = getHeight();
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		setPosition(xOffset = (int) mouseX - dragStartX, yOffset = (int) mouseY - dragStartY);
	}

	/**
	 * Handles mouse click events on the visitor UI.
	 */
	public void onClick(double mouseX, double mouseY) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		dragStartX = (int) mouseX - getX();
		dragStartY = (int) mouseY - getY();

		int index = 0;
		int y = getY() - (int) (textRenderer.fontHeight / 2f - ICON_SIZE * 0.95f / 2) + PADDING;

		for (Object2IntMap.Entry<Text> entry : groupedItems.object2IntEntrySet()) {
			Text itemName = entry.getKey();
			int totalAmount = entry.getIntValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors != null && !visitors.isEmpty()) {
				index += visitors.size();

				int iconX = getX() + 12;
				int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
				int yPosition = y + index * (LINE_HEIGHT + textRenderer.fontHeight);

				MutableText name = itemName.copy();
				Text itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper && totalAmount >= 64
						? name.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
						: name.append(" x" + totalAmount);

				if (isMouseOverText(textRenderer, itemText, textX, yPosition, mouseX, mouseY)) {
					MinecraftClient.getInstance().keyboard.setClipboard(String.valueOf(totalAmount));
					copiedTimestamps.put(itemName, System.currentTimeMillis());

					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName.getString(), true);

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
	public static void onSlotClick(Slot slot, int slotId, String title, Slot visitorHeadSlot) {
		if ((slotId == 29 || slotId == 13 || slotId == 33) && slot.hasStack() &&
				ItemUtils.getLoreLineIf(slot.getStack(), s -> s.equals("Click to give!") || s.equals("Click to refuse!")) != null) {
			activeVisitors.removeIf(entry -> entry.name().getString().equals(title) && visitorHeadSlot.hasStack() && ItemUtils.getHeadTexture(visitorHeadSlot.getStack()).equals(ItemUtils.getHeadTexture(entry.head())));
			processVisitor = false;
		}

		updateItems();
	}

	private static void drawTextWithHoverUnderline(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, double mouseX, double mouseY) {
		context.drawText(textRenderer, text, x, y, Colors.WHITE, true);

		if (isMouseOverText(textRenderer, text, x, y, mouseX, mouseY)) {
			context.drawHorizontalLine(x, x + textRenderer.getWidth(text), y + textRenderer.fontHeight, Colors.WHITE);
		}
	}

	/**
	 * Checks if the mouse is over a specific rectangular region.
	 */
	private static boolean isMouseOverText(TextRenderer textRenderer, Text text, int x, int y, double mouseX, double mouseY) {
		return HudHelper.pointIsInArea(mouseX, mouseY, x, y, x + textRenderer.getWidth(text), y + textRenderer.fontHeight);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
