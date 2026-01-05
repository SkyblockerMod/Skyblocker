package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.BooleanSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.DefaultSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.EditableSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetsListSlotEntry;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

// TODO: recommend disabling spacing and enabling wrapping
public class WidgetsListTab implements Tab {
	private final WidgetsElementList widgetsElementList;
	private final Button back;
	private final Button previousPage;
	private final Button nextPage;
	private final Button thirdColumnButton;
	private @Nullable ChestMenu handler;
	private final Minecraft client;
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

	public WidgetsListTab(Minecraft client, @Nullable ChestMenu handler) {
		widgetsElementList = new WidgetsElementList(this, client, 0, 0, 0);
		this.client = client;
		this.handler = handler;
		back = Button.builder(Component.translatable("gui.back"), button -> clickAndWaitForServer(48, 0))
				.size(64, 15)
				.build();
		thirdColumnButton = Button.builder(Component.literal("3rd Column:"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build();
		thirdColumnButton.setTooltip(Tooltip.create(Component.literal("It is recommended to have this enabled, to have more info be displayed!")));
		previousPage = Button.builder(Component.translatable("book.page_button.previous"), button -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build();
		nextPage = Button.builder(Component.translatable("book.page_button.next"), button -> clickAndWaitForServer(53, 0))
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
	public Component getTabTitle() {
		return Component.literal("Widgets");
	}

	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(previousPage);
		consumer.accept(nextPage);
		consumer.accept(thirdColumnButton);
		consumer.accept(widgetsElementList);
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.gameMode == null || this.client.player == null) return;
		client.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.PICKUP, this.client.player);
		waitingForServer = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.gameMode == null || this.client.player == null) return;
		client.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.QUICK_MOVE, this.client.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void updateHandler(@Nullable ChestMenu newHandler) {
		this.handler = newHandler;
		back.visible = handler != null;
		entries.clear();
		listNeedsUpdate = true;
	}

	public void hopper(@Nullable List<String> hopperTooltip) {
		if (hopperTooltip == null) {
			widgetsElementList.setEditingPosition(-1);
			return;
		}
		int start = -1;
		int editing = 1;
		for (int i = 0; i < hopperTooltip.size(); i++) {
			String string = hopperTooltip.get(i);
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
				previousPage.visible = stack.is(Items.ARROW);
				return;
			}
			case 53 -> {
				nextPage.visible = stack.is(Items.ARROW);
				return;
			}
			case 50 -> {
				thirdColumnButton.visible = stack.is(Items.BOOKSHELF) || stack.is(Items.STONE_BUTTON);
				if (thirdColumnButton.visible) {
					if (stack.is(Items.STONE_BUTTON))
						thirdColumnButton.setMessage(Component.literal("Apply to all locations"));
					else if (ItemUtils.getLoreLineIf(stack, s -> s.contains("DISABLED")) == null)
						thirdColumnButton.setMessage(Component.literal("3rd Column: ").append(WidgetsListSlotEntry.ENABLED_TEXT));
					else
						thirdColumnButton.setMessage(Component.literal("3rd Column: ").append(WidgetsListSlotEntry.DISABLED_TEXT));
				}
				return;
			}
		}

		if (stack.isEmpty() || stack.is(Items.BLACK_STAINED_GLASS_PANE)) {
			entries.remove(slot);
			return;
		}

		String lowerCase = stack.getHoverName().getString().trim().toLowerCase(Locale.ENGLISH);
		List<String> lore = stack.skyblocker$getLoreStrings();
		String lastLowerCase = lore.getLast().toLowerCase(Locale.ENGLISH);

		WidgetsListSlotEntry entry;
		if (lowerCase.startsWith("widgets on") || lowerCase.startsWith("widgets in") || lastLowerCase.contains("click to edit") || stack.is(Items.RED_STAINED_GLASS_PANE)) {
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
	public void doLayout(ScreenRectangle tabArea) {
		back.setPosition(16, tabArea.top() + 4);
		widgetsElementList.setY(tabArea.top());
		widgetsElementList.setSize(tabArea.width(), tabArea.height() - 20);
		widgetsElementList.refreshScrollAmount();
		previousPage.setPosition(widgetsElementList.getRowLeft(), widgetsElementList.getBottom() + 4);
		nextPage.setPosition(widgetsElementList.scrollBarX() - 100, widgetsElementList.getBottom() + 4);
		thirdColumnButton.setPosition(widgetsElementList.scrollBarX() + 5, widgetsElementList.getBottom() + 4);
	}

	public boolean shouldShowCustomWidgetEntries() {
		return shouldShowCustomWidgetEntries;
	}

	public void setShouldShowCustomWidgetEntries(boolean shouldShowCustomWidgetEntries) {
		this.shouldShowCustomWidgetEntries = shouldShowCustomWidgetEntries;
	}

	@Override
	public Component getTabExtraNarration() {
		return Component.empty();
	}
}
