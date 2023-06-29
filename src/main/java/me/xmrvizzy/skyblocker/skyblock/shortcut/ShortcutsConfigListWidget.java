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

import java.util.List;

public class ShortcutsConfigListWidget extends ElementListWidget<ShortcutsConfigListWidget.AbstractShortcutEntry> {
    public ShortcutsConfigListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        ShortcutCategoryEntry commandCategory = new ShortcutCategoryEntry("skyblocker.shortcuts.targetCommand", "skyblocker.shortcuts.replacementCommand");
        addEntry(commandCategory);
        if (!Shortcuts.isShortcutsLoaded()) {
            addEntry(new ShortcutLoadingEntry());
        } else {
            Shortcuts.commands.keySet().stream().sorted().forEach(commandTarget -> addEntry(new ShortcutEntry(commandTarget, Shortcuts.commands.get(commandTarget), commandCategory)));
        }
        ShortcutCategoryEntry commandArgCategory = new ShortcutCategoryEntry("skyblocekr.shortcuts.targetCommandArg", "skyblocker.shortcuts.replacementCommandArg");
        addEntry(commandArgCategory);
        if (!Shortcuts.isShortcutsLoaded()) {
            addEntry(new ShortcutLoadingEntry());
        } else {
            Shortcuts.commandArgs.keySet().stream().sorted().forEach(commandArgTarget -> addEntry(new ShortcutEntry(commandArgTarget, Shortcuts.commandArgs.get(commandArgTarget), commandArgCategory)));
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

    protected abstract static class AbstractShortcutEntry extends ElementListWidget.Entry<AbstractShortcutEntry> {
    }

    private class ShortcutCategoryEntry extends AbstractShortcutEntry {
        private final Text targetName;
        private final Text replacementName;

        private ShortcutCategoryEntry(String targetName, String replacementName) {
            this.targetName = Text.translatable(targetName);
            this.replacementName = Text.translatable(replacementName);
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
        }
    }

    private class ShortcutLoadingEntry extends AbstractShortcutEntry {
        private final Text text;

        private ShortcutLoadingEntry() {
            this.text = Text.of("§c§lShortcuts not loaded yet");
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

    private class ShortcutEntry extends AbstractShortcutEntry {
        private final List<TextFieldWidget> children;
        private final TextFieldWidget target;
        private final TextFieldWidget replacement;

        private ShortcutEntry(String target, String replacement, ShortcutCategoryEntry category) {
            this.target = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 160, 5, 150, 20, category.targetName);
            this.replacement = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 + 10, 5, 150, 20, category.replacementName);
            this.target.setText(target);
            this.replacement.setText(replacement);
            children = List.of(this.target, this.replacement);
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
