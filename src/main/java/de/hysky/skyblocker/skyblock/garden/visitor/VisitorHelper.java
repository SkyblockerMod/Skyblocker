package de.hysky.skyblocker.skyblock.garden.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Constants;
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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class VisitorHelper extends AbstractWidget {
	private static final Set<Visitor> activeVisitors = new HashSet<>();
	private static final Map<String, ItemStack> cachedItems = new HashMap<>();
	// Map of grouped items with their total amount and associated visitors
	private static final Object2IntMap<Component> groupedItems = new Object2IntOpenHashMap<>();
	private static final Map<Component, List<Visitor>> visitorsByItem = new LinkedHashMap<>();
	private static int xOffset = 4;
	private static int yOffset = 4;
	private static int exclusionZoneWidth = 215;
	private static int exclusionZoneHeight = 215;
	private static final int ICON_SIZE = 16;
	private static final int LINE_HEIGHT = 3;
	private static final int PADDING = 4;
	private static final Object2LongMap<Component> copiedTimestamps = new Object2LongOpenHashMap<>();

	// Used to prevent adding the visitor again after the player clicks accept or refuse.
	private static boolean processVisitor = false;

	private int dragStartX, dragStartY;

	public VisitorHelper(int x, int y) {
		super(x, y, 0, 0, Component.literal("Visitor Helper"));
	}

	@Init
	public static void initialize() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?> handledScreen) || !shouldRender()) return;

			processVisitor = true;
			ScreenEvents.afterTick(screen).register(_screen -> updateVisitors(handledScreen.getMenu()));
			Screens.getButtons(screen).add(new VisitorHelper(xOffset, yOffset));
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _buildContext) ->
				dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("garden").then(literal("visitors")
						.then(literal("clearAll").executes(ctx -> {
							activeVisitors.clear();
							updateItems();
							ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.farming.visitorHelper.command.clearedAllVisitors")));
							return Command.SINGLE_SUCCESS;
						}))
						.then(literal("remove").then(argument("visitor", StringArgumentType.greedyString()).executes(ctx -> {
							String name = ctx.getArgument("visitor", String.class).toLowerCase(Locale.ENGLISH);
							Optional<Visitor> visitor = activeVisitors.stream().filter(v -> v.name().getString().toLowerCase(Locale.ENGLISH).equals(name)).findAny();
							if (visitor.isEmpty()) {
								ctx.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.farming.visitorHelper.command.unableToRemoveVisitor")));
								return Command.SINGLE_SUCCESS;
							}
							activeVisitors.remove(visitor.get());
							updateItems();
							ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatableEscape("skyblocker.farming.visitorHelper.command.removedVisitor", visitor.get().name())));
							return Command.SINGLE_SUCCESS;
						}).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(activeVisitors.stream().map(Visitor::name).map(Component::getString), builder))))
				))));
	}

	public static boolean shouldRender() {
		boolean isHelperEnabled = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelper;
		boolean isGardenMode = SkyblockerConfigManager.get().farming.visitorHelper.visitorHelperGardenOnly;
		return isHelperEnabled && (!isGardenMode || Utils.isInGarden() || Utils.getArea() == Area.BAZAAR);
	}

	public static List<ScreenRectangle> getExclusionZones() {
		if (activeVisitors.isEmpty()) return List.of();

		return List.of(new ScreenRectangle(new ScreenPosition(xOffset, yOffset), exclusionZoneWidth, exclusionZoneHeight));
	}

	/**
	 * Updates the current visitors and their required items.
	 */
	private static void updateVisitors(AbstractContainerMenu handler) {
		if (!processVisitor) return;
		ItemStack visitorHead = handler.getSlot(13).getItem();
		if (visitorHead.isEmpty() || !visitorHead.has(DataComponents.LORE) || ItemUtils.getLoreLineIf(visitorHead, t -> t.contains("Times Visited")) == null) return;

		Component visitorName = visitorHead.getHoverName();
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
	private static void extractRequiredItems(AbstractContainerMenu handler, Visitor visitor) {
		ItemStack acceptButton = handler.getSlot(29).getItem();
		if (acceptButton.isEmpty() || ItemUtils.getLoreLineIf(acceptButton, t -> t.contains("Items Required")) == null) return;

		acceptButton.skyblocker$getLoreStrings().stream()
				.map(String::trim)
				.dropWhile(lore -> !lore.contains("Items Required")) // All lines before Items Required (shouldn't be any, but you never know)
				.skip(1) // skip the Items Required line
				.takeWhile(lore -> !lore.isEmpty()) // All lines until the blank line before Rewards
				.forEach(requirement -> {
					String[] split = requirement.split(" x");
					Component item = Component.nullToEmpty(split[0].trim());
					if (split.length == 1) visitor.addRequiredItem(item, 1);
					else visitor.addRequiredItem(item, Formatters.parseNumber(split[1].trim()).intValue());
				});
	}

	private static void updateItems() {
		groupedItems.clear();
		visitorsByItem.clear();

		// Group items by their name and accumulate their counts
		for (Visitor visitor : activeVisitors) {
			for (Object2IntMap.Entry<Component> entry : visitor.requiredItems().object2IntEntrySet()) {
				Component itemName = entry.getKey();
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
		String cleanName = ChatFormatting.stripFormatting(itemName);
		return cachedItems.computeIfAbsent(cleanName, name -> {
			if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return ItemUtils.getNamedPlaceholder(itemName);

			return NEURepoManager.getItemByName(itemName)
					.stream()
					.findFirst()
					.map(NEUItem::getSkyblockItemId)
					.map(ItemRepository::getItemStack)
					.orElseGet(() -> ItemUtils.getNamedPlaceholder(itemName));
		});
	}

	/**
	 * Draws the visitor items and their associated information.
	 */
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		if (activeVisitors.isEmpty()) return;

		Font textRenderer = Minecraft.getInstance().font;
		int index = 0;
		int newWidth = 0;
		int x = getX() + PADDING;
		int y = getY() - (int) (textRenderer.lineHeight / 2f - ICON_SIZE * 0.95f / 2) + PADDING;
		context.fill(getX(), getY(), getRight(), getBottom(), 0x18_80_80_80);

		for (Object2IntMap.Entry<Component> entry : groupedItems.object2IntEntrySet()) {
			Component itemName = entry.getKey();
			int totalAmount = entry.getIntValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors == null || visitors.isEmpty()) continue;

			// Render visitors' heads for the shared item
			for (Visitor visitor : visitors) {
				int yPosition = y + index * (LINE_HEIGHT + textRenderer.lineHeight);

				context.pose().pushMatrix();
				context.pose().translate(x, yPosition + (float) textRenderer.lineHeight / 2 - ICON_SIZE * 0.95f / 2);
				context.pose().scale(0.95f, 0.95f);
				context.renderItem(visitor.head(), 0, 0);
				context.pose().popMatrix();

				context.drawString(textRenderer, visitor.name(), x + (int) (ICON_SIZE * 0.95f) + 4, yPosition, CommonColors.WHITE, true);

				index++;
			}

			// Render the shared item with the total amount
			int iconX = x + 12;
			int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
			int yPosition = y + index * (LINE_HEIGHT + textRenderer.lineHeight);

			ItemStack cachedStack = getCachedItem(itemName.getString());
			context.pose().pushMatrix();
			context.pose().translate(iconX, yPosition + (float) textRenderer.lineHeight / 2 - ICON_SIZE * 0.95f / 2);
			context.pose().scale(0.95f, 0.95f);
			context.renderItem(cachedStack, 0, 0);
			context.pose().popMatrix();

			MutableComponent name = cachedStack.getHoverName().copy();
			MutableComponent itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper && totalAmount >= 64
					? name.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
					: name.append(" x" + totalAmount);

			if (copiedTimestamps.containsKey(itemName)) {
				long timeSinceCopy = System.currentTimeMillis() - copiedTimestamps.getLong(itemName);
				if (timeSinceCopy < 1000) {
					itemText.append(Component.literal(" ✔ ").withStyle(ChatFormatting.GREEN));
				} else {
					copiedTimestamps.removeLong(itemName);
				}
			}
			newWidth = Math.max(newWidth, textX + textRenderer.width(itemText) - x);

			drawTextWithHoverUnderline(context, textRenderer, itemText, textX, yPosition, mouseX, mouseY);

			index++;
		}
		setHeight(index * (LINE_HEIGHT + Minecraft.getInstance().font.lineHeight) + PADDING * 2);
		setWidth(newWidth + PADDING * 2);
		exclusionZoneWidth = getWidth();
		exclusionZoneHeight = getHeight();
	}

	@Override
	protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
		if (activeVisitors.isEmpty()) return;
		setPosition(xOffset = (int) click.x() - dragStartX, yOffset = (int) click.y() - dragStartY);
	}

	/**
	 * Handles mouse click events on the visitor UI.
	 */
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (activeVisitors.isEmpty()) return;

		Font textRenderer = Minecraft.getInstance().font;
		dragStartX = (int) click.x() - getX();
		dragStartY = (int) click.y() - getY();

		int index = 0;
		int y = getY() - (int) (textRenderer.lineHeight / 2f - ICON_SIZE * 0.95f / 2) + PADDING;

		for (Object2IntMap.Entry<Component> entry : groupedItems.object2IntEntrySet()) {
			Component itemName = entry.getKey();
			int totalAmount = entry.getIntValue();
			List<Visitor> visitors = visitorsByItem.get(itemName);

			if (visitors != null && !visitors.isEmpty()) {
				index += visitors.size();

				int iconX = getX() + 12;
				int textX = iconX + (int) (ICON_SIZE * 0.95f) + 4;
				int yPosition = y + index * (LINE_HEIGHT + textRenderer.lineHeight);

				MutableComponent name = itemName.copy();
				Component itemText = SkyblockerConfigManager.get().farming.visitorHelper.showStacksInVisitorHelper && totalAmount >= 64
						? name.append(" x" + (totalAmount / 64) + " stacks + " + (totalAmount % 64))
						: name.append(" x" + totalAmount);

				if (isMouseOverText(textRenderer, itemText, textX, yPosition, click.x(), click.y())) {
					Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(totalAmount));
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
		if ((slotId == 29 || slotId == 13 || slotId == 33) && slot.hasItem() &&
				ItemUtils.getLoreLineIf(slot.getItem(), s -> s.equals("Click to give!") || s.equals("Click to refuse!")) != null) {
			activeVisitors.removeIf(entry -> entry.name().getString().equals(title) && visitorHeadSlot.hasItem() && ItemUtils.getHeadTexture(visitorHeadSlot.getItem()).equals(ItemUtils.getHeadTexture(entry.head())));
			processVisitor = false;
		}

		updateItems();
	}

	private static void drawTextWithHoverUnderline(GuiGraphics context, Font textRenderer, Component text, int x, int y, double mouseX, double mouseY) {
		context.drawString(textRenderer, text, x, y, CommonColors.WHITE, true);

		if (isMouseOverText(textRenderer, text, x, y, mouseX, mouseY)) {
			context.hLine(x, x + textRenderer.width(text), y + textRenderer.lineHeight, CommonColors.WHITE);
		}
	}

	/**
	 * Checks if the mouse is over a specific rectangular region.
	 */
	private static boolean isMouseOverText(Font textRenderer, Component text, int x, int y, double mouseX, double mouseY) {
		return HudHelper.pointIsInArea(mouseX, mouseY, x, y, x + textRenderer.width(text), y + textRenderer.lineHeight);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
