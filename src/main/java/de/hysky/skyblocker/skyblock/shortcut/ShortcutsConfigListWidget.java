package de.hysky.skyblocker.skyblock.shortcut;

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

import java.util.*;
import java.util.stream.Stream;

public class ShortcutsConfigListWidget extends ElementListWidget<ShortcutsConfigListWidget.AbstractShortcutEntry> {
    private final ShortcutsConfigScreen screen;
    private final List<Map<String, String>> shortcutMaps = new ArrayList<>();

    /**
     * @param width the width of the widget
     * @param height the height of the widget
     * @param y the y coordinate to start rendering/placing the widget from
     * @param itemHeight the height of each item
     */
    public ShortcutsConfigListWidget(MinecraftClient minecraftClient, ShortcutsConfigScreen screen, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
        this.screen = screen;
        ShortcutCategoryEntry commandCategory = new ShortcutCategoryEntry(Shortcuts.commands, "skyblocker.shortcuts.command.target", "skyblocker.shortcuts.command.replacement");
        if (Shortcuts.isShortcutsLoaded()) {
            commandCategory.shortcutsMap.keySet().stream().sorted().forEach(commandTarget -> addEntry(new ShortcutEntry(commandCategory, commandTarget)));
        } else {
            addEntry(new ShortcutLoadingEntry());
        }
        ShortcutCategoryEntry commandArgCategory = new ShortcutCategoryEntry(Shortcuts.commandArgs, "skyblocker.shortcuts.commandArg.target", "skyblocker.shortcuts.commandArg.replacement", "skyblocker.shortcuts.commandArg.tooltip");
        if (Shortcuts.isShortcutsLoaded()) {
            commandArgCategory.shortcutsMap.keySet().stream().sorted().forEach(commandArgTarget -> addEntry(new ShortcutEntry(commandArgCategory, commandArgTarget)));
        } else {
            addEntry(new ShortcutLoadingEntry());
        }
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

    @Override
    public void setSelected(@Nullable ShortcutsConfigListWidget.AbstractShortcutEntry entry) {
        super.setSelected(entry);
        screen.updateButtons();
    }

    protected void addShortcutAfterSelected() {
        getCategory().ifPresent(category -> children().add(children().indexOf(getSelectedOrNull()) + 1, new ShortcutEntry(category)));
    }

    protected void updatePositions() {
        for (AbstractShortcutEntry child : children()) {
            child.updatePositions();
        }
    }

    @Override
    protected boolean removeEntry(AbstractShortcutEntry entry) {
        return super.removeEntry(entry);
    }

    protected boolean hasChanges() {
        ShortcutEntry[] notEmptyShortcuts = getNotEmptyShortcuts().toArray(ShortcutEntry[]::new);
        return notEmptyShortcuts.length != shortcutMaps.stream().mapToInt(Map::size).sum() || Arrays.stream(notEmptyShortcuts).anyMatch(ShortcutEntry::isChanged);
    }

    protected void saveShortcuts() {
        shortcutMaps.forEach(Map::clear);
        getNotEmptyShortcuts().forEach(ShortcutEntry::save);
        Shortcuts.saveShortcuts(MinecraftClient.getInstance()); // Save shortcuts to disk
    }

    private Stream<ShortcutEntry> getNotEmptyShortcuts() {
        return children().stream().filter(ShortcutEntry.class::isInstance).map(ShortcutEntry.class::cast).filter(ShortcutEntry::isNotEmpty);
    }

    public static abstract class AbstractShortcutEntry extends ElementListWidget.Entry<AbstractShortcutEntry> {
        protected void updatePositions() {}
    }

    protected class ShortcutCategoryEntry extends AbstractShortcutEntry {
        private final Map<String, String> shortcutsMap;
        private final Text targetName;
        private final Text replacementName;
        @Nullable
        private final Text tooltip;

        private ShortcutCategoryEntry(Map<String, String> shortcutsMap, String targetName, String replacementName) {
            this(shortcutsMap, targetName, replacementName, (Text) null);
        }

        private ShortcutCategoryEntry(Map<String, String> shortcutsMap, String targetName, String replacementName, String tooltip) {
            this(shortcutsMap, targetName, replacementName, Text.translatable(tooltip));
        }

        private ShortcutCategoryEntry(Map<String, String> shortcutsMap, String targetName, String replacementName, @Nullable Text tooltip) {
            this.shortcutsMap = shortcutsMap;
            this.targetName = Text.translatable(targetName);
            this.replacementName = Text.translatable(replacementName);
            this.tooltip = tooltip;
            shortcutMaps.add(shortcutsMap);
            addEntry(this);
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
        private final ShortcutCategoryEntry category;
        private final TextFieldWidget target;
        private final TextFieldWidget replacement;

        private ShortcutEntry(ShortcutCategoryEntry category) {
            this(category, "");
        }

        private ShortcutEntry(ShortcutCategoryEntry category, String targetString) {
            this.category = category;
            target = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 160, 5, 150, 20, category.targetName);
            replacement = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 + 10, 5, 150, 20, category.replacementName);
            target.setMaxLength(48);
            replacement.setMaxLength(48);
            target.setText(targetString);
            replacement.setText(category.shortcutsMap.getOrDefault(targetString, ""));
            children = List.of(target, replacement);
        }

        @Override
        public String toString() {
            return target.getText() + " → " + replacement.getText();
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children;
        }

        private boolean isNotEmpty() {
            return !target.getText().isEmpty() && !replacement.getText().isEmpty();
        }

        private boolean isChanged() {
            return !category.shortcutsMap.containsKey(target.getText()) || !category.shortcutsMap.get(target.getText()).equals(replacement.getText());
        }

        private void save() {
            category.shortcutsMap.put(target.getText(), replacement.getText());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            target.setY(y);
            replacement.setY(y);
            target.render(context, mouseX, mouseY, tickDelta);
            replacement.render(context, mouseX, mouseY, tickDelta);
            context.drawCenteredTextWithShadow(client.textRenderer, "→", width / 2, y + 5, 0xFFFFFF);
        }

        @Override
        protected void updatePositions() {
            super.updatePositions();
            target.setX(width / 2 - 160);
            replacement.setX(width / 2 + 10);
        }
    }
}
