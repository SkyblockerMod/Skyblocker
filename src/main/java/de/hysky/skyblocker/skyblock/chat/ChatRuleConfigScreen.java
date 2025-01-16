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
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class ChatRuleConfigScreen extends Screen {
	private static final int SPACER_X = 5;
	private static final int SPACER_Y = 25;

	private final Map<MutableText, SoundEvent> soundsLookup = Map.ofEntries(
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling"), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave"), SoundEvents.AMBIENT_CAVE.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie"), SoundEvents.ENTITY_ZOMBIE_AMBIENT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit"), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit"), SoundEvents.ENTITY_ARROW_HIT_PLAYER),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst"), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil"), SoundEvents.BLOCK_ANVIL_LAND)
	);

	private int buttonWidth = 75;

	private final int chatRuleIndex;
	private final ChatRule chatRule;
	private final TextFieldWidget nameInput;
	private final TextFieldWidget filterInput;
	private ButtonWidget partialMatchToggle;
	private ButtonWidget regexToggle;
	private ButtonWidget ignoreCaseToggle;
	private final ButtonWidget locationsConfigButton;
	private ButtonWidget hideMessageToggle;
	private ButtonWidget actionBarToggle;
	private ButtonWidget announcementToggle;
	private ButtonWidget soundsToggle;
	private final ButtonWidget finishButton;
	private final TextFieldWidget replaceMessageInput;

	//textLocations
	private final IntIntPair nameLabelTextPos;
	private final IntIntPair inputsLabelTextPos;
	private final IntIntPair filterLabelTextPos;
	private final IntIntPair partialMatchTextPos;
	private final IntIntPair regexTextPos;
	private final IntIntPair ignoreCaseTextPos;
	private final IntIntPair locationLabelTextPos;
	private final IntIntPair outputsLabelTextPos;
	private final IntIntPair hideMessageTextPos;
	private final IntIntPair actionBarTextPos;
	private final IntIntPair announcementTextPos;
	private final IntIntPair customSoundLabelTextPos;
	private final IntIntPair replaceMessageLabelTextPos;

	private int currentSoundIndex;

	private final Screen parent;

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.chatRuleList.get(chatRuleIndex);
		this.parent = parent;
		this.currentSoundIndex = getCurrentSoundIndex();

		//Early initialization of values from the static instance because we want to initialize this stuff in the constructor
		this.client = MinecraftClient.getInstance();
		this.width = client.getWindow().getScaledWidth();
		this.height = client.getWindow().getScaledHeight();

		//start centered on the X and 1/3 down on the Y
		calculateMaxButtonWidth();
		IntIntPair currentPos = IntIntPair.of((this.width - getMaxUsedWidth()) / 2, (int) ((this.height - getMaxUsedHeight()) * 0.33));
		int lineXOffset = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name")) + SPACER_X;
		nameLabelTextPos = currentPos;

		nameInput = new TextFieldWidget(client.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 100, 20, Text.of(""));
		nameInput.setText(chatRule.getName());
		nameInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		inputsLabelTextPos = currentPos;
		currentPos = IntIntPair.of(currentPos.leftInt() + 10, currentPos.rightInt() + SPACER_Y);

		filterLabelTextPos = currentPos;
		lineXOffset = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter")) + SPACER_X;
		filterInput = new TextFieldWidget(client.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""));
		filterInput.setMaxLength(96);
		filterInput.setText(chatRule.getFilter());
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);
		lineXOffset = 0;

		partialMatchTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch")) + SPACER_X;
		partialMatchToggle = ButtonWidget.builder(enabledButtonText(chatRule.getPartialMatch()), a -> {
			                                 chatRule.setPartialMatch(!chatRule.getPartialMatch());
			                                 partialMatchToggle.setMessage(enabledButtonText(chatRule.getPartialMatch()));
		                                 })
		                                 .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                                 .size(buttonWidth, 20)
		                                 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
		                                 .build();
		lineXOffset += buttonWidth + SPACER_X;
		regexTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex")) + SPACER_X;
		regexToggle = ButtonWidget.builder(enabledButtonText(chatRule.getRegex()), a -> {
			                          chatRule.setRegex(!chatRule.getRegex());
			                          regexToggle.setMessage(enabledButtonText(chatRule.getRegex()));
		                          })
		                          .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                          .size(buttonWidth, 20)
		                          .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
		                          .build();
		lineXOffset += buttonWidth + SPACER_X;
		ignoreCaseTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase")) + SPACER_X;
		ignoreCaseToggle = ButtonWidget.builder(enabledButtonText(chatRule.getIgnoreCase()), a -> {
			                               chatRule.setIgnoreCase(!chatRule.getIgnoreCase());
			                               ignoreCaseToggle.setMessage(enabledButtonText(chatRule.getIgnoreCase()));
		                               })
		                               .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                               .size(buttonWidth, 20)
		                               .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
		                               .build();
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		locationLabelTextPos = currentPos;
		lineXOffset = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations")) + SPACER_X;
		locationsConfigButton = ButtonWidget.builder(Text.translatable("text.skyblocker.open"),
				                                    widget -> client.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
		                                    .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
		                                    .dimensions(currentPos.leftInt() + lineXOffset, currentPos.rightInt(), buttonWidth, 20)
		                                    .build();

		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		outputsLabelTextPos = IntIntPair.of(currentPos.leftInt() - 10, currentPos.rightInt());
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		hideMessageTextPos = currentPos;
		lineXOffset = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage")) + SPACER_X;
		hideMessageToggle = ButtonWidget.builder(enabledButtonText(chatRule.getHideMessage()), a -> {
			                                chatRule.setHideMessage(!chatRule.getHideMessage());
			                                hideMessageToggle.setMessage(enabledButtonText(chatRule.getHideMessage()));
		                                })
		                                .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                                .size(buttonWidth, 20)
		                                .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
		                                .build();
		lineXOffset += buttonWidth + SPACER_X;
		actionBarTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar")) + SPACER_X;
		actionBarToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowActionBar()), a -> {
			                              chatRule.setShowActionBar(!chatRule.getShowActionBar());
			                              actionBarToggle.setMessage(enabledButtonText(chatRule.getShowActionBar()));
		                              })
		                              .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                              .size(buttonWidth, 20)
		                              .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
		                              .build();
		lineXOffset = 0;
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		announcementTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement")) + SPACER_X;
		announcementToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowAnnouncement()), a -> {
			                                 chatRule.setShowAnnouncement(!chatRule.getShowAnnouncement());
			                                 announcementToggle.setMessage(enabledButtonText(chatRule.getShowAnnouncement()));
		                                 })
		                                 .position(currentPos.leftInt() + lineXOffset, currentPos.rightInt())
		                                 .size(buttonWidth, 20)
		                                 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")))
		                                 .build();
		lineXOffset += buttonWidth + SPACER_X;
		customSoundLabelTextPos = IntIntPair.of(currentPos.leftInt() + lineXOffset, currentPos.rightInt());
		lineXOffset += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds")) + SPACER_X;
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
		                           .size(buttonWidth, 20)
		                           .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.@Tooltip")))
		                           .build();
		currentPos = IntIntPair.of(currentPos.leftInt(), currentPos.rightInt() + SPACER_Y);

		replaceMessageLabelTextPos = currentPos;
		lineXOffset = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace")) + SPACER_X;
		replaceMessageInput = new TextFieldWidget(client.textRenderer, currentPos.leftInt() + lineXOffset, currentPos.rightInt(), 200, 20, Text.of(""));
		replaceMessageInput.setMaxLength(96);
		replaceMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace.@Tooltip")));
		replaceMessageInput.setText(chatRule.getReplaceMessage());

		finishButton = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.finish"), a -> close())
		                           .position(this.width - buttonWidth - SPACER_Y, this.height - SPACER_Y)
		                           .size(buttonWidth, 20)
		                           .build();
	}

	private int getCurrentSoundIndex() {
		if (chatRule.getCustomSound() == null) return -1; //if no sound just return -1

		List<SoundEvent> soundOptions = soundsLookup.values().stream().toList();
		Identifier ruleSoundId = chatRule.getCustomSound().id();

		for (int i = 0; i < soundOptions.size(); i++) {
			if (soundOptions.get(i).id().compareTo(ruleSoundId) == 0) {
				return i;
			}
		}
		//not found
		return -1;
	}

	@Override
	protected void init() {
		addDrawableChild(nameInput);
		addDrawableChild(filterInput);
		addDrawableChild(partialMatchToggle);
		addDrawableChild(regexToggle);
		addDrawableChild(ignoreCaseToggle);
		addDrawableChild(locationsConfigButton);
		addDrawableChild(hideMessageToggle);
		addDrawableChild(actionBarToggle);
		addDrawableChild(announcementToggle);
		addDrawableChild(soundsToggle);
		addDrawableChild(replaceMessageInput);
		addDrawableChild(finishButton);
	}

	/**
	 * if the maxUsedWidth is above the available width decrease the button width to fix this
	 */
	private void calculateMaxButtonWidth() {
		if (client == null || client.currentScreen == null) return;
		buttonWidth = 75;
		int available = client.currentScreen.width - getMaxUsedWidth() - SPACER_X * 2;
		if (available >= 0) return; //keep the largest size if room
		buttonWidth += available / 3; //remove the needed width from the width of the total 3 buttons
		buttonWidth = Math.max(10, buttonWidth); //do not let the width go below 10
	}

	/**
	 * Works out the width of the maximum line
	 *
	 * @return the max used width
	 */
	private int getMaxUsedWidth() {
		if (client == null) return 0;
		//text
		int total = client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"));
		total += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"));
		total += client.textRenderer.getWidth(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"));
		//space
		total += SPACER_X * 6;
		//button width
		total += buttonWidth * 3;
		return total;
	}

	/**
	 * Works out the height used
	 *
	 * @return height used by the gui
	 */
	private int getMaxUsedHeight() {
		//there are 8 rows so just times the spacer by 8
		return SPACER_Y * 8;
	}

	private Text enabledButtonText(boolean enabled) {
		if (enabled) {
			return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.getRGB());
		} else {
			return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.false").withColor(Color.RED.getRGB());
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFFFF);

		//draw labels ands text
		int yOffset = (SPACER_Y - this.textRenderer.fontHeight) / 2;
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.inputs"), inputsLabelTextPos.leftInt(), inputsLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name"), nameLabelTextPos.leftInt(), nameLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter"), filterLabelTextPos.leftInt(), filterLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"), partialMatchTextPos.leftInt(), partialMatchTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"), regexTextPos.leftInt(), regexTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"), ignoreCaseTextPos.leftInt(), ignoreCaseTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations"), locationLabelTextPos.leftInt(), locationLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs"), outputsLabelTextPos.leftInt(), outputsLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), hideMessageTextPos.leftInt(), hideMessageTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), actionBarTextPos.leftInt(), actionBarTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), announcementTextPos.leftInt(), announcementTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds"), customSoundLabelTextPos.leftInt(), customSoundLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace"), replaceMessageLabelTextPos.leftInt(), replaceMessageLabelTextPos.rightInt() + yOffset, 0xFFFFFFFF);
	}

	/**
	 * Saves and returns to parent screen
	 */
	@Override
	public void close() {
		if (client != null) {
			save();
			client.setScreen(parent);
		}
	}

	private void save() {
		chatRule.setName(nameInput.getText());
		chatRule.setFilter(filterInput.getText());
		chatRule.setReplaceMessage(replaceMessageInput.getText());

		ChatRulesHandler.chatRuleList.set(chatRuleIndex, chatRule);
	}

	private MutableText getSoundName() {
		if (currentSoundIndex == -1) {
			return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none");
		}

		return soundsLookup.keySet().stream().toList().get(currentSoundIndex);
	}
}
