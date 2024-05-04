package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

public class ChatRulesConfigListWidget extends ElementListWidget<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
    private final ChatRulesConfigScreen screen;
    private Boolean hasChanged;

    public ChatRulesConfigListWidget(MinecraftClient client, ChatRulesConfigScreen screen, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.screen = screen;
        this.hasChanged = false;

        //add labels
        addEntry(new ChatRuleLabelsEntry());
        //add entry fall all existing rules
        for (int i = 0; i < ChatRulesHandler.chatRuleList.size(); i++){
            addEntry(new ChatRuleConfigEntry(i));
        }
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 100;
    }

    @Override
    protected int getScrollbarX() {
        return super.getScrollbarX() + 50;
    }

    protected void addRuleAfterSelected() {
        hasChanged = true;
        int newIndex = children().indexOf(getSelectedOrNull()) + 1;

        ChatRulesHandler.chatRuleList.add(newIndex, new ChatRule());
        children().add(newIndex + 1, new ChatRuleConfigEntry(newIndex));
    }

    protected boolean removeEntry(AbstractChatRuleEntry entry) {
        hasChanged = true;
        return super.removeEntry(entry);
    }

    protected void saveRules() {
        hasChanged = false;
        ChatRulesHandler.saveChatRules();
    }

    protected boolean hasChanges() {
        return (hasChanged || children().stream().filter(ChatRuleConfigEntry.class::isInstance).map(ChatRuleConfigEntry.class::cast).anyMatch(ChatRuleConfigEntry::isChange));
    }

    protected static abstract class AbstractChatRuleEntry extends ElementListWidget.Entry<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
    }

    private class ChatRuleLabelsEntry extends AbstractChatRuleEntry {

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleName"), width / 2 - 125, y + 5, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleEnabled"), width / 2, y + 5, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.modify"), width / 2 + 100, y + 5, 0xFFFFFFFF);
        }
    }

    private class ChatRuleConfigEntry extends AbstractChatRuleEntry {
        //data
        private final int chatRuleIndex;
        private final ChatRule chatRule;

        private final List<? extends Element> children;

        //widgets
        private final ButtonWidget enabledButton;
        private final ButtonWidget openConfigButton;
        private final ButtonWidget deleteButton;

        //text location
        private final int nameX = width / 2 - 125;
        //saved data
        private double oldScrollAmount = 0;


        public ChatRuleConfigEntry(int chatRuleIndex) {
            this.chatRuleIndex = chatRuleIndex;
            this.chatRule = ChatRulesHandler.chatRuleList.get(chatRuleIndex);

            enabledButton = ButtonWidget.builder(enabledButtonText(), a -> toggleEnabled())
                    .size(50, 20)
                    .position(width / 2 - 25, 5)
                    .build();

            openConfigButton = ButtonWidget.builder(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.editRule"), a -> {
                        client.setScreen(new ChatRuleConfigScreen(screen, chatRuleIndex));
                    })
                    .size(50, 20)
                    .position(width / 2 + 45, 5)
                    .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.editRule.@Tooltip")))
                    .build();

            deleteButton = ButtonWidget.builder(Text.translatable("selectServer.delete"), a -> {
                        oldScrollAmount = getScrollAmount();
                        client.setScreen(new ConfirmScreen(this::deleteEntry, Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.deleteQuestion"), Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.deleteWarning", chatRule.getName()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
                    })
                    .size(50, 20)
                    .position(width / 2 + 105, 5)
                    .build();

            children = List.of(enabledButton, openConfigButton, deleteButton);
        }

        private Text enabledButtonText() {
            if (chatRule.getEnabled()) {
                return Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.getRGB());
            } else {
                return Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.false").withColor(Color.RED.getRGB());
            }
        }
        private void toggleEnabled() {
            hasChanged = true;
            chatRule.setEnabled(!chatRule.getEnabled());
            enabledButton.setMessage(enabledButtonText());
        }

        private void deleteEntry(boolean confirmedAction) {
            if (confirmedAction) {
                //delete this
                ChatRulesHandler.chatRuleList.remove(chatRuleIndex);
                removeEntry(this);
            }

            client.setScreen(screen);
            setScrollAmount(oldScrollAmount);
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
                    builder.put(NarrationPart.TITLE,chatRule.getName());
                }
            });
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) { //todo get strings form en_us.json
            //widgets
            enabledButton.setY(y);
            enabledButton.render(context, mouseX, mouseY, tickDelta);
            openConfigButton.setY(y);
            openConfigButton.render(context, mouseX, mouseY, tickDelta);
            deleteButton.setY(y);
            deleteButton.render(context, mouseX, mouseY, tickDelta);
            //text
            context.drawCenteredTextWithShadow(client.textRenderer, chatRule.getName(), nameX, y + 5, 0xFFFFFFFF);
        }

        public boolean isChange() {
            return (!chatRule.getEnabled().equals(ChatRulesHandler.chatRuleList.get(chatRuleIndex).getEnabled()));
        }
    }
}
