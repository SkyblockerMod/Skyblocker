package de.hysky.skyblocker.skyblock.chat;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


import java.awt.*;
import java.util.Map;

import static java.util.Map.entry;

public class ChatRuleConfigScreen extends Screen {
    private static final int SPACER_X = 5;
    private static final int SPACER_Y = 25;

    private final Map<MutableText, SoundEvent> soundsLookup = Map.ofEntries(
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.pling"), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.cave"), SoundEvents.AMBIENT_CAVE.value()),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.zombie"), SoundEvents.ENTITY_ZOMBIE_AMBIENT),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.crit"), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.arrowHit"), SoundEvents.ENTITY_ARROW_HIT_PLAYER),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.amethyst"), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT),
            entry(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.anvil"), SoundEvents.BLOCK_ANVIL_LAND)
    );

    private final int chatRuleIndex;
    private final ChatRule chatRule;
    private TextFieldWidget nameInput;
    private TextFieldWidget filterInput;
    private ButtonWidget partialMatchToggle;
    private ButtonWidget regexToggle;
    private ButtonWidget ignoreCaseToggle;
    private TextFieldWidget locationsInput;
    private ButtonWidget hideMessageToggle;
    private ButtonWidget actionBarToggle;
    private ButtonWidget announcementToggle;
    private ButtonWidget soundsToggle;
    private TextFieldWidget replaceMessageInput;

    //textLocations
    private IntIntPair nameLabelTextPos;
    private IntIntPair inputsLabelTextPos;
    private IntIntPair filterLabelTextPos;
    private IntIntPair partialMatchTextPos;
    private IntIntPair regexTextPos;
    private  IntIntPair ignoreCaseTextPos;
    private IntIntPair locationLabelTextPos;
    private IntIntPair outputsLabelTextPos;
    private IntIntPair hideMessageTextPos;
    private IntIntPair actionBarTextPos;
    private IntIntPair announcementTextPos;
    private IntIntPair customSoundLabelTextPos;
    private IntIntPair replaceMessageLabelTextPos;

    private int currentSoundIndex;

    private final Screen parent;

    public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
        super(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen"));
        this.chatRuleIndex = chatRuleIndex;
        this.chatRule = ChatRulesHandler.chatRuleList.get(chatRuleIndex);
        this.parent = parent;
        this.currentSoundIndex = soundsLookup.values().stream().toList().indexOf(chatRule.getCustomSound());
    }

    @Override
    protected void init() {
        super.init();
        if (client == null) return;
        //start centered on the X and 1/3 down on the Y
        IntIntPair currentPos = IntIntPair.of((this.width - getMaxUsedWidth()) / 2,(int)((this.height -getMaxUsedHeight()) * 0.33));
        int lineXOffset;

        nameLabelTextPos = currentPos;
        lineXOffset  = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.name")) + SPACER_X;
        nameInput =  new TextFieldWidget(MinecraftClient.getInstance().textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 100, 20, Text.of(""));
        nameInput.setText(chatRule.getName());
        nameInput.setTooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.name.@Tooltip")));
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        inputsLabelTextPos = currentPos;
        currentPos = IntIntPair.of(currentPos.leftInt() + 10 ,currentPos.rightInt() + SPACER_Y);

        filterLabelTextPos = currentPos;
        lineXOffset = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.filter")) + SPACER_X;
        filterInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""));
        filterInput.setMaxLength(96);
        filterInput.setText(chatRule.getFilter());
        filterInput.setTooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.filter.@Tooltip")));
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);
        lineXOffset = 0;

        partialMatchTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.partialMatch")) + SPACER_X;
        partialMatchToggle = ButtonWidget.builder(enabledButtonText(chatRule.getPartialMatch()), a -> {
                    chatRule.setPartialMatch(!chatRule.getPartialMatch());
                    partialMatchToggle.setMessage(enabledButtonText(chatRule.getPartialMatch()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
                .build();
        lineXOffset += 75 + SPACER_X;
        regexTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.regex")) + SPACER_X;
        regexToggle = ButtonWidget.builder(enabledButtonText(chatRule.getRegex()), a -> {
                    chatRule.setRegex(!chatRule.getRegex());
                    regexToggle.setMessage(enabledButtonText(chatRule.getRegex()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.regex.@Tooltip")))
                .build();
        lineXOffset += 75 + SPACER_X;
        ignoreCaseTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.ignoreCase")) + SPACER_X;
        ignoreCaseToggle = ButtonWidget.builder(enabledButtonText(chatRule.getIgnoreCase()), a -> {
                    chatRule.setIgnoreCase(!chatRule.getIgnoreCase());
                    ignoreCaseToggle.setMessage(enabledButtonText(chatRule.getIgnoreCase()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
                .build();
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        locationLabelTextPos = currentPos;
        lineXOffset = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.locations")) + SPACER_X;
        locationsInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""));
        locationsInput.setText(chatRule.getValidLocations());
        MutableText locationToolTip = Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.locations.@Tooltip");
        ChatRulesHandler.locationsList.forEach(location -> locationToolTip.append(" " + location + ",\n"));
        locationsInput.setTooltip(Tooltip.of(locationToolTip));
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        outputsLabelTextPos = IntIntPair.of(currentPos.leftInt() - 10,currentPos.rightInt());
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        hideMessageTextPos = currentPos;
        lineXOffset = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.hideMessage")) + SPACER_X;
        hideMessageToggle = ButtonWidget.builder(enabledButtonText(chatRule.getHideMessage()), a -> {
                    chatRule.setHideMessage(!chatRule.getHideMessage());
                    hideMessageToggle.setMessage(enabledButtonText(chatRule.getHideMessage()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
                .build();
        lineXOffset += 75 + SPACER_X;
        actionBarTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.actionBar")) + SPACER_X;
        actionBarToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowActionBar()), a -> {
                    chatRule.setShowActionBar(!chatRule.getShowActionBar());
                    actionBarToggle.setMessage(enabledButtonText(chatRule.getShowActionBar()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
                .build();
        lineXOffset = 0;
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        announcementTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.announcement")) + SPACER_X;
        announcementToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowAnnouncement()), a -> {
                    chatRule.setShowAnnouncement(!chatRule.getShowAnnouncement());
                    announcementToggle.setMessage(enabledButtonText(chatRule.getShowAnnouncement()));
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(75,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.announcement.@Tooltip")))
                .build();
        lineXOffset += 75 + SPACER_X;
        customSoundLabelTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset,currentPos.rightInt());
        lineXOffset += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds")) + SPACER_X;
        soundsToggle = ButtonWidget.builder(getSoundName(), a -> {
                currentSoundIndex += 1;
                if (currentSoundIndex == soundsLookup.size()) {
                    currentSoundIndex = -1;
                }
                MutableText newText = getSoundName();
                soundsToggle.setMessage(newText);
                SoundEvent sound = soundsLookup.get(newText);
                chatRule.setCustomSound(sound);
                if (client.player != null && sound != null) {
                    client.player.playSound(sound, 100f, 0.1f);
                }
                })
                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
                .size(100,20)
                .tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sound.@Tooltip")))
                .build();
        currentPos = IntIntPair.of(currentPos.leftInt(),currentPos.rightInt() + SPACER_Y);

        replaceMessageLabelTextPos = currentPos;
        lineXOffset = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.replace")) + SPACER_X;
        replaceMessageInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""));
        replaceMessageInput.setMaxLength(96);
        replaceMessageInput.setTooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.replace.@Tooltip")));
        replaceMessageInput.setText(chatRule.getReplaceMessage());

        ButtonWidget finishButton = ButtonWidget.builder(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.finish"), a -> close())
                .position(this.width - 75 - SPACER_Y, this.height - SPACER_Y)
                .size(75, 20)
                .build();

        addDrawableChild(nameInput);
        addDrawableChild(filterInput);
        addDrawableChild(partialMatchToggle);
        addDrawableChild(regexToggle);
        addDrawableChild(ignoreCaseToggle);
        addDrawableChild(locationsInput);
        addDrawableChild(hideMessageToggle);
        addDrawableChild(actionBarToggle);
        addDrawableChild(announcementToggle);
        addDrawableChild(soundsToggle);
        addDrawableChild(replaceMessageInput);
        addDrawableChild(finishButton);
    }

    /**
     * Works out the width of the maximum line
     * @return the max used width
     */
    private int getMaxUsedWidth() {
        if (client == null) return 0;
        //text
        int total = client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.partialMatch"));
        total += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.regex"));
        total += client.textRenderer.getWidth(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.ignoreCase"));
        //space
        total += SPACER_X * 6;
        //button width
        total += 75 * 3;
        return total;
    }

    /**
     * Works out the height used
     * @return height used by the gui
     */
    private int getMaxUsedHeight() {
        //there are 8 rows so just times the spacer by 8
        return SPACER_Y * 8;
    }

    private Text enabledButtonText(boolean enabled) {
        if (enabled){
            return Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.true").withColor(Color.green.getRGB());
        }else {
            return Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.false").withColor(Color.red.getRGB());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);

        //draw labels ands text
        int yOffset = (SPACER_Y - this.textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.inputs"), inputsLabelTextPos.leftInt(), inputsLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.name"), nameLabelTextPos.leftInt(), nameLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.filter"), filterLabelTextPos.leftInt(), filterLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.partialMatch"), partialMatchTextPos.leftInt(), partialMatchTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.regex"), regexTextPos.leftInt(), regexTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.ignoreCase"), ignoreCaseTextPos.leftInt(), ignoreCaseTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.locations"), locationLabelTextPos.leftInt(), locationLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.outputs"), outputsLabelTextPos.leftInt(), outputsLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.hideMessage"), hideMessageTextPos.leftInt(), hideMessageTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.actionBar"), actionBarTextPos.leftInt(), actionBarTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.announcement"), announcementTextPos.leftInt(), announcementTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds"), customSoundLabelTextPos.leftInt(), customSoundLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.replace"), replaceMessageLabelTextPos.leftInt(), replaceMessageLabelTextPos.rightInt() + yOffset, 0xFFFFFF);
    }

    @Override
    public void close() {
        //todo add checks to see if valid rule e.g. has name
        //and if valid save a
        if (client != null ) {
            save();
            client.setScreen(parent);
        }
    }
    private void save(){
        chatRule.setName(nameInput.getText());
        chatRule.setFilter(filterInput.getText());
        chatRule.setReplaceMessage(replaceMessageInput.getText());
        chatRule.setValidLocations(locationsInput.getText());

        ChatRulesHandler.chatRuleList.set(chatRuleIndex,chatRule);
    }

    private MutableText getSoundName() {
        if (currentSoundIndex == -1){
            return  Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen.ruleScreen.sounds.none");
        }
        return soundsLookup.keySet().stream().toList().get(currentSoundIndex);
    }
}
