package de.hysky.skyblocker.skyblock.tabhud.config.list;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot.BooleanSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot.DefaultSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot.EditableSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot.WidgetSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot.WidgetsListSlotEntry;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class WidgetsListScreen extends Screen implements ContainerListener {
	public static boolean overrideWidgetsScreen = false;
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

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 25);
	private final FrameLayout headerLayout = new FrameLayout(0, 25);
	@SuppressWarnings("unchecked")
	private final List<Component>[] previewColumns = new List[3];

	private final Int2ObjectMap<WidgetsListSlotEntry> entries = new Int2ObjectOpenHashMap<>();

	private ChestMenu handler;
	private boolean waitingForServer = false;
	private int resetSlotId = -1;
	private boolean shouldResetScroll = false;

	private String titleLowercase;
	private boolean overflowing = false;
	private boolean previewVisible = false;
	private boolean thirdColumnEnabled = false;

	public ObjectSet<Int2ObjectMap.Entry<WidgetsListSlotEntry>> getEntries() {
		return entries.int2ObjectEntrySet();
	}

	public WidgetsListScreen(ChestMenu handler, Component name) {
		super(name);
		widgetsElementList = new WidgetsElementList(this, minecraft, 0, 0, 0);
		this.handler = handler;
		titleLowercase = name.getString().toLowerCase(Locale.ENGLISH);
		this.handler.addSlotListener(this);
	}

	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
			dispatcher.register(ClientCommands.literal(SkyblockerMod.NAMESPACE)
					.then(ClientCommands.literal("hypixelWidgets").executes(_ -> {
						if (Utils.isOnSkyblock()) {
							overrideWidgetsScreen = true;
							MessageScheduler.INSTANCE.sendMessageAfterCooldown("/widgets", true);
						}
						return Command.SINGLE_SUCCESS;
					})));
		});
	}

	@Override
	protected void init() {
		super.init();
		layout.addToHeader(headerLayout);
		widgetsElementList = layout.addToContents(new WidgetsElementList(this, minecraft, 0, 0, 0));
		back = headerLayout.addChild(Button.builder(Component.translatable("gui.back"), _ -> {
					clickAndWaitForServer(48, 0);
					this.resetScrollOnLoad();
				})
				.size(60, 15)
				.build(), l -> l.alignHorizontallyLeft().paddingLeft(PADDING));
		thirdColumnButton = headerLayout.addChild(Button.builder(Component.literal("if you are seeing this for more than a second, something went very wrong! (no i will not translate this aaron)"), _ -> clickAndWaitForServer(50, 0))
				.size(120, 15)
				.build(), l -> l.alignHorizontallyRight().paddingRight(PADDING));
		infoText = headerLayout.addChild(new MultiLineTextWidget(Component.empty(), font).setCentered(true), l -> l.paddingVertical(4));
		thirdColumnButton.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.hud.widgetsList.thirdColumn.@Tooltip")));
		LinearLayout footer = LinearLayout.horizontal().spacing(10);
		previousPage = footer.addChild(Button.builder(Component.translatable("book.page_button.previous"), _ -> clickAndWaitForServer(45, 0))
				.size(100, 15)
				.build());
		nextPage = footer.addChild(Button.builder(Component.translatable("book.page_button.next"), _ -> clickAndWaitForServer(53, 0))
				.size(100, 15)
				.build());
		layout.addToFooter(footer);
		waitingForServerText = new StringWidget(Component.translatable("skyblocker.config.hud.widgetsList.waitingForServer"), font);
		waitingForServerText.setWidth(font.width(waitingForServerText.getMessage()));
		resetButton = layout.addToFooter(Button.builder(Component.translatable("skyblocker.config.hud.widgetsList.reset"), _ -> {
			if (resetSlotId == -1) return;
			clickAndWaitForServer(resetSlotId, 0);
		}).size(60, 15).build(), l -> l.alignHorizontallyRight().paddingRight(PADDING));
		layout.visitWidgets(this::addRenderableWidget);
		addRenderableWidget(waitingForServerText);
		repositionElements();
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

	public void resetScrollOnLoad() {
		this.shouldResetScroll = true;
	}

	public boolean isWaitingForServer() {
		return waitingForServer;
	}

	public void clickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleContainerInput(handler.containerId, slot, button, ContainerInput.PICKUP, this.minecraft.player);
		waitingForServer = true;
		waitingForServerText.visible = true;
	}

	public void shiftClickAndWaitForServer(int slot, int button) {
		if (waitingForServer) return;
		if (minecraft.gameMode == null || this.minecraft.player == null) return;
		minecraft.gameMode.handleContainerInput(handler.containerId, slot, button, ContainerInput.QUICK_MOVE, this.minecraft.player);
		// When moving a widget down it gets stuck sometimes
		Scheduler.INSTANCE.schedule(() -> {
			this.waitingForServer = false;
			waitingForServerText.visible = false;
		}, 4);
		waitingForServer = true;
		waitingForServerText.visible = true;
	}

	public void updateHandler(ChestMenu newHandler, Component name) {
		titleLowercase = name.getString().toLowerCase(Locale.ENGLISH);
		this.handler.removeSlotListener(this);
		newHandler.addSlotListener(this);
		this.handler = newHandler;
		back.visible = true;
		entries.clear();
		widgetsElementList.updateList();
		resetButton.visible = false;
		if (this.shouldResetScroll) {
			this.shouldResetScroll = false;
			this.widgetsElementList.setScrollAmount(0);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
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

			graphics.fill(startX - 5, startY - 5, startX + totalWidth + 5, startY + totalHeight + 5, 0xA0_00_00_00);
			for (int i = 0; i < previewColumns.length; i++) {
				int colX = startX + i * (colWidth + columnSpacing);
				graphics.fill(colX, startY, colX + colWidth, startY + totalHeight, 0x20FFFFFF);
				List<Component> column = previewColumns[i];
				if (column.isEmpty()) {
					List<FormattedCharSequence> split = font.split(Component.translatable("skyblocker.config.hud.widgetsList.playerColumn"), colWidth);
					for (int j = 0; j < split.size(); j++) {
						graphics.text(font, split.get(j), colX, startY + j * font.lineHeight, -1);
					}
				} else {
					for (int j = 0; j < column.size(); j++) {
						Component component = column.get(j);
						FormattedCharSequence trimmed = font.width(component) >= colWidth ? ComponentRenderUtils.clipText(component, font, colWidth) : component.getVisualOrderText();
						graphics.text(font, trimmed, colX, startY + j * font.lineHeight, -1);
					}
				}
			}
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
						buttonText = Component.translatable("text.skyblocker.reset").withStyle(ChatFormatting.RED);
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
						thirdColumnButton.setMessage(Component.translatable("skyblocker.config.hud.widgetsList.applyToAllLocations"));
					else if (ItemUtils.getLoreLineIf(stack, s -> s.contains("DISABLED")) == null) {
						thirdColumnEnabled = true;
						thirdColumnButton.setMessage(Component.translatable("skyblocker.config.hud.widgetsList.thirdColumn", WidgetsListSlotEntry.ENABLED_TEXT));
						updateInfoText();
					} else {
						thirdColumnEnabled = false;
						thirdColumnButton.setMessage(Component.translatable("skyblocker.config.hud.widgetsList.thirdColumn", WidgetsListSlotEntry.DISABLED_TEXT));
						updateInfoText();
					}
				}
				return;
			}
		}

		if (stack.isEmpty() || stack.is(Items.STAINED_GLASS_PANE.black())) {
			entries.remove(slot);
			return;
		}

		String lowerCase = stack.getHoverName().getString().trim().toLowerCase(Locale.ENGLISH);
		List<String> lore = stack.skyblocker$getLoreStrings();
		String lastLowerCase = lore.getLast().toLowerCase(Locale.ENGLISH);

		WidgetsListSlotEntry entry;
		if (lowerCase.startsWith("widgets on") || lowerCase.startsWith("widgets in") || lastLowerCase.contains("click to edit") || stack.is(Items.STAINED_GLASS_PANE.red())) {
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

	private void updateInfoText() {
		if (!previewVisible) {
			infoText.setMessage(CommonComponents.EMPTY);
			repositionElements();
			return;
		}
		MutableComponent text = Component.translatable("skyblocker.config.hud.widgetsList.info.preview");
		if (overflowing) {
			MutableComponent overflowWarning = Component.translatable("skyblocker.config.hud.widgetsList.info.overflowWarning").withStyle(ChatFormatting.RED).append("\n");
			if (!thirdColumnEnabled) overflowWarning.append(Component.translatable("skyblocker.config.hud.widgetsList.info.overflowWarning.columnTip")).append("\n");
			overflowWarning.append(Component.translatable("skyblocker.config.hud.widgetsList.info.overflowWarning.wrappingSpacingTip"));
			text.append("\n");
			text.append(overflowWarning);
		}
		infoText.setMessage(text);
		repositionElements();
	}

	@Override
	public void removed() {
		if (this.minecraft.player != null) this.handler.removed(this.minecraft.player);
		this.handler.removeSlotListener(this);
	}

	@Override
	public void onClose() {
		if (this.minecraft.player != null) this.minecraft.player.closeContainer();
		overrideWidgetsScreen = false;
	}

	@Override
	public void dataChanged(AbstractContainerMenu container, int id, int value) {}
}
