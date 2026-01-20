package de.hysky.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class ShortcutsConfigScreen extends Screen {
	private final Screen parent;
	private ShortcutsConfigListWidget shortcutsConfigListWidget;
	private Button buttonDelete;
	private Button buttonNew;
	private Button buttonDone;
	private boolean initialized;
	private double scrollAmount;

	public ShortcutsConfigScreen() {
		this(null);
	}

	public ShortcutsConfigScreen(Screen parent) {
		super(Component.translatable("skyblocker.shortcuts.config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		if (initialized) {
			shortcutsConfigListWidget.setSize(width, height - 96);
			shortcutsConfigListWidget.updatePositions();
			shortcutsConfigListWidget.refreshScrollAmount();
		} else {
			shortcutsConfigListWidget = new ShortcutsConfigListWidget(minecraft, this, width, height - 96, 32);
			initialized = true;
		}
		addRenderableWidget(shortcutsConfigListWidget);
		GridLayout gridWidget = new GridLayout();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
		buttonDelete = Button.builder(Component.translatable("selectServer.deleteButton"), button -> {
			if (minecraft != null && shortcutsConfigListWidget.getSelected() instanceof ShortcutsConfigListWidget.ShortcutEntry<?> shortcutEntry) {
				scrollAmount = shortcutsConfigListWidget.scrollAmount();
				minecraft.setScreen(new ConfirmScreen(confirmedAction -> deleteEntry(confirmedAction, shortcutEntry), Component.translatable("skyblocker.shortcuts.deleteQuestion"), Component.translatableEscape("skyblocker.shortcuts.deleteWarning", shortcutEntry), Component.translatable("selectServer.deleteButton"), CommonComponents.GUI_CANCEL));
			}
		}).build();
		adder.addChild(buttonDelete);
		buttonNew = Button.builder(Component.translatable("skyblocker.shortcuts.new"), buttonNew -> shortcutsConfigListWidget.addShortcutAfterSelected()).build();
		adder.addChild(buttonNew);
		adder.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).build());
		buttonDone = Button.builder(CommonComponents.GUI_DONE, button -> {
			shortcutsConfigListWidget.saveShortcuts();
			onClose();
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.shortcuts.commandSuggestionTooltip"))).build();
		adder.addChild(buttonDone);
		gridWidget.arrangeElements();
		FrameLayout.centerInRectangle(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.visitWidgets(this::addRenderableWidget);
		updateButtons();
	}

	private void deleteEntry(boolean confirmedAction, ShortcutsConfigListWidget.AbstractShortcutEntry entry) {
		if (minecraft != null) {
			if (confirmedAction && entry instanceof ShortcutsConfigListWidget.ShortcutEntry<?> shortcutEntry) {
				shortcutsConfigListWidget.removeEntry(shortcutEntry);
			}
			minecraft.setScreen(this); // Re-inits the screen and keeps the old instance of ShortcutsConfigListWidget
			shortcutsConfigListWidget.setScrollAmount(scrollAmount);
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredString(this.font, this.title, this.width / 2, 16, CommonColors.WHITE);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (super.mouseClicked(click, doubled)) {
			return true;
		}
		// Only stop editing if super didn't consume the click
		boolean wasEditing = shortcutsConfigListWidget.stopEditing();
		if (wasEditing) {
			shortcutsConfigListWidget.updateKeybinds();
		}
		return wasEditing;
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		// Process ESC before super to prevent closing the screen if we were editing a keybind
		if (input.isEscape() && shortcutsConfigListWidget.stopEditing()) {
			shortcutsConfigListWidget.updateKeybinds();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			if (shortcutsConfigListWidget.hasChanges()) {
				minecraft.setScreen(new ConfirmScreen(confirmedAction -> {
					if (confirmedAction) {
						this.minecraft.setScreen(parent);
					} else {
						minecraft.setScreen(this);
					}
				}, Component.translatable("text.skyblocker.quit_config"), Component.translatable("text.skyblocker.quit_config_sure"), Component.translatable("text.skyblocker.quit_discard"), CommonComponents.GUI_CANCEL));
			} else {
				this.minecraft.setScreen(parent);
			}
		}
	}

	protected void updateButtons() {
		buttonDelete.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getSelected() instanceof ShortcutsConfigListWidget.ShortcutEntry;
		buttonNew.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getCategory().isPresent();
		buttonDone.active = Shortcuts.isShortcutsLoaded();
	}
}
