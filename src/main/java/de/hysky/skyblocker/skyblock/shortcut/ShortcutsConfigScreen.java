package de.hysky.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class ShortcutsConfigScreen extends Screen {
	private final Screen parent;
	private ShortcutsConfigListWidget shortcutsConfigListWidget;
	private ButtonWidget buttonDelete;
	private ButtonWidget buttonNew;
	private ButtonWidget buttonDone;
	private boolean initialized;
	private double scrollAmount;

	public ShortcutsConfigScreen() {
		this(null);
	}

	public ShortcutsConfigScreen(Screen parent) {
		super(Text.translatable("skyblocker.shortcuts.config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		if (initialized) {
			shortcutsConfigListWidget.setDimensions(width, height - 96);
			shortcutsConfigListWidget.updatePositions();
		} else {
			shortcutsConfigListWidget = new ShortcutsConfigListWidget(client, this, width, height - 96, 32, 24);
			initialized = true;
		}
		addDrawableChild(shortcutsConfigListWidget);
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(2);
		buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), button -> {
			if (client != null && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry<?> shortcutEntry) {
				scrollAmount = shortcutsConfigListWidget.getScrollY();
				client.setScreen(new ConfirmScreen(this::deleteEntry, Text.translatable("skyblocker.shortcuts.deleteQuestion"), Text.stringifiedTranslatable("skyblocker.shortcuts.deleteWarning", shortcutEntry), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
			}
		}).build();
		adder.add(buttonDelete);
		buttonNew = ButtonWidget.builder(Text.translatable("skyblocker.shortcuts.new"), buttonNew -> shortcutsConfigListWidget.addShortcutAfterSelected()).build();
		adder.add(buttonNew);
		adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()).build());
		buttonDone = ButtonWidget.builder(ScreenTexts.DONE, button -> {
			shortcutsConfigListWidget.saveShortcuts();
			close();
		}).tooltip(Tooltip.of(Text.translatable("skyblocker.shortcuts.commandSuggestionTooltip"))).build();
		adder.add(buttonDone);
		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);
		updateButtons();
	}

	private void deleteEntry(boolean confirmedAction) {
		if (client != null) {
			if (confirmedAction && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry<?> shortcutEntry) {
				shortcutsConfigListWidget.removeEntry(shortcutEntry);
			}
			client.setScreen(this); // Re-inits the screen and keeps the old instance of ShortcutsConfigListWidget
			shortcutsConfigListWidget.setScrollY(scrollAmount);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// Process ESC before super to prevent closing the screen if we were editing a keybind
		if (keyCode == InputUtil.GLFW_KEY_ESCAPE && shortcutsConfigListWidget.stopEditing()) {
			shortcutsConfigListWidget.updateKeybinds();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		if (client != null) {
			if (shortcutsConfigListWidget.hasChanges()) {
				client.setScreen(new ConfirmScreen(confirmedAction -> {
					if (confirmedAction) {
						this.client.setScreen(parent);
					} else {
						client.setScreen(this);
					}
				}, Text.translatable("text.skyblocker.quit_config"), Text.translatable("text.skyblocker.quit_config_sure"), Text.translatable("text.skyblocker.quit_discard"), ScreenTexts.CANCEL));
			} else {
				this.client.setScreen(parent);
			}
		}
	}

	protected void updateButtons() {
		buttonDelete.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry;
		buttonNew.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getCategory().isPresent();
		buttonDone.active = Shortcuts.isShortcutsLoaded();
	}
}
