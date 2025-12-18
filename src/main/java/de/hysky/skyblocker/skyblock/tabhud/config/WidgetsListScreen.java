package de.hysky.skyblocker.skyblock.tabhud.config;

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
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

// TODO: recommend disabling spacing and enabling wrapping
public class WidgetsListScreen extends Screen implements ContainerListener {
	private WidgetsElementList widgetsElementList;
	private Button back;
	private Button previousPage;
	private Button nextPage;
	private Button thirdColumnButton;
	private String titleLowercase;
	private ChestMenu handler;
	private boolean waitingForServer = false;

	private final Int2ObjectMap<WidgetsListSlotEntry> entries = new Int2ObjectOpenHashMap<>();
	private boolean listNeedsUpdate = false;

	public boolean listNeedsUpdate() {
		boolean b = listNeedsUpdate;
		listNeedsUpdate = false;
		return b;
	}

	public ObjectSet<Int2ObjectMap.Entry<WidgetsListSlotEntry>> getEntries() {
		return entries.int2ObjectEntrySet();
	}

	public WidgetsListScreen(ChestMenu handler, String titleLowercase) {
		super(Component.literal("Widgets EntryList"));
		this.handler = handler;
		this.titleLowercase = titleLowercase;
		handler.addSlotListener(this);
	}

	@Override
	public Component getTitle() {
		return Component.literal("Widgets");
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.PICKUP, this.minecraft.player);
		// wacky fix for "this action is on cooldown"
		if (slot == 50) Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.QUICK_MOVE, this.minecraft.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void updateHandler(ChestMenu newHandler, String titleLowercase) {
		this.handler.removeSlotListener(this);
		newHandler.addSlotListener(this);
		this.handler = newHandler;
		this.titleLowercase = titleLowercase;
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
	protected void init() {
		super.init();
		widgetsElementList = new WidgetsElementList(this, minecraft, 0, 0, 0);
		back = Button.builder(Component.literal("Back"), button -> clickAndWaitForServer(48, 0))
				.size(64, 15)
				.build();
		thirdColumnButton = Button.builder(Component.translatable("gui.back"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build();
		thirdColumnButton.setTooltip(Tooltip.create(Component.literal("It is recommended to have this enabled, to have more info be displayed!")));
		previousPage = Button.builder(Component.translatable("book.page_button.previous"), button -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build();
		nextPage = Button.builder(Component.translatable("book.page_button.next"), button -> clickAndWaitForServer(53, 0))
				.size(100, 15)
				.build();
		addWidget(back); // element list was blocking the clicks for some reason
		addRenderableWidget(widgetsElementList);
		addRenderableOnly(back);
		addRenderableWidget(thirdColumnButton);
		addRenderableWidget(thirdColumnButton);
		addRenderableWidget(previousPage);
		addRenderableWidget(nextPage);
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		back.setPosition(16, 4);
		widgetsElementList.setY(0);
		widgetsElementList.setSize(width, height - 20);
		widgetsElementList.refreshScrollAmount();
		previousPage.setPosition(widgetsElementList.getRowLeft(), widgetsElementList.getBottom() + 4);
		nextPage.setPosition(widgetsElementList.scrollBarX() - 100, widgetsElementList.getBottom() + 4);
		thirdColumnButton.setPosition(widgetsElementList.scrollBarX() + 5, widgetsElementList.getBottom() + 4);
	}

	@Override
	public void onClose() {
		assert this.minecraft != null;
		assert this.minecraft.player != null;
		this.minecraft.player.closeContainer();
		super.onClose();
	}

	@Override
	public void removed() {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.handler.removed(this.minecraft.player);
		}
		handler.removeSlotListener(this);
	}

	@Override
	public void tick() {
		super.tick();
		assert this.minecraft != null;
		assert this.minecraft.player != null;
		if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved()) {
			this.minecraft.player.closeContainer();
		}
	}

	@Override
	public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
		if (slotId == 13) {
			if (stack.is(Items.HOPPER)) {
				hopper(stack.skyblocker$getLoreStrings());
			} else {
				hopper(null);
			}
		}
		if (slotId > (titleLowercase.startsWith("tablist widgets") ? 9 : 18) && slotId < this.handler.getRowCount() * 9 - 9 || slotId == 45 || slotId == 53 || slotId == 50) {
			onSlotChange(slotId, stack);
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {

	}
}
