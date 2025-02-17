package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.WidgetUtils;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
	private static final int BUTTON_HEIGHT = ButtonWidget.DEFAULT_HEIGHT;

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

	// Text widgets
	private final TextWidget nameLabel;
	private final TextWidget inputsLabel;
	private final TextWidget filterLabel;
	private final TextWidget partialMatchLabel;
	private final TextWidget regexLabel;
	private final TextWidget ignoreCaseLabel;
	private final TextWidget locationLabel;
	private final TextWidget outputsLabel;
	private final TextWidget hideMessageLabel;
	private final TextWidget actionBarLabel;
	private final TextWidget announcementLabel;
	private final TextWidget soundsLabel;
	private final TextWidget replaceMessageLabel;
	private final TextWidget titleWidget;

	private int currentSoundIndex;

	private final Screen parent;

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.chatRuleList.get(chatRuleIndex);
		this.parent = parent;
		this.currentSoundIndex = getCurrentSoundIndex();

		// Early initialization of values from the static instance because we want to initialize this stuff in the constructor
		this.client = MinecraftClient.getInstance();
		this.width = client.getWindow().getScaledWidth();
		this.height = client.getWindow().getScaledHeight();

		// Title
		titleWidget = new TextWidget(0, 16, this.width, client.textRenderer.fontHeight, getTitle(), client.textRenderer).alignCenter();

		// Start centered on the X and 1/3 down on the Y
		calculateMaxButtonWidth();
		IntIntPair rootPos = IntIntImmutablePair.of((this.width - getMaxUsedWidth()) / 2, (int) ((this.height - getMaxUsedHeight()) * 0.33));
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		int yOffset = (BUTTON_HEIGHT - client.textRenderer.fontHeight) / 2;

		// Row 1, name
		Text nameText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name");
		nameLabel = textWidget(rootPos, currentPos, yOffset, nameText);
		nextColumn(currentPos, client.textRenderer.getWidth(nameText));

		nameInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), 100, BUTTON_HEIGHT, Text.of(""));
		nameInput.setText(chatRule.getName());
		nameInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));
		nextRow(currentPos);

		// Row 2, inputs header

		inputsLabel = textWidget(rootPos, currentPos, yOffset, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.inputs").formatted(Formatting.BOLD));
		nextRow(currentPos);

		// Row 3, filter

		Text filterText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter");
		filterLabel = textWidget(currentPos, rootPos, yOffset, filterText);
		nextColumn(currentPos, client.textRenderer.getWidth(filterText));
		filterInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), 200, BUTTON_HEIGHT, Text.of(""));
		filterInput.setMaxLength(96);
		filterInput.setText(chatRule.getFilter());
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));
		nextRow(currentPos);

		// Row 4, partial match, regex and ignore case checkboxes.

		Text partialMatchText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch");
		partialMatchLabel = textWidget(rootPos, currentPos, yOffset, partialMatchText);
		nextColumn(currentPos, client.textRenderer.getWidth(partialMatchText));
		partialMatchToggle = ButtonWidget.builder(enabledButtonText(chatRule.getPartialMatch()), a -> {
											 chatRule.setPartialMatch(!chatRule.getPartialMatch());
											 partialMatchToggle.setMessage(enabledButtonText(chatRule.getPartialMatch()));
										 })
										 .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
										 .size(buttonWidth, BUTTON_HEIGHT)
										 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
										 .build();
		nextColumn(currentPos, buttonWidth);

		Text regexLabelText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex");
		regexLabel = textWidget(rootPos, currentPos, yOffset, regexLabelText);
		nextColumn(currentPos, client.textRenderer.getWidth(regexLabelText));
		regexToggle = ButtonWidget.builder(enabledButtonText(chatRule.getRegex()), a -> {
									  chatRule.setRegex(!chatRule.getRegex());
									  regexToggle.setMessage(enabledButtonText(chatRule.getRegex()));
								  })
								  .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
								  .size(buttonWidth, BUTTON_HEIGHT)
								  .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
								  .build();
		nextColumn(currentPos, buttonWidth);

		Text ignoreCaseText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase");
		ignoreCaseLabel = textWidget(rootPos, currentPos, yOffset, ignoreCaseText);
		nextColumn(currentPos, client.textRenderer.getWidth(ignoreCaseText));
		ignoreCaseToggle = ButtonWidget.builder(enabledButtonText(chatRule.getIgnoreCase()), a -> {
										   chatRule.setIgnoreCase(!chatRule.getIgnoreCase());
										   ignoreCaseToggle.setMessage(enabledButtonText(chatRule.getIgnoreCase()));
									   })
									   .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
									   .size(buttonWidth, BUTTON_HEIGHT)
									   .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
									   .build();
		nextRow(currentPos);

		// Row 5, location selection

		Text locationsText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations");
		locationLabel = textWidget(rootPos, currentPos, yOffset, locationsText);
		nextColumn(currentPos, client.textRenderer.getWidth(locationsText));

		locationsConfigButton = ButtonWidget.builder(Text.translatable("text.skyblocker.open"),
													widget -> client.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
											.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
											.dimensions(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), buttonWidth, BUTTON_HEIGHT)
											.build();

		nextRow(currentPos);

		// Row 6, outputs header

		outputsLabel = textWidget(rootPos, currentPos, yOffset, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").formatted(Formatting.BOLD));
		nextRow(currentPos);

		// Row 7, hide message, action bar, announcement checkboxes and sound selection

		Text hideMessageText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage");
		hideMessageLabel = textWidget(rootPos, currentPos, yOffset, hideMessageText);
		nextColumn(currentPos, client.textRenderer.getWidth(hideMessageText));
		hideMessageToggle = ButtonWidget.builder(enabledButtonText(chatRule.getHideMessage()), a -> {
											chatRule.setHideMessage(!chatRule.getHideMessage());
											hideMessageToggle.setMessage(enabledButtonText(chatRule.getHideMessage()));
										})
										.position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
										.size(buttonWidth, BUTTON_HEIGHT)
										.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
										.build();
		nextColumn(currentPos, buttonWidth);

		Text actionBarText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar");
		actionBarLabel = textWidget(rootPos, currentPos, yOffset, actionBarText);
		nextColumn(currentPos, client.textRenderer.getWidth(actionBarText));
		actionBarToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowActionBar()), a -> {
										  chatRule.setShowActionBar(!chatRule.getShowActionBar());
										  actionBarToggle.setMessage(enabledButtonText(chatRule.getShowActionBar()));
									  })
									  .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
									  .size(buttonWidth, BUTTON_HEIGHT)
									  .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
									  .build();
		nextRow(currentPos);

		// Row 8, announcement, sounds and replacement message

		Text announcementText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement");
		announcementLabel = textWidget(rootPos, currentPos, yOffset, announcementText);
		nextColumn(currentPos, client.textRenderer.getWidth(announcementText));
		announcementToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowAnnouncement()), a -> {
											 chatRule.setShowAnnouncement(!chatRule.getShowAnnouncement());
											 announcementToggle.setMessage(enabledButtonText(chatRule.getShowAnnouncement()));
										 })
										 .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
										 .size(buttonWidth, BUTTON_HEIGHT)
										 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")))
										 .build();
		nextColumn(currentPos, buttonWidth);

		Text soundsText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds");
		soundsLabel = textWidget(rootPos, currentPos, yOffset, soundsText);
		nextColumn(currentPos, client.textRenderer.getWidth(soundsText));

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
								   .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
								   .size(buttonWidth, BUTTON_HEIGHT)
								   .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.@Tooltip")))
								   .build();
		nextRow(currentPos);

		// Row 9, replacement message

		Text replaceMessageText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace");
		replaceMessageLabel = textWidget(rootPos, currentPos, yOffset, replaceMessageText);
		nextColumn(currentPos, client.textRenderer.getWidth(replaceMessageText));
		replaceMessageInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), 200, BUTTON_HEIGHT, Text.of(""));
		replaceMessageInput.setMaxLength(96);
		replaceMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace.@Tooltip")));
		replaceMessageInput.setText(chatRule.getReplaceMessage());

		// Finish button at bottom right corner

		finishButton = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.finish"), a -> close())
								   .position(this.width - buttonWidth - SPACER_Y, this.height - SPACER_Y)
								   .size(buttonWidth, BUTTON_HEIGHT)
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
		recalculateWidgetPositions();
		// Title
		addDrawableChild(titleWidget);
		// Row 1
		addDrawableChild(nameInput);
		addDrawableChild(nameLabel);
		// Row 2
		addDrawableChild(inputsLabel);
		// Row 3
		addDrawableChild(filterInput);
		addDrawableChild(filterLabel);
		// Row 4
		addDrawableChild(partialMatchToggle);
		addDrawableChild(partialMatchLabel);
		addDrawableChild(regexToggle);
		addDrawableChild(regexLabel);
		addDrawableChild(ignoreCaseToggle);
		addDrawableChild(ignoreCaseLabel);
		// Row 5
		addDrawableChild(locationsConfigButton);
		addDrawableChild(locationLabel);
		// Row 6
		addDrawableChild(outputsLabel);
		// Row 7
		addDrawableChild(hideMessageToggle);
		addDrawableChild(hideMessageLabel);
		addDrawableChild(actionBarToggle);
		addDrawableChild(actionBarLabel);
		// Row 8
		addDrawableChild(announcementToggle);
		addDrawableChild(announcementLabel);
		addDrawableChild(soundsToggle);
		addDrawableChild(soundsLabel);
		// Row 9
		addDrawableChild(replaceMessageInput);
		addDrawableChild(replaceMessageLabel);
		// Finish button
		addDrawableChild(finishButton);
	}

	private void recalculateWidgetPositions() {
		IntIntPair rootPos = IntIntImmutablePair.of((this.width - getMaxUsedWidth()) / 2, (int) ((this.height - getMaxUsedHeight()) * 0.33));
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		assert client != null;
		int yOffset = (BUTTON_HEIGHT - client.textRenderer.fontHeight) / 2;
		// Title
		titleWidget.setWidth(this.width);
		// Row 1
		setWidgetPosition(nameLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(nameLabel.getMessage()));
		setWidgetPosition(nameInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 2
		setWidgetPosition(inputsLabel, rootPos, currentPos, yOffset);
		nextRow(currentPos);
		// Row 3
		setWidgetPosition(filterLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(filterLabel.getMessage()));
		setWidgetPosition(filterInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 4
		setWidgetPosition(partialMatchLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(partialMatchLabel.getMessage()));
		setWidgetPosition(partialMatchToggle, rootPos, currentPos);
		nextColumn(currentPos, buttonWidth);
		setWidgetPosition(regexLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(regexLabel.getMessage()));
		setWidgetPosition(regexToggle, rootPos, currentPos);
		nextColumn(currentPos, buttonWidth);
		setWidgetPosition(ignoreCaseLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(ignoreCaseLabel.getMessage()));
		setWidgetPosition(ignoreCaseToggle, rootPos, currentPos);
		nextRow(currentPos);
		// Row 5
		setWidgetPosition(locationLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(locationLabel.getMessage()));
		setWidgetPosition(locationsConfigButton, rootPos, currentPos);
		nextRow(currentPos);
		// Row 6
		setWidgetPosition(outputsLabel, rootPos, currentPos, yOffset);
		nextRow(currentPos);
		// Row 7
		setWidgetPosition(hideMessageLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(hideMessageLabel.getMessage()));
		setWidgetPosition(hideMessageToggle, rootPos, currentPos);
		nextColumn(currentPos, buttonWidth);
		setWidgetPosition(actionBarLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(actionBarLabel.getMessage()));
		setWidgetPosition(actionBarToggle, rootPos, currentPos);
		nextRow(currentPos);
		// Row 8
		setWidgetPosition(announcementLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(announcementLabel.getMessage()));
		setWidgetPosition(announcementToggle, rootPos, currentPos);
		nextColumn(currentPos, buttonWidth);
		setWidgetPosition(soundsLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(soundsLabel.getMessage()));
		setWidgetPosition(soundsToggle, rootPos, currentPos);
		nextRow(currentPos);
		// Row 9
		setWidgetPosition(replaceMessageLabel, rootPos, currentPos, yOffset);
		nextColumn(currentPos, client.textRenderer.getWidth(replaceMessageLabel.getMessage()));
		setWidgetPosition(replaceMessageInput, rootPos, currentPos);
		// Finish button
		finishButton.setPosition(this.width - buttonWidth - SPACER_Y, this.height - SPACER_Y);
	}

	/**
	 * Convenience method for creating a text widget with a root position and an offset, and another offset to center the text vertically.
	 *
	 * @param rootPos The root position of the widget within the screen.
	 * @param offset The offset from the root position.
	 * @param yCenterOffset The offset to center the text vertically.
	 * @param text The text to display.
	 * @return A new text widget.
	 */
	private static TextWidget textWidget(IntIntPair rootPos, IntIntPair offset, int yCenterOffset, Text text) {
		return WidgetUtils.textWidget(rootPos.leftInt() + offset.leftInt(), rootPos.rightInt() + offset.rightInt() + yCenterOffset, text);
	}

	/**
	 * Convenience method to set the position of a widget based on the root position and the current position.
	 */
	private static void setWidgetPosition(Widget widget, IntIntPair rootPos, IntIntPair currentPos) {
		widget.setPosition(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt());
	}

	/**
	 * Convenience method to set the position of a widget based on the root position and the current position, with an additional y offset.
	 */
	private static void setWidgetPosition(Widget widget, IntIntPair rootPos, IntIntPair currentPos, int yOffset) {
		widget.setPosition(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt() + yOffset);
	}

	/**
	 * Moves the current position to the right by the given offset + {@link #SPACER_X}. This is used for rendering multiple elements in the same row.
	 */
	private static void nextColumn(IntIntMutablePair pos, int offset) {
		pos.left(pos.leftInt() + offset + SPACER_X);
	}

	/**
	 * Moves the current position down by {@link #SPACER_Y}, to advance to the next row. Also resets the x position to 0, to start back from the left.
	 */
	private static void nextRow(IntIntMutablePair pos) {
		pos.right(pos.rightInt() + SPACER_Y);
		pos.left(0);
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
