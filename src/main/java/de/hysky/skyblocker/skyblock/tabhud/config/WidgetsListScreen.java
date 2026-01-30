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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class WidgetsListScreen extends Screen implements ContainerListener {
	public static final SystemToast.SystemToastId SYSTEM_TOAST_ID = new SystemToast.SystemToastId(1_000);
	private static final int PADDING = 8;

	private WidgetsElementList widgetsElementList;
	private Button back;
	private Button previousPage;
	private Button nextPage;
	private Button thirdColumnButton;
	private Button resetButton;
	private StringWidget waitingForServerText;
	private MultiLineTextWidget infoText;

	private String titleLowercase;
	private boolean overflowing = false;
	private boolean previewVisible = false;
	private boolean thirdColumnEnabled = false;

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 25);
	private final FrameLayout headerLayout = new FrameLayout(0, 25);
	@SuppressWarnings("unchecked")
	private final List<Component>[] previewColumns = new List[3];

	private final Int2ObjectMap<WidgetsListSlotEntry> entries = new Int2ObjectOpenHashMap<>();

	private @Nullable ChestMenu handler;
	private boolean waitingForServer = false;
	private int resetSlotId = -1;
	private boolean shouldResetScroll = false;

	public ObjectSet<Int2ObjectMap.Entry<WidgetsListSlotEntry>> getEntries() {
		return entries.int2ObjectEntrySet();
	}

	public WidgetsListScreen(ChestMenu handler, String titleLowercase) {
		super(Component.literal("Widgets"));
		this.handler = handler;
		this.titleLowercase = titleLowercase;
		handler.addSlotListener(this);
		Arrays.fill(previewColumns, List.of());
	}

	public boolean isWaitingForServer() {
		return waitingForServer;
	}

	public void resetScrollOnLoad() {
		this.shouldResetScroll = true;
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.PICKUP, this.minecraft.player);
		// wacky fix for "this action is on cooldown"
		if (slot == 50) Scheduler.INSTANCE.schedule(() -> {
			this.waitingForServer = false;
			waitingForServerText.visible = false;
		}, 4);
		waitingForServer = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, ClickType.QUICK_MOVE, this.minecraft.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> {
			this.waitingForServer = false;
			waitingForServerText.visible = false;
		}, 4);
		waitingForServer = true;
	}

	public void updateHandler(ChestMenu newHandler, String titleLowercase) {
		this.handler.removeSlotListener(this);
		newHandler.addSlotListener(this);
		this.handler = newHandler;
		this.titleLowercase = titleLowercase;
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
					else if (ItemUtils.getLoreLineIf(stack, s -> s.contains("DISABLED")) == null) {
						thirdColumnEnabled = true;
						thirdColumnButton.setMessage(Component.literal("3rd Column: ").append(WidgetsListSlotEntry.ENABLED_TEXT));
						updateInfoText();
					} else {
						thirdColumnEnabled = false;
						thirdColumnButton.setMessage(Component.literal("3rd Column: ").append(WidgetsListSlotEntry.DISABLED_TEXT));
						updateInfoText();
					}
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
		layout.addToHeader(headerLayout);
		widgetsElementList = layout.addToContents(new WidgetsElementList(this, minecraft, 0, 0, 0));
		back = headerLayout.addChild(Button.builder(Component.translatable("gui.back"), button -> {
					clickAndWaitForServer(48, 0);
					this.resetScrollOnLoad();
				})
				.size(60, 15)
				.build(), l -> l.alignHorizontallyLeft().paddingLeft(PADDING));
		thirdColumnButton = headerLayout.addChild(Button.builder(Component.translatable("gui.back"), button -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build(), l -> l.alignHorizontallyRight().paddingRight(PADDING));
		infoText = headerLayout.addChild(new MultiLineTextWidget(Component.empty(), font).setCentered(true), l -> l.paddingVertical(4));
		thirdColumnButton.setTooltip(Tooltip.create(Component.literal("It is recommended to have this enabled, to have more info be displayed!")));
		LinearLayout footer = LinearLayout.horizontal();
		previousPage = footer.addChild(Button.builder(Component.translatable("book.page_button.previous"), button -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build());
		nextPage = footer.addChild(Button.builder(Component.translatable("book.page_button.next"), button -> clickAndWaitForServer(53, 0))
				.size(100, 15)
				.build());
		layout.addToFooter(footer);
		waitingForServerText = new StringWidget(Component.literal("Waiting for server..."), font);
		waitingForServerText.setWidth(font.width(waitingForServerText.getMessage()));
		resetButton = layout.addToFooter(Button.builder(Component.literal("Reset"), button -> {
			if (resetSlotId == -1) return;
			clickAndWaitForServer(resetSlotId, 0);
		}).size(60, 15).build(), l -> l.alignHorizontallyRight().paddingRight(PADDING));
		layout.visitWidgets(this::addRenderableWidget);
		addRenderableWidget(waitingForServerText);
		repositionElements();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.render(guiGraphics, mouseX, mouseY, delta);
		if (minecraft.hasControlDown()) {
			int max = Arrays.stream(previewColumns).flatMap(Collection::stream).mapToInt(font::width).max().orElse(0);
			if (max <= 0) return;
			int colWidth = Math.min(previewColumns.length * max, width - 20) / previewColumns.length;
			int lineCount = 0;
			for (List<Component> column : previewColumns) {
				lineCount = Math.max(lineCount, column.size());
			}
			int columnSpacing = 4;
			int totalWidth = colWidth * previewColumns.length + columnSpacing * (previewColumns.length - 1);
			int totalHeight = lineCount * font.lineHeight;
			int startX = (width - totalWidth) / 2;
			int startY = (height - totalHeight) / 2;

			guiGraphics.fill(startX - 5, startY - 5, startX + totalWidth + 5, startY + totalHeight + 5, 0xA0_00_00_00);
			for (int i = 0; i < previewColumns.length; i++) {
				int colX = startX + i * (colWidth + columnSpacing);
				guiGraphics.fill(colX, startY, colX + colWidth, startY + totalHeight, 0x20FFFFFF);
				List<Component> column = previewColumns[i];
				if (column.isEmpty()) {
					List<FormattedCharSequence> split = font.split(Component.translatable("skyblocker.widgetsList.playerColumn"), colWidth);
					for (int j = 0; j < split.size(); j++) {
						guiGraphics.drawString(font, split.get(j), colX, startY + j * font.lineHeight, -1);
					}
				} else {
					for (int j = 0; j < column.size(); j++) {
						Component component = column.get(j);
						FormattedCharSequence trimmed = font.width(component) >= colWidth ? StringWidget.clipText(component, font, colWidth) : component.getVisualOrderText();
						guiGraphics.drawString(font, trimmed, colX, startY + j * font.lineHeight, -1);
					}
				}
			}
		}
	}

	@Override
	protected void repositionElements() {
		infoText.setMaxWidth(width - thirdColumnButton.getWidth() * 2 - PADDING * 3);
		headerLayout.setMinWidth(width);
		headerLayout.arrangeElements();
		layout.setHeaderHeight(headerLayout.getHeight());
		widgetsElementList.updateSize(width, layout);
		layout.arrangeElements();
		waitingForServerText.setPosition(width - waitingForServerText.getWidth() - 5, height - font.lineHeight - 2);
	}

	@Override
	public void onClose() {
		assert this.minecraft.player != null;
		this.minecraft.player.closeContainer();
		super.onClose();
	}

	@Override
	public void removed() {
		if (this.minecraft.player != null) {
			this.handler.removed(this.minecraft.player);
		}
		handler.removeSlotListener(this);
	}

	@Override
	public void tick() {
		super.tick();
		assert this.minecraft.player != null;
		if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved()) {
			this.minecraft.player.closeContainer();
		}
	}

	private void updateInfoText() {
		if (!previewVisible) {
			infoText.setMessage(CommonComponents.EMPTY);
			repositionElements();
			return;
		}
		MutableComponent text = Component.translatable("skyblocker.widgetsList.info.preview");
		if (overflowing) {
			MutableComponent overflowWarning = Component.translatable("skyblocker.widgetsList.info.overflowWarning").withStyle(ChatFormatting.RED).append("\n");
			if (!thirdColumnEnabled) overflowWarning.append(Component.translatable("skyblocker.widgetsList.info.overflowWarning.columnTip"));
			overflowWarning.append(Component.translatable("skyblocker.widgetsList.info.overflowWarning.wrappingSpacingTip"));
			text.append("\n");
			text.append(overflowWarning);
		}
		infoText.setMessage(text);
		repositionElements();
	}

	@Override
	public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
		if (slotId >= 3 && slotId <= 5) {
			if (!stack.getHoverName().getString().endsWith("Widgets Preview")) {
				previewVisible = false;
				updateInfoText();
				return;
			} else {
				previewVisible = true;
			}
			if (slotId == 5) {
				List<Boolean> list = stack.skyblocker$getLoreStrings().stream()
						.map(s -> s.substring(s.indexOf('⬛') + 1).isBlank())
						.toList();
				overflowing = list.size() >= 2 && !(list.getLast() && list.get(list.size() - 2));
				updateInfoText();
			}
			//noinspection deprecation
			previewColumns[slotId - 3] = ItemUtils.getLore(stack).stream()
					.filter(c -> c.getString().startsWith("⬛"))
					.toList();

		}
		if (slotId == 13) {
			if (stack.is(Items.HOPPER)) {
				hopper(stack.skyblocker$getLoreStrings());
			} else {
				hopper(null);
			}
		}
		if (slotId > (titleLowercase.startsWith("tablist widgets") ? 9 : 18) && slotId < this.handler.getRowCount() * 9 - 9 || slotId == 45 || slotId == 53 || slotId == 50 || slotId == 51) {
			onSlotChange(slotId, stack);
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {

	}
}
