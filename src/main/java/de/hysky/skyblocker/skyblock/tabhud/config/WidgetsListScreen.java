package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.*;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

// TODO: recommend disabling spacing and enabling wrapping
public class WidgetsListScreen extends Screen implements ScreenHandlerListener {
	private WidgetsElementList widgetsElementList;
	private ButtonWidget back;
	private ButtonWidget previousPage;
	private ButtonWidget nextPage;
	private ButtonWidget thirdColumnButton;
	private String titleLowercase;
	private @NotNull GenericContainerScreenHandler handler;
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

	public WidgetsListScreen(@NotNull GenericContainerScreenHandler handler, String titleLowercase) {
		super(Text.literal("Widgets EntryList"));
		this.handler = handler;
		this.titleLowercase = titleLowercase;
		handler.addListener(this);
	}

	@Override
	public Text getTitle() {
		return Text.literal("Widgets");
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (client.interactionManager == null || this.client.player == null) return;
		client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.PICKUP, this.client.player);
		// wacky fix for "this action is on cooldown"
		if (slot == 50) Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (client.interactionManager == null || this.client.player == null) return;
		client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.QUICK_MOVE, this.client.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> this.waitingForServer = false, 4);
		waitingForServer = true;
	}

	public void updateHandler(@NotNull GenericContainerScreenHandler newHandler, String titleLowercase) {
		this.handler.removeListener(this);
		newHandler.addListener(this);
		this.handler = newHandler;
		this.titleLowercase = titleLowercase;
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
	protected void init() {
		super.init();
		widgetsElementList = new WidgetsElementList(this, client, 0, 0, 0);
		back = ButtonWidget.builder(Text.literal("Back"), button -> {clickAndWaitForServer(48, 0);
					System.out.println("aaaaaaaa"); })
				.size(64, 15)
				.build();
		thirdColumnButton = ButtonWidget.builder(Text.literal("3rd Column:"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build();
		thirdColumnButton.setTooltip(Tooltip.of(Text.literal("It is recommended to have this enabled, to have more info be displayed!")));
		previousPage = ButtonWidget.builder(Text.literal("Previous Page"), button -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build();
		nextPage = ButtonWidget.builder(Text.literal("Next Page"), button -> clickAndWaitForServer(53, 0))
				.size(100, 15)
				.build();
		addSelectableChild(back); // element list was blocking the clicks for some reason
		addDrawableChild(widgetsElementList);
		addDrawable(back);
		addDrawableChild(thirdColumnButton);
		addDrawableChild(thirdColumnButton);
		addDrawableChild(previousPage);
		addDrawableChild(nextPage);
		refreshWidgetPositions();
	}

	@Override
	protected void refreshWidgetPositions() {
		back.setPosition(16, 4);
		widgetsElementList.setY(0);
		widgetsElementList.setDimensions(width, height - 20);
		previousPage.setPosition(widgetsElementList.getRowLeft(), widgetsElementList.getBottom() + 4);
		nextPage.setPosition(widgetsElementList.getScrollbarX() - 100, widgetsElementList.getBottom() + 4);
		thirdColumnButton.setPosition(widgetsElementList.getScrollbarX() + 5, widgetsElementList.getBottom() + 4);
	}

	@Override
	public void close() {
		assert this.client != null;
		assert this.client.player != null;
		this.client.player.closeHandledScreen();
		super.close();
	}

	@Override
	public void removed() {
		if (this.client != null && this.client.player != null) {
			this.handler.onClosed(this.client.player);
		}
		handler.removeListener(this);
	}

	@Override
	public void tick() {
		super.tick();
		assert this.client != null;
		assert this.client.player != null;
		if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
			this.client.player.closeHandledScreen();
		}
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
		if (slotId == 13) {
			if (stack.isOf(Items.HOPPER)) {
				hopper(ItemUtils.getLore(stack));
			} else {
				hopper(null);
			}
		}
		if (slotId > (titleLowercase.startsWith("tablist widgets") ? 9 : 18) && slotId < this.handler.getRows() * 9 - 9 || slotId == 45 || slotId == 53 || slotId == 50) {
			onSlotChange(slotId, stack);
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

	}
}
