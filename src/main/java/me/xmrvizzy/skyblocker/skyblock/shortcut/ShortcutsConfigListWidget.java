package me.xmrvizzy.skyblocker.skyblock.shortcut;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShortcutsConfigListWidget extends ElementListWidget<ShortcutsConfigListWidget.AbstractShortcutEntry> {
    private final ShortcutsConfigScreen screen;
    protected final List<ShortcutCategoryEntry> categories;

    public ShortcutsConfigListWidget(MinecraftClient minecraftClient, ShortcutsConfigScreen screen, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.screen = screen;
        ShortcutCategoryEntry commandCategory = new ShortcutCategoryEntry("skyblocker.shortcuts.command.target", "skyblocker.shortcuts.command.replacement");
        addEntry(commandCategory);
        if (!Shortcuts.isShortcutsLoaded()) {
            addEntry(new ShortcutLoadingEntry());
        } else {
            Shortcuts.commands.keySet().stream().sorted().forEach(commandTarget -> addEntry(new ShortcutEntry(commandCategory, commandTarget, Shortcuts.commands.get(commandTarget))));
        }
        ShortcutCategoryEntry commandArgCategory = new ShortcutCategoryEntry("skyblocker.shortcuts.commandArg.target", "skyblocker.shortcuts.commandArg.replacement", "skyblocker.shortcuts.commandArg.tooltip");
        addEntry(commandArgCategory);
        if (!Shortcuts.isShortcutsLoaded()) {
            addEntry(new ShortcutLoadingEntry());
        } else {
            Shortcuts.commandArgs.keySet().stream().sorted().forEach(commandArgTarget -> addEntry(new ShortcutEntry(commandArgCategory, commandArgTarget, Shortcuts.commandArgs.get(commandArgTarget))));
        }
        categories = List.of(commandCategory, commandArgCategory);
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 100;
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 50;
    }

    protected Optional<ShortcutCategoryEntry> getCategory() {
        if (getSelectedOrNull() instanceof ShortcutCategoryEntry category) {
            return Optional.of(category);
        } else if (getSelectedOrNull() instanceof ShortcutEntry shortcutEntry) {
            return Optional.of(shortcutEntry.category);
        }
        return Optional.empty();
    }

    protected Map<String, String> getShortcutsMap(ShortcutCategoryEntry category) {
        return switch (categories.indexOf(category)) {
            case 0 -> Shortcuts.commands;
            case 1 -> Shortcuts.commandArgs;
            default -> throw new IllegalStateException("Unexpected category: " + category);
        };
    }

    @Override
    public void setSelected(@Nullable ShortcutsConfigListWidget.AbstractShortcutEntry entry) {
        super.setSelected(entry);
        screen.updateButtons();
    }

    protected void addShortcutAfterSelected() {
        getCategory().ifPresent(category -> children().add(children().indexOf(getSelectedOrNull()) + 1, new ShortcutEntry(category)));
    }

    @Override
    protected boolean removeEntry(AbstractShortcutEntry entry) {
        return super.removeEntry(entry);
    }

    protected void saveShortcuts() {
        for (ShortcutCategoryEntry category : categories) {
            getShortcutsMap(category).clear();
        }
        for (AbstractShortcutEntry entry : children()) {
            if (entry instanceof ShortcutEntry shortcutEntry && !shortcutEntry.target.getText().isEmpty() && !shortcutEntry.replacement.getText().isEmpty()) {
                getShortcutsMap(shortcutEntry.category).put(shortcutEntry.target.getText(), shortcutEntry.replacement.getText());
            }
        }
        Shortcuts.saveShortcuts(MinecraftClient.getInstance()); // Save shortcuts to disk
    }

    protected static abstract class AbstractShortcutEntry extends ElementListWidget.Entry<AbstractShortcutEntry> {
    }

    protected class ShortcutCategoryEntry extends AbstractShortcutEntry {
        private final Text targetName;
        private final Text replacementName;
        @Nullable
        private final Text tooltip;

        private ShortcutCategoryEntry(String targetName, String replacementName) {
            this(targetName, replacementName, (Text) null);
        }

        private ShortcutCategoryEntry(String targetName, String replacementName, String tooltip) {
            this(targetName, replacementName, Text.translatable(tooltip));
        }

        private ShortcutCategoryEntry(String targetName, String replacementName, @Nullable Text tooltip) {
            this.targetName = Text.translatable(targetName);
            this.replacementName = Text.translatable(replacementName);
            this.tooltip = tooltip;
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(new Selectable() {
                @Override
                public SelectionType getType() {
                    return SelectionType.HOVERED;
                }

                @Override
                public void appendNarrations(NarrationMessageBuilder builder) {
                    builder.put(NarrationPart.TITLE, targetName, replacementName);
                }
            });
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(client.textRenderer, targetName, width / 2 - 85, y + 5, 0xFFFFFF);
            context.drawCenteredTextWithShadow(client.textRenderer, replacementName, width / 2 + 85, y + 5, 0xFFFFFF);
            if (tooltip != null && isMouseOver(mouseX, mouseY)) {
                screen.setTooltip(tooltip);
            }
        }
    }

    private class ShortcutLoadingEntry extends AbstractShortcutEntry {
        private final Text text;

        private ShortcutLoadingEntry() {
            this.text = Text.translatable("skyblocker.shortcuts.notLoaded");
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(new Selectable() {
                @Override
                public SelectionType getType() {
                    return SelectionType.HOVERED;
                }

                @Override
                public void appendNarrations(NarrationMessageBuilder builder) {
                    builder.put(NarrationPart.TITLE, text);
                }
            });
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(client.textRenderer, text, width / 2, y + 5, 0xFFFFFF);
        }
    }

    protected class ShortcutEntry extends AbstractShortcutEntry {
        private final List<TextFieldWidget> children;
        protected final ShortcutCategoryEntry category;
        protected final TextFieldWidget target;
        protected final TextFieldWidget replacement;

        protected ShortcutEntry(ShortcutCategoryEntry category) {
            this(category, "", "");
        }

        private ShortcutEntry(ShortcutCategoryEntry category, String targetString, String replacementString) {
            this.category = category;
            target = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 160, 5, 150, 20, category.targetName);
            replacement = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 + 10, 5, 150, 20, category.replacementName);
            target.setText(targetString);
            replacement.setText(replacementString);
            children = List.of(target, replacement);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            target.setY(y);
            replacement.setY(y);
            target.render(context, mouseX, mouseY, tickDelta);
            replacement.render(context, mouseX, mouseY, tickDelta);
            context.drawCenteredTextWithShadow(client.textRenderer, "→", width / 2, y + 5, 0xFFFFFF);
        }
    }
}
