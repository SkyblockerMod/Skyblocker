package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class ChatRulesConfigListWidget extends ElementListWidget<ChatRulesConfigListWidget.chatRuleConfigEntry> {


    private final ChatRulesConfigScreen screen;

    private Boolean hasChanged;

    public ChatRulesConfigListWidget(MinecraftClient minecraftClient, ChatRulesConfigScreen screen, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
        this.screen = screen;
        this.hasChanged = false;
        //add entry fall all existing rules
        for (int i = 0; i < (long) ChatRulesHandler.chatRuleList.size(); i++){
            addEntry(new chatRuleConfigEntry(i));
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

    @Override
    public void setSelected(@Nullable ChatRulesConfigListWidget.chatRuleConfigEntry entry) {
        super.setSelected(entry);
        screen.updateButtons();
    }

    protected void addRuleAfterSelected() {
        hasChanged = true;
        int newIndex = children().indexOf(getSelectedOrNull()) + 1;
        ChatRulesHandler.chatRuleList.add(newIndex, new ChatRule());
        children().add(newIndex, new chatRuleConfigEntry(newIndex));
    }

    protected boolean removeEntry(chatRuleConfigEntry entry) {
        hasChanged = true;
        return super.removeEntry(entry);
    }


    protected void saveRules() {
        hasChanged = false;
        ChatRulesHandler.saveChatRules();
    }


    protected boolean hasChanges(){
        return (hasChanged || children().stream().anyMatch(chatRuleConfigEntry::hasChange));
    }

    public class chatRuleConfigEntry extends Entry<chatRuleConfigEntry> {

        private static final int SPACING = 20;

        //data
        private int chatRuleIndex;
        private ChatRule chatRule;



        private final List<? extends Element> children;

        //widgets
        private final ButtonWidget enabledWidget;
        private final ButtonWidget openConfigWidget;

        //text locations
        private final int labelX;
        private final int enabledX;


        public chatRuleConfigEntry(int chatRuleIndex) {
            this.chatRuleIndex = chatRuleIndex;
            this.chatRule = ChatRulesHandler.chatRuleList.get(chatRuleIndex);

            //initialize the widgets
            int currentX  = width / 2 - 160;
            labelX = currentX;
            currentX += client.textRenderer.getWidth("Rule: \"" + chatRule.getName() + "\"");
            currentX += SPACING; //spacer

            enabledX = currentX;
            currentX += client.textRenderer.getWidth("Enabled:");
            enabledWidget = ButtonWidget.builder(enabledButtonText() , a -> {
                toggleEnabled();
            })
                    .size(50,20)
                    .position(currentX,5)
                    .build()
            ;
            currentX += 50;
            currentX += SPACING; //spacer

            openConfigWidget = ButtonWidget.builder(Text.of("Edit Rule"), a -> {
                        client.setScreen(new ChatRuleConfigScreen(screen, chatRuleIndex));
            })
                    .size(100,20)
                    .position(currentX,5)
                    .build()
            ;

            children = List.of(enabledWidget, openConfigWidget);
        }

        private Text enabledButtonText() {
            if (chatRule.getEnabled()){
                return Text.literal("TRUE").withColor(Color.green.getRGB());
            }else {
                return Text.literal("FALSE").withColor(Color.red.getRGB());
            }
        }
        private void toggleEnabled() {
            hasChanged = true;
            chatRule.setEnabled(!chatRule.getEnabled());
            enabledWidget.setMessage(enabledButtonText());
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
                    builder.put(NarrationPart.TITLE); //todo add more e.g. , targetName, replacementName
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
            enabledWidget.setY(y);
            enabledWidget.render(context, mouseX, mouseY, tickDelta);
            openConfigWidget.setY(y);
            openConfigWidget.render(context, mouseX, mouseY, tickDelta);
            //text
            context.drawTextWithShadow(client.textRenderer, "Rule: \"" + chatRule.getName() + "\"", labelX, y + 5, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, "enabled:", enabledX, y + 5, 0xFFFFFF);
        }

        public boolean hasChange() {
            return (!chatRule.getEnabled().equals(ChatRulesHandler.chatRuleList.get(chatRuleIndex).getEnabled()));
        }
    }
}
