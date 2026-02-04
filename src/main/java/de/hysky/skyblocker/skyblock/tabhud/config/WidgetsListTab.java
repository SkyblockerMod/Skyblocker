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

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

// TODO: recommend disabling spacing and enabling wrapping
public class WidgetsListTab implements Tab {
	public static final SystemToast.SystemToastId SYSTEM_TOAST_ID = new SystemToast.SystemToastId(1_000);

	private final WidgetsElementList widgetsElementList;
	private final Button back;
	private final Button previousPage;
	private final Button nextPage;
	private final Button thirdColumnButton;
	private final Button resetButton;
	private final StringWidget waitingForServerText;
	private final Minecraft client;

	private final Int2ObjectMap<WidgetsListSlotEntry> entries = new Int2ObjectOpenHashMap<>();
	private final List<WidgetEntry> customWidgetEntries = new ArrayList<>();

	private @Nullable ChestMenu handler;
	private boolean waitingForServer = false;
	private boolean shouldShowCustomWidgetEntries = false;
	private int resetSlotId = -1;
	private boolean shouldResetScroll = false;

	public void setCustomWidgetEntries(Collection<WidgetEntry> entries) {
		this.customWidgetEntries.clear();
		this.customWidgetEntries.addAll(entries);
		widgetsElementList.updateList();
	}

	public List<WidgetEntry> getCustomWidgetEntries() {
		return customWidgetEntries;
	}

	public ObjectSet<Int2ObjectMap.Entry<WidgetsListSlotEntry>> getEntries() {
		return entries.int2ObjectEntrySet();
	}

	public WidgetsListTab(Minecraft client, @Nullable ChestMenu handler) {
		widgetsElementList = new WidgetsElementList(this, client, 0, 0, 0);
		this.client = client;
		this.handler = handler;
		back = Button.builder(Component.translatable("gui.back"), button -> {
			clickAndWaitForServer(48, 0);
			this.resetScrollOnLoad();
		}).size(64, 15).build();
		widgetsElementList.setBackButton(back);
		thirdColumnButton = Button.builder(Component.literal("3rd Column:"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build();
		thirdColumnButton.setTooltip(Tooltip.create(Component.literal("It is recommended to have this enabled, to have more info be displayed!")));
		previousPage = Button.builder(Component.translatable("book.page_button.previous"), button -> clickAndWaitForServer(45, 0))
				.size(90, 15)
				.build();
		nextPage = Button.builder(Component.translatable("book.page_button.next"), button -> clickAndWaitForServer(53, 0))
				.size(90, 15)
				.build();
		resetButton = Button.builder(Component.literal("Reset"), button -> {
			if (resetSlotId == -1) return;
			clickAndWaitForServer(resetSlotId, 0);
		}).size(60, 15).build();
		waitingForServerText = new StringWidget(Component.literal("Waiting for server..."), client.font);
		waitingForServerText.setWidth(client.font.width(waitingForServerText.getMessage()));
		if (handler == null) {
			back.visible = false;
			previousPage.visible = false;
			nextPage.visible = false;
			thirdColumnButton.visible = false;
			resetButton.visible = false;
			waitingForServerText.visible = false;
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
		consumer.accept(resetButton);
		consumer.accept(widgetsElementList);
		consumer.accept(waitingForServerText);
	}

	public void resetScrollOnLoad() {
		this.shouldResetScroll = true;
	}

	public boolean isWaitingForServer() {
		return waitingForServer;
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.gameMode == null || this.client.player == null) return;
		client.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.PICKUP, this.client.player);
		waitingForServer = true;
		waitingForServerText.visible = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer || handler == null) return;
		if (client.gameMode == null || this.client.player == null) return;
		client.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.QUICK_MOVE, this.client.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> {
			this.waitingForServer = false;
			waitingForServerText.visible = false;
		}, 4);
		waitingForServer = true;
		waitingForServerText.visible = true;
	}

	public void updateHandler(@Nullable ChestMenu newHandler) {
		this.handler = newHandler;
		back.visible = handler != null;
		entries.clear();
		widgetsElementList.updateList();
		resetButton.visible = false;
		if (this.shouldResetScroll) {
			this.shouldResetScroll = false;
			this.widgetsElementList.setScrollAmount(0);
		}
	}

	public void hopper(@Nullable List<String> hopperTooltip) {
		if (hopperTooltip == null) {
			widgetsElementList.setEditingPosition(false, -1, -1);
			return;
		}
		int start = -1;
		int editing = 1;
		int end = -1;

		for (int i = 0; i < hopperTooltip.size(); i++) {
			String string = hopperTooltip.get(i);
			if (string.contains("▶")) {
				if (start == -1) start = i;
				if (i > end) end = i;
			}
			if (string.contains("(EDITING)")) {
				editing = i;
			}
		}
		widgetsElementList.setEditingPosition(true, editing - start, end - start);
	}

	public void onSlotChange(int slot, ItemStack stack) {
		waitingForServer = false;
		waitingForServerText.visible = false;
		widgetsElementList.updateList();
		switch (slot) {
			case 45 -> {
				widgetsElementList.setIsOnSecondPage(previousPage.visible = stack.is(Items.ARROW));
				return;
			}
			case 51, 53 -> {
				if (slot == 53) nextPage.visible = stack.is(Items.ARROW);
				if (stack.is(Items.PLAYER_HEAD)) {
					String stackName = stack.getHoverName().getString().toLowerCase(Locale.ENGLISH);
					if (!stackName.startsWith("reset")) return;
					Component buttonText = Component.literal("Reset ALL").withStyle(style -> style.withColor(ChatFormatting.RED).withUnderlined(true));
					if (slot == 51) {
						buttonText = Component.literal("Reset").withStyle(ChatFormatting.RED);
					}
					resetButton.visible = true;
					resetButton.setMessage(buttonText);
					resetSlotId = slot;
				}
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

		int bottomButtonY = widgetsElementList.getBottom() + 4;
		thirdColumnButton.setPosition((tabArea.width() - thirdColumnButton.getWidth()) / 2, bottomButtonY);
		previousPage.setPosition(thirdColumnButton.getX() - previousPage.getWidth() - 5, bottomButtonY);
		nextPage.setPosition(thirdColumnButton.getRight() + 5, bottomButtonY);
		resetButton.setPosition(tabArea.right() - resetButton.getWidth() - 4, bottomButtonY);
		waitingForServerText.setPosition(tabArea.width() - waitingForServerText.getWidth() - 5, tabArea.height() - client.font.lineHeight - 2);
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
