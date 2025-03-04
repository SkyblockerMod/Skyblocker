package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ChatRulesConfigScreen extends Screen {

    private ChatRulesConfigListWidget chatRulesConfigListWidget;
    private final Screen parent;

    public ChatRulesConfigScreen(Screen parent) {
        super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        chatRulesConfigListWidget = new ChatRulesConfigListWidget(client, this, width, height - 96, 32, 25);
        addDrawableChild(chatRulesConfigListWidget);
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(3);
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            if (client != null) {
                close();
            }
        }).build());
        ButtonWidget buttonNew1 = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.new"), buttonNew -> chatRulesConfigListWidget.addRuleAfterSelected()).build();
        adder.add(buttonNew1);
        ButtonWidget buttonDone = ButtonWidget.builder(ScreenTexts.DONE, button -> {
            chatRulesConfigListWidget.saveRules();
            if (client != null) {
                close();
            }
        }).build();
        adder.add(buttonDone);
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        if (client != null && chatRulesConfigListWidget.hasChanges()) {
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
