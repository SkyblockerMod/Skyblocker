package de.hysky.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ShortcutsConfigScreen extends Screen {

    private ShortcutsConfigListWidget shortcutsConfigListWidget;
    private ButtonWidget buttonDelete;
    private ButtonWidget buttonNew;
    private ButtonWidget buttonDone;
    private boolean initialized;
    private double scrollAmount;
    private final Screen parent;

    public ShortcutsConfigScreen() {
        this(null);
    }

    public ShortcutsConfigScreen(Screen parent) {
        super(Text.translatable("skyblocker.shortcuts.config"));
        this.parent = parent;
    }

    @Override
    public void setTooltip(Text tooltip) {
        super.setTooltip(tooltip);
    }

    @Override
    protected void init() {
        super.init();
        if (initialized) {
            shortcutsConfigListWidget.setDimensions(width, height - 96);
            shortcutsConfigListWidget.updatePositions();
        } else {
            shortcutsConfigListWidget = new ShortcutsConfigListWidget(client, this, width, height - 96, 32, 25);
            initialized = true;
        }
        addDrawableChild(shortcutsConfigListWidget);
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            if (client != null && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry shortcutEntry) {
                scrollAmount = shortcutsConfigListWidget.getScrollAmount();
                client.setScreen(new ConfirmScreen(this::deleteEntry, Text.translatable("skyblocker.shortcuts.deleteQuestion"), Text.stringifiedTranslatable("skyblocker.shortcuts.deleteWarning", shortcutEntry), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
            }
        }).build();
        adder.add(buttonDelete);
        buttonNew = ButtonWidget.builder(Text.translatable("skyblocker.shortcuts.new"), buttonNew -> shortcutsConfigListWidget.addShortcutAfterSelected()).build();
        adder.add(buttonNew);
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            if (client != null) {
                close();
            }
        }).build());
        buttonDone = ButtonWidget.builder(ScreenTexts.DONE, button -> {
            shortcutsConfigListWidget.saveShortcuts();
            if (client != null) {
                close();
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.shortcuts.commandSuggestionTooltip"))).build();
        adder.add(buttonDone);
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
        updateButtons();
    }

    private void deleteEntry(boolean confirmedAction) {
        if (client != null) {
            if (confirmedAction && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry shortcutEntry) {
                shortcutsConfigListWidget.removeEntry(shortcutEntry);
            }
            client.setScreen(this); // Re-inits the screen and keeps the old instance of ShortcutsConfigListWidget
            shortcutsConfigListWidget.setScrollAmount(scrollAmount);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
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
