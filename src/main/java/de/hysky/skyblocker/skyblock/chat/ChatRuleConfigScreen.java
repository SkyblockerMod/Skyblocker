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
	private static final int SPACER_Y = 5;

	private final Map<MutableText, SoundEvent> soundsLookup = Map.ofEntries(
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling"), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave"), SoundEvents.AMBIENT_CAVE.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie"), SoundEvents.ENTITY_ZOMBIE_AMBIENT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit"), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit"), SoundEvents.ENTITY_ARROW_HIT_PLAYER),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst"), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil"), SoundEvents.BLOCK_ANVIL_LAND)
	);

	private static final int MAX_WIDTH = 360;
	private static final int BUTTON_WIDTH = 75; // Placeholder for all buttons except the finish button, the others are calculated dynamically but still need an initial value
	private static final int ROW_HEIGHT = ButtonWidget.DEFAULT_HEIGHT;
	private static final int Y_OFFSET = (ROW_HEIGHT - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;

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
		this.chatRule = ChatRulesHandler.chatRuleList.getData().get(chatRuleIndex);
		this.parent = parent;
		this.currentSoundIndex = getCurrentSoundIndex();

		// Early initialization of values from the static instance because we want to initialize this stuff in the constructor
		this.client = MinecraftClient.getInstance();
		this.width = client.getWindow().getScaledWidth();
		this.height = client.getWindow().getScaledHeight();

		// Title
		titleWidget = new TextWidget(getTitle(), client.textRenderer);
		titleWidget.setPosition((width - titleWidget.getWidth()) / 2, 16);

		// Start centered
		IntIntPair rootPos = getRootPos();
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		int yOffset = (ROW_HEIGHT - client.textRenderer.fontHeight) / 2;

		// Row 1, name
		Text nameText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name");
		nameLabel = textWidget(rootPos, currentPos, yOffset, nameText);
		nextColumn(currentPos, client.textRenderer.getWidth(nameText));

		int textFieldWidth = 200; // Placeholder value, their size is calculated dynamically afterward
		nameInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Text.of(""));
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
		filterInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Text.of(""));
		filterInput.setMaxLength(256);
		filterInput.setText(chatRule.getFilter());
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));
		nextRow(currentPos);

		// Row 4, partial match and regex

		Text partialMatchText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch");
		partialMatchLabel = textWidget(rootPos, currentPos, yOffset, partialMatchText);
		nextColumn(currentPos, client.textRenderer.getWidth(partialMatchText));
		partialMatchToggle = ButtonWidget.builder(enabledButtonText(chatRule.getPartialMatch()), a -> {
											 chatRule.setPartialMatch(!chatRule.getPartialMatch());
											 partialMatchToggle.setMessage(enabledButtonText(chatRule.getPartialMatch()));
										 })
										 .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
										 .size(BUTTON_WIDTH, ROW_HEIGHT)
										 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
										 .build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Text regexLabelText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex");
		regexLabel = textWidget(rootPos, currentPos, yOffset, regexLabelText);
		nextColumn(currentPos, client.textRenderer.getWidth(regexLabelText));
		regexToggle = ButtonWidget.builder(enabledButtonText(chatRule.getRegex()), a -> {
									  chatRule.setRegex(!chatRule.getRegex());
									  regexToggle.setMessage(enabledButtonText(chatRule.getRegex()));
								  })
								  .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
								  .size(BUTTON_WIDTH, ROW_HEIGHT)
								  .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
								  .build();
		nextRow(currentPos);

		// Row 5, ignore case and location selection
		Text ignoreCaseText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase");
		ignoreCaseLabel = textWidget(rootPos, currentPos, yOffset, ignoreCaseText);
		nextColumn(currentPos, client.textRenderer.getWidth(ignoreCaseText));
		ignoreCaseToggle = ButtonWidget.builder(enabledButtonText(chatRule.getIgnoreCase()), a -> {
										   chatRule.setIgnoreCase(!chatRule.getIgnoreCase());
										   ignoreCaseToggle.setMessage(enabledButtonText(chatRule.getIgnoreCase()));
									   })
									   .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
									   .size(BUTTON_WIDTH, ROW_HEIGHT)
									   .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
									   .build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Text locationsText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations");
		locationLabel = textWidget(rootPos, currentPos, yOffset, locationsText);
		nextColumn(currentPos, client.textRenderer.getWidth(locationsText));

		locationsConfigButton = ButtonWidget.builder(Text.translatable("text.skyblocker.open"),
													widget -> client.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
											.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
											.dimensions(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), BUTTON_WIDTH, ROW_HEIGHT)
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
										.size(BUTTON_WIDTH, ROW_HEIGHT)
										.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
										.build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Text actionBarText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar");
		actionBarLabel = textWidget(rootPos, currentPos, yOffset, actionBarText);
		nextColumn(currentPos, client.textRenderer.getWidth(actionBarText));
		actionBarToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowActionBar()), a -> {
										  chatRule.setShowActionBar(!chatRule.getShowActionBar());
										  actionBarToggle.setMessage(enabledButtonText(chatRule.getShowActionBar()));
									  })
									  .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
									  .size(BUTTON_WIDTH, ROW_HEIGHT)
									  .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
									  .build();
		nextRow(currentPos);

		// Row 8, announcement, sounds

		Text announcementText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement");
		announcementLabel = textWidget(rootPos, currentPos, yOffset, announcementText);
		nextColumn(currentPos, client.textRenderer.getWidth(announcementText));
		announcementToggle = ButtonWidget.builder(enabledButtonText(chatRule.getShowAnnouncement()), a -> {
											 chatRule.setShowAnnouncement(!chatRule.getShowAnnouncement());
											 announcementToggle.setMessage(enabledButtonText(chatRule.getShowAnnouncement()));
										 })
										 .position(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
										 .size(BUTTON_WIDTH, ROW_HEIGHT)
										 .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")))
										 .build();
		nextColumn(currentPos, BUTTON_WIDTH);

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
								   .size(BUTTON_WIDTH, ROW_HEIGHT)
								   .tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.@Tooltip")))
								   .build();
		nextRow(currentPos);

		// Row 9, replacement message

		Text replaceMessageText = Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace");
		replaceMessageLabel = textWidget(rootPos, currentPos, yOffset, replaceMessageText);
		nextColumn(currentPos, client.textRenderer.getWidth(replaceMessageText));
		replaceMessageInput = new TextFieldWidget(client.textRenderer, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Text.of(""));
		replaceMessageInput.setMaxLength(96);
		replaceMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace.@Tooltip")));
		replaceMessageInput.setText(chatRule.getReplaceMessage());

		// Finish button at bottom right corner

		finishButton = ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.finish"), a -> close())
								   .position(this.width - BUTTON_WIDTH - SPACER_Y, this.height - SPACER_Y)
								   .size(BUTTON_WIDTH, ROW_HEIGHT)
								   .build();
		calculateTextFieldWidths();
		calculateButtonWidths();
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
		// Row 5
		addDrawableChild(ignoreCaseToggle);
		addDrawableChild(ignoreCaseLabel);
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
		calculateTextFieldWidths();
		calculateButtonWidths();
	}

	private void recalculateWidgetPositions() {
		IntIntPair rootPos = getRootPos();
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		assert client != null;
		// Title
		titleWidget.setX((width - titleWidget.getWidth()) / 2);
		// Row 1
		setWidgetPosition(nameLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(nameLabel.getMessage()));
		setWidgetPosition(nameInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 2
		setWidgetPosition(inputsLabel, rootPos, currentPos, Y_OFFSET);
		nextRow(currentPos);
		// Row 3
		setWidgetPosition(filterLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(filterLabel.getMessage()));
		setWidgetPosition(filterInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 4
		setWidgetPosition(partialMatchLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(partialMatchLabel.getMessage()));
		setWidgetPosition(partialMatchToggle, rootPos, currentPos);
		partialMatchToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(regexLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(regexLabel.getMessage()));
		setWidgetPosition(regexToggle, rootPos, currentPos);
		regexToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 5
		setWidgetPosition(ignoreCaseLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(ignoreCaseLabel.getMessage()));
		setWidgetPosition(ignoreCaseToggle, rootPos, currentPos);
		ignoreCaseToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(locationLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(locationLabel.getMessage()));
		setWidgetPosition(locationsConfigButton, rootPos, currentPos);
		locationsConfigButton.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 6
		setWidgetPosition(outputsLabel, rootPos, currentPos, Y_OFFSET);
		nextRow(currentPos);
		// Row 7
		setWidgetPosition(hideMessageLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(hideMessageLabel.getMessage()));
		setWidgetPosition(hideMessageToggle, rootPos, currentPos);
		hideMessageToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(actionBarLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(actionBarLabel.getMessage()));
		setWidgetPosition(actionBarToggle, rootPos, currentPos);
		actionBarToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 8
		setWidgetPosition(announcementLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(announcementLabel.getMessage()));
		setWidgetPosition(announcementToggle, rootPos, currentPos);
		announcementToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(soundsLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(soundsLabel.getMessage()));
		setWidgetPosition(soundsToggle, rootPos, currentPos);
		soundsToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 9
		setWidgetPosition(replaceMessageLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, client.textRenderer.getWidth(replaceMessageLabel.getMessage()));
		setWidgetPosition(replaceMessageInput, rootPos, currentPos);
		// Finish button
		finishButton.setPosition(this.width - BUTTON_WIDTH - SPACER_X, this.height - SPACER_Y - ROW_HEIGHT);
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
	 * Convenience method to set the position of a widget based on the root position and the current position, with an additional y offset, mainly for text widgets.
	 */
	@SuppressWarnings("SameParameterValue") // We can't just inline the parameter value since it causes method signature conflicts. This is fine.
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
	 * Moves the current position down by {@link #SPACER_Y} + {@link #ROW_HEIGHT}, to advance to the next row. Also resets the x position to 0, to start back from the left.
	 */
	private static void nextRow(IntIntMutablePair pos) {
		pos.right(pos.rightInt() + SPACER_Y + ROW_HEIGHT);
		pos.left(0);
	}

	/**
	 * Dynamically calculates the width of the text fields based on the width of the labels.
	 */
	private void calculateTextFieldWidths() {
		calculateTextFieldWidth(nameLabel, nameInput);
		calculateTextFieldWidth(filterLabel, filterInput);
		calculateTextFieldWidth(replaceMessageLabel, replaceMessageInput);
	}

	private IntIntImmutablePair getRootPos() {
		return IntIntImmutablePair.of((this.width - getMaxWidth()) / 2 + SPACER_X, (this.height - getMaxUsedHeight()) / 2);
	}

	private int getMaxWidth() {
		return Math.min(width, MAX_WIDTH);
	}

	/**
	 *
	 * @param textWidget The labeling text widget
	 * @param textFieldWidget The text field widget to calculate the width for
	 */
	private void calculateTextFieldWidth(TextWidget textWidget, TextFieldWidget textFieldWidget) {
		assert client != null;
		int textWidth = client.textRenderer.getWidth(textWidget.getMessage());
		textFieldWidget.setWidth(getMaxWidth() - textWidth - SPACER_X * 3); // 1 inner, 2 outer paddings
	}

	/**
	 * Dynamically calculates the width of the buttons based on the width of the labels.
	 */
	private void calculateButtonWidths() {
		calculateButtonWidth(partialMatchLabel, partialMatchToggle, regexLabel, regexToggle);
		calculateButtonWidth(ignoreCaseLabel, ignoreCaseToggle, locationLabel, locationsConfigButton);
		calculateButtonWidth(hideMessageLabel, hideMessageToggle, actionBarLabel, actionBarToggle);
		calculateButtonWidth(announcementLabel, announcementToggle, soundsLabel, soundsToggle);
	}

	/**
	 * Calculates button widths for a row of buttons. Each row has 2 buttons and 2 labels, so this method needs 4 parameters.
	 *
	 * @param label1 The first label
	 * @param button1 The first button
	 * @param label2 The second label
	 * @param button2 The second button
	 */
	private void calculateButtonWidth(TextWidget label1, ButtonWidget button1, TextWidget label2, ButtonWidget button2) {
		assert client != null;
		int label1Width = client.textRenderer.getWidth(label1.getMessage());
		int label2Width = client.textRenderer.getWidth(label2.getMessage());
		int remainingWidth = getMaxWidth() - label1Width - label2Width - SPACER_X * 5; // 3 inner, 2 outer paddings
		int buttonWidth = remainingWidth / 2;
		button1.setWidth(buttonWidth);
		button2.setWidth(buttonWidth + remainingWidth % 2); // Add the remainder to the second button if there's any
		int label2x = ((this.width - getMaxWidth()) / 2) + SPACER_X + label1Width + SPACER_X + buttonWidth + SPACER_X;
		label2.setX(label2x); // Reposition the second label to the right of the first button
		button2.setX(label2x + label2Width + SPACER_X); // Reposition the second button to the right of the second label
	}

	/**
	 * Works out the height used
	 *
	 * @return height used by the gui
	 */
	private int getMaxUsedHeight() {
		// 9 rows, and there's a spacer between each row
		// Since this isn't used to make anything fit, we don't need to care about the top and bottom outer padding or the finish button. This is just used to center the gui contents on the screen.
		return ROW_HEIGHT * 9 + SPACER_Y * 8;
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

		ChatRulesHandler.chatRuleList.getData().set(chatRuleIndex, chatRule);
	}

	private MutableText getSoundName() {
		if (currentSoundIndex == -1) {
			return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none");
		}

		return soundsLookup.keySet().stream().toList().get(currentSoundIndex);
	}
}
