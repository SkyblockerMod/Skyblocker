package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.*;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

// TODO: recommend disabling spacing and enabling wrapping
public class WidgetsListTab implements Tab {
	private final WidgetsElementList widgetsElementList;
	private final ButtonWidget back;
	private final ButtonWidget previousPage;
	private final ButtonWidget nextPage;
	private final ButtonWidget thirdColumnButton;
	private @Nullable GenericContainerScreenHandler handler;
	private final MinecraftClient client;
	private boolean waitingForServer = false;

	private final Int2ObjectMap<WidgetsListSlotEntry> entries = new Int2ObjectOpenHashMap<>();
	private final List<WidgetEntry> customWidgetEntries = new ArrayList<>();
	private boolean listNeedsUpdate = false;
	private boolean shouldShowCustomWidgetEntries = false;


	public void setCustomWidgetEntries(Collection<WidgetEntry> entries) {
		this.customWidgetEntries.clear();
		this.customWidgetEntries.addAll(entries);
		listNeedsUpdate = true;
	}

	public List<WidgetEntry> getCustomWidgetEntries() {
		return customWidgetEntries;
	}

	public boolean listNeedsUpdate() {
		boolean b = listNeedsUpdate;
		listNeedsUpdate = false;
		return b;
	}

	public ObjectSet<Int2ObjectMap.Entry<WidgetsListSlotEntry>> getEntries() {
		return entries.int2ObjectEntrySet();
	}

	public WidgetsListTab(MinecraftClient client, @Nullable GenericContainerScreenHandler handler) {
		widgetsElementList = new WidgetsElementList(this, client, 0, 0, 0);
		this.client = client;
		this.handler = handler;
		back = ButtonWidget.builder(Text.translatable("gui.back"), button -> clickAndWaitForServer(48, 0))
				.size(64, 15)
				.build();
		thirdColumnButton = ButtonWidget.builder(Text.literal("3rd Column:"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build();
		thirdColumnButton.setTooltip(Tooltip.of(Text.literal("It is recommended to have this enabled, to have more info be displayed!")));
		previousPage = ButtonWidget.builder(Text.translatable("book.page_button.previous"), button -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build();
		nextPage = ButtonWidget.builder(Text.translatable("book.page_button.next"), button -> clickAndWaitForServer(53, 0))
				.size(100, 15)
				.build();
		if (handler == null) {
			back.visible = false;
			previousPage.visible = false;
			nextPage.visible = false;
			thirdColumnButton.visible = false;
		}
	}

	@Override
	public Text getTitle() {
		return Text.literal("Widgets");
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		consumer.accept(back);
		consumer.accept(previousPage);
		consumer.accept(nextPage);
		consumer.accept(thirdColumnButton);
		consumer.accept(widgetsElementList);
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.interactionManager == null || this.client.player == null) return;
		client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.PICKUP, this.client.player);
		waitingForServer = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.interactionManager == null || this.client.player == null) return;
		client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.QUICK_MOVE, this.client.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void updateHandler(GenericContainerScreenHandler newHandler) {
		this.handler = newHandler;
		back.visible = handler != null;
		entries.clear();
		listNeedsUpdate = true;
	}

	public void hopper(@Nullable List<Text> hopperTooltip) {
		if (hopperTooltip == null) {
			widgetsElementList.setEditingPosition(-1);
			return;
		}
		int start = -1;
		int editing = 1;
		for (int i = 0; i < hopperTooltip.size(); i++) {
			Text text = hopperTooltip.get(i);
			String string = text.getString();
			if (start == -1 && string.contains("▶")) {
				start = i;
			}
			if (string.contains("(EDITING)")) {
				editing = i;
				break;
			}
		}
		widgetsElementList.setEditingPosition(editing - start);
	}

	public void onSlotChange(int slot, ItemStack stack) {
		waitingForServer = false;
		listNeedsUpdate = true;
		switch (slot) {
			case 45 -> {
				previousPage.visible = stack.isOf(Items.ARROW);
				return;
			}
			case 53 -> {
				nextPage.visible = stack.isOf(Items.ARROW);
				return;
			}
			case 50 -> {
				thirdColumnButton.visible = stack.isOf(Items.BOOKSHELF) || stack.isOf(Items.STONE_BUTTON);
				if (thirdColumnButton.visible) {
					if (stack.isOf(Items.STONE_BUTTON))
						thirdColumnButton.setMessage(Text.literal("Apply to all locations"));
					else if (ItemUtils.getLoreLineIf(stack, s -> s.contains("DISABLED")) == null)
						thirdColumnButton.setMessage(Text.literal("3rd Column: ").append(WidgetsListSlotEntry.ENABLED_TEXT));
					else
						thirdColumnButton.setMessage(Text.literal("3rd Column: ").append(WidgetsListSlotEntry.DISABLED_TEXT));
				}
				return;
			}
		}

		if (stack.isEmpty() || stack.isOf(Items.BLACK_STAINED_GLASS_PANE)) {
			entries.remove(slot);
			return;
		}


		String lowerCase = stack.getName().getString().trim().toLowerCase(Locale.ENGLISH);
		List<Text> lore = ItemUtils.getLore(stack);
		String lastLowerCase = lore.getLast().getString().toLowerCase(Locale.ENGLISH);

		WidgetsListSlotEntry entry;
		if (lowerCase.startsWith("widgets on") || lowerCase.startsWith("widgets in") || lastLowerCase.contains("click to edit") || stack.isOf(Items.RED_STAINED_GLASS_PANE)) {
			entry = new EditableSlotEntry(this, slot, stack);
		} else if (lowerCase.endsWith("widget")) {
			entry = new WidgetSlotEntry(this, slot, stack);
		} else if (lastLowerCase.contains("enable") || lastLowerCase.contains("disable")) {
			entry = new BooleanSlotEntry(this, slot, stack);
		} else {
			entry = new DefaultSlotEntry(this, slot, stack);
		}
		entries.put(slot, entry);

	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		back.setPosition(16, tabArea.getTop() + 4);
		widgetsElementList.setY(tabArea.getTop());
		widgetsElementList.setDimensions(tabArea.width(), tabArea.height() - 20);
		widgetsElementList.refreshScroll();
		previousPage.setPosition(widgetsElementList.getRowLeft(), widgetsElementList.getBottom() + 4);
		nextPage.setPosition(widgetsElementList.getScrollbarX() - 100, widgetsElementList.getBottom() + 4);
		thirdColumnButton.setPosition(widgetsElementList.getScrollbarX() + 5, widgetsElementList.getBottom() + 4);
	}

	public boolean shouldShowCustomWidgetEntries() {
		return shouldShowCustomWidgetEntries;
	}

	public void setShouldShowCustomWidgetEntries(boolean shouldShowCustomWidgetEntries) {
		this.shouldShowCustomWidgetEntries = shouldShowCustomWidgetEntries;
	}

	@Override
	public Text getNarratedHint() {
		return Text.empty();
	}
}
