package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.WidgetUtils;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import static java.util.Map.entry;

import java.awt.Color;

public class ChatRuleConfigScreen extends Screen {
	private static final int SPACER_X = 5;
	private static final int SPACER_Y = 5;

	private final Map<MutableComponent, SoundEvent> soundsLookup = Map.ofEntries(
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling"), SoundEvents.NOTE_BLOCK_PLING.value()),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave"), SoundEvents.AMBIENT_CAVE.value()),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie"), SoundEvents.ZOMBIE_AMBIENT),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit"), SoundEvents.PLAYER_ATTACK_CRIT),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit"), SoundEvents.ARROW_HIT_PLAYER),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst"), SoundEvents.AMETHYST_BLOCK_HIT),
			entry(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil"), SoundEvents.ANVIL_LAND)
	);

	private static final int MAX_WIDTH = 360;
	private static final int BUTTON_WIDTH = 75; // Placeholder for all buttons except the finish button, the others are calculated dynamically but still need an initial value
	private static final int ROW_HEIGHT = Button.DEFAULT_HEIGHT;
	private static final int Y_OFFSET = (ROW_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2;

	private final int chatRuleIndex;
	private final ChatRule chatRule;
	private final EditBox nameInput;
	private final EditBox filterInput;
	private Button partialMatchToggle;
	private Button regexToggle;
	private Button ignoreCaseToggle;
	private final Button locationsConfigButton;
	private Button hideMessageToggle;
	private Button actionBarToggle;
	private Button announcementToggle;
	private Button soundsToggle;
	private final Button finishButton;
	private final EditBox replaceMessageInput;

	// Text widgets
	private final StringWidget nameLabel;
	private final StringWidget inputsLabel;
	private final StringWidget filterLabel;
	private final StringWidget partialMatchLabel;
	private final StringWidget regexLabel;
	private final StringWidget ignoreCaseLabel;
	private final StringWidget locationLabel;
	private final StringWidget outputsLabel;
	private final StringWidget hideMessageLabel;
	private final StringWidget actionBarLabel;
	private final StringWidget announcementLabel;
	private final StringWidget soundsLabel;
	private final StringWidget replaceMessageLabel;
	private final StringWidget titleWidget;

	private int currentSoundIndex;

	private final Screen parent;

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.chatRuleList.getData().get(chatRuleIndex);
		this.parent = parent;
		this.currentSoundIndex = getCurrentSoundIndex();

		// Early initialization of values from the static instance because we want to initialize this stuff in the constructor
		this.width = minecraft.getWindow().getGuiScaledWidth();
		this.height = minecraft.getWindow().getGuiScaledHeight();

		// Title
		titleWidget = new StringWidget(getTitle(), minecraft.font);
		titleWidget.setPosition((width - titleWidget.getWidth()) / 2, 16);

		// Start centered
		IntIntPair rootPos = getRootPos();
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		int yOffset = (ROW_HEIGHT - minecraft.font.lineHeight) / 2;

		// Row 1, name
		Component nameText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name");
		nameLabel = textWidget(rootPos, currentPos, yOffset, nameText);
		nextColumn(currentPos, minecraft.font.width(nameText));

		int textFieldWidth = 200; // Placeholder value, their size is calculated dynamically afterward
		nameInput = new EditBox(minecraft.font, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Component.nullToEmpty(""));
		nameInput.setValue(chatRule.getName());
		nameInput.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));
		nextRow(currentPos);

		// Row 2, inputs header

		inputsLabel = textWidget(rootPos, currentPos, yOffset, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.inputs").withStyle(ChatFormatting.BOLD));
		nextRow(currentPos);

		// Row 3, filter

		Component filterText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter");
		filterLabel = textWidget(currentPos, rootPos, yOffset, filterText);
		nextColumn(currentPos, minecraft.font.width(filterText));
		filterInput = new EditBox(minecraft.font, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Component.nullToEmpty(""));
		filterInput.setMaxLength(256);
		filterInput.setValue(chatRule.getFilter());
		filterInput.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));
		nextRow(currentPos);

		// Row 4, partial match and regex

		Component partialMatchText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch");
		partialMatchLabel = textWidget(rootPos, currentPos, yOffset, partialMatchText);
		nextColumn(currentPos, minecraft.font.width(partialMatchText));
		partialMatchToggle = Button.builder(enabledButtonText(chatRule.getPartialMatch()), a -> {
					chatRule.setPartialMatch(!chatRule.getPartialMatch());
					partialMatchToggle.setMessage(enabledButtonText(chatRule.getPartialMatch()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
				.build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Component regexLabelText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex");
		regexLabel = textWidget(rootPos, currentPos, yOffset, regexLabelText);
		nextColumn(currentPos, minecraft.font.width(regexLabelText));
		regexToggle = Button.builder(enabledButtonText(chatRule.getRegex()), a -> {
					chatRule.setRegex(!chatRule.getRegex());
					regexToggle.setMessage(enabledButtonText(chatRule.getRegex()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
				.build();
		nextRow(currentPos);

		// Row 5, ignore case and location selection
		Component ignoreCaseText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase");
		ignoreCaseLabel = textWidget(rootPos, currentPos, yOffset, ignoreCaseText);
		nextColumn(currentPos, minecraft.font.width(ignoreCaseText));
		ignoreCaseToggle = Button.builder(enabledButtonText(chatRule.getIgnoreCase()), a -> {
					chatRule.setIgnoreCase(!chatRule.getIgnoreCase());
					ignoreCaseToggle.setMessage(enabledButtonText(chatRule.getIgnoreCase()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
				.build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Component locationsText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations");
		locationLabel = textWidget(rootPos, currentPos, yOffset, locationsText);
		nextColumn(currentPos, minecraft.font.width(locationsText));

		locationsConfigButton = Button.builder(Component.translatable("text.skyblocker.open"),
						widget -> minecraft.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
				.bounds(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), BUTTON_WIDTH, ROW_HEIGHT)
				.build();

		nextRow(currentPos);

		// Row 6, outputs header

		outputsLabel = textWidget(rootPos, currentPos, yOffset, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").withStyle(ChatFormatting.BOLD));
		nextRow(currentPos);

		// Row 7, hide message, action bar, announcement checkboxes and sound selection

		Component hideMessageText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage");
		hideMessageLabel = textWidget(rootPos, currentPos, yOffset, hideMessageText);
		nextColumn(currentPos, minecraft.font.width(hideMessageText));
		hideMessageToggle = Button.builder(enabledButtonText(chatRule.getHideMessage()), a -> {
					chatRule.setHideMessage(!chatRule.getHideMessage());
					hideMessageToggle.setMessage(enabledButtonText(chatRule.getHideMessage()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
				.build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Component actionBarText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar");
		actionBarLabel = textWidget(rootPos, currentPos, yOffset, actionBarText);
		nextColumn(currentPos, minecraft.font.width(actionBarText));
		actionBarToggle = Button.builder(enabledButtonText(chatRule.getShowActionBar()), a -> {
					chatRule.setShowActionBar(!chatRule.getShowActionBar());
					actionBarToggle.setMessage(enabledButtonText(chatRule.getShowActionBar()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar.@Tooltip")))
				.build();
		nextRow(currentPos);

		// Row 8, announcement, sounds

		Component announcementText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement");
		announcementLabel = textWidget(rootPos, currentPos, yOffset, announcementText);
		nextColumn(currentPos, minecraft.font.width(announcementText));
		announcementToggle = Button.builder(enabledButtonText(chatRule.getShowAnnouncement()), a -> {
					chatRule.setShowAnnouncement(!chatRule.getShowAnnouncement());
					announcementToggle.setMessage(enabledButtonText(chatRule.getShowAnnouncement()));
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")))
				.build();
		nextColumn(currentPos, BUTTON_WIDTH);

		Component soundsText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds");
		soundsLabel = textWidget(rootPos, currentPos, yOffset, soundsText);
		nextColumn(currentPos, minecraft.font.width(soundsText));

		soundsToggle = Button.builder(getSoundName(), a -> {
					currentSoundIndex += 1;
					if (currentSoundIndex == soundsLookup.size()) {
						currentSoundIndex = -1;
					}
					MutableComponent newText = getSoundName();
					soundsToggle.setMessage(newText);
					SoundEvent sound = soundsLookup.get(newText);
					chatRule.setCustomSound(sound);
					if (minecraft.player != null && sound != null) {
						minecraft.player.playSound(sound, 100f, 0.1f);
					}
				})
				.pos(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt())
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.@Tooltip")))
				.build();
		nextRow(currentPos);

		// Row 9, replacement message

		Component replaceMessageText = Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace");
		replaceMessageLabel = textWidget(rootPos, currentPos, yOffset, replaceMessageText);
		nextColumn(currentPos, minecraft.font.width(replaceMessageText));
		replaceMessageInput = new EditBox(minecraft.font, rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt(), textFieldWidth, ROW_HEIGHT, Component.nullToEmpty(""));
		replaceMessageInput.setMaxLength(96);
		replaceMessageInput.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.replace.@Tooltip")));
		replaceMessageInput.setValue(chatRule.getReplaceMessage());

		// Finish button at bottom right corner

		finishButton = Button.builder(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.finish"), a -> onClose())
				.pos(this.width - BUTTON_WIDTH - SPACER_Y, this.height - SPACER_Y)
				.size(BUTTON_WIDTH, ROW_HEIGHT)
				.build();
		calculateTextFieldWidths();
		calculateButtonWidths();
	}

	private int getCurrentSoundIndex() {
		if (chatRule.getCustomSound() == null) return -1; //if no sound just return -1

		List<SoundEvent> soundOptions = soundsLookup.values().stream().toList();
		Identifier ruleSoundId = chatRule.getCustomSound().location();

		for (int i = 0; i < soundOptions.size(); i++) {
			if (soundOptions.get(i).location().compareTo(ruleSoundId) == 0) {
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
		addRenderableWidget(titleWidget);
		// Row 1
		addRenderableWidget(nameInput);
		addRenderableWidget(nameLabel);
		// Row 2
		addRenderableWidget(inputsLabel);
		// Row 3
		addRenderableWidget(filterInput);
		addRenderableWidget(filterLabel);
		// Row 4
		addRenderableWidget(partialMatchToggle);
		addRenderableWidget(partialMatchLabel);
		addRenderableWidget(regexToggle);
		addRenderableWidget(regexLabel);
		// Row 5
		addRenderableWidget(ignoreCaseToggle);
		addRenderableWidget(ignoreCaseLabel);
		addRenderableWidget(locationsConfigButton);
		addRenderableWidget(locationLabel);
		// Row 6
		addRenderableWidget(outputsLabel);
		// Row 7
		addRenderableWidget(hideMessageToggle);
		addRenderableWidget(hideMessageLabel);
		addRenderableWidget(actionBarToggle);
		addRenderableWidget(actionBarLabel);
		// Row 8
		addRenderableWidget(announcementToggle);
		addRenderableWidget(announcementLabel);
		addRenderableWidget(soundsToggle);
		addRenderableWidget(soundsLabel);
		// Row 9
		addRenderableWidget(replaceMessageInput);
		addRenderableWidget(replaceMessageLabel);
		// Finish button
		addRenderableWidget(finishButton);
		calculateTextFieldWidths();
		calculateButtonWidths();
	}

	private void recalculateWidgetPositions() {
		IntIntPair rootPos = getRootPos();
		IntIntMutablePair currentPos = IntIntMutablePair.of(0, 0); // Offset from root pos, add them up and we get the actual position
		assert minecraft != null;
		// Title
		titleWidget.setX((width - titleWidget.getWidth()) / 2);
		// Row 1
		setWidgetPosition(nameLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(nameLabel.getMessage()));
		setWidgetPosition(nameInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 2
		setWidgetPosition(inputsLabel, rootPos, currentPos, Y_OFFSET);
		nextRow(currentPos);
		// Row 3
		setWidgetPosition(filterLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(filterLabel.getMessage()));
		setWidgetPosition(filterInput, rootPos, currentPos);
		nextRow(currentPos);
		// Row 4
		setWidgetPosition(partialMatchLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(partialMatchLabel.getMessage()));
		setWidgetPosition(partialMatchToggle, rootPos, currentPos);
		partialMatchToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(regexLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(regexLabel.getMessage()));
		setWidgetPosition(regexToggle, rootPos, currentPos);
		regexToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 5
		setWidgetPosition(ignoreCaseLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(ignoreCaseLabel.getMessage()));
		setWidgetPosition(ignoreCaseToggle, rootPos, currentPos);
		ignoreCaseToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(locationLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(locationLabel.getMessage()));
		setWidgetPosition(locationsConfigButton, rootPos, currentPos);
		locationsConfigButton.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 6
		setWidgetPosition(outputsLabel, rootPos, currentPos, Y_OFFSET);
		nextRow(currentPos);
		// Row 7
		setWidgetPosition(hideMessageLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(hideMessageLabel.getMessage()));
		setWidgetPosition(hideMessageToggle, rootPos, currentPos);
		hideMessageToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(actionBarLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(actionBarLabel.getMessage()));
		setWidgetPosition(actionBarToggle, rootPos, currentPos);
		actionBarToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 8
		setWidgetPosition(announcementLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(announcementLabel.getMessage()));
		setWidgetPosition(announcementToggle, rootPos, currentPos);
		announcementToggle.setWidth(BUTTON_WIDTH);
		nextColumn(currentPos, BUTTON_WIDTH);
		setWidgetPosition(soundsLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(soundsLabel.getMessage()));
		setWidgetPosition(soundsToggle, rootPos, currentPos);
		soundsToggle.setWidth(BUTTON_WIDTH);
		nextRow(currentPos);
		// Row 9
		setWidgetPosition(replaceMessageLabel, rootPos, currentPos, Y_OFFSET);
		nextColumn(currentPos, minecraft.font.width(replaceMessageLabel.getMessage()));
		setWidgetPosition(replaceMessageInput, rootPos, currentPos);
		// Finish button
		finishButton.setPosition(this.width - BUTTON_WIDTH - SPACER_X, this.height - SPACER_Y - ROW_HEIGHT);
	}

	/**
	 * Convenience method for creating a text widget with a root position and an offset, and another offset to center the text vertically.
	 *
	 * @param rootPos       The root position of the widget within the screen.
	 * @param offset        The offset from the root position.
	 * @param yCenterOffset The offset to center the text vertically.
	 * @param text          The text to display.
	 * @return A new text widget.
	 */
	private static StringWidget textWidget(IntIntPair rootPos, IntIntPair offset, int yCenterOffset, Component text) {
		return WidgetUtils.textWidget(rootPos.leftInt() + offset.leftInt(), rootPos.rightInt() + offset.rightInt() + yCenterOffset, text);
	}

	/**
	 * Convenience method to set the position of a widget based on the root position and the current position.
	 */
	private static void setWidgetPosition(LayoutElement widget, IntIntPair rootPos, IntIntPair currentPos) {
		widget.setPosition(rootPos.leftInt() + currentPos.leftInt(), rootPos.rightInt() + currentPos.rightInt());
	}

	/**
	 * Convenience method to set the position of a widget based on the root position and the current position, with an additional y offset, mainly for text widgets.
	 */
	@SuppressWarnings("SameParameterValue") // We can't just inline the parameter value since it causes method signature conflicts. This is fine.
	private static void setWidgetPosition(LayoutElement widget, IntIntPair rootPos, IntIntPair currentPos, int yOffset) {
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
	 * @param textWidget      The labeling text widget
	 * @param textFieldWidget The text field widget to calculate the width for
	 */
	private void calculateTextFieldWidth(StringWidget textWidget, EditBox textFieldWidget) {
		assert minecraft != null;
		int textWidth = minecraft.font.width(textWidget.getMessage());
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
	 * @param label1  The first label
	 * @param button1 The first button
	 * @param label2  The second label
	 * @param button2 The second button
	 */
	private void calculateButtonWidth(StringWidget label1, Button button1, StringWidget label2, Button button2) {
		assert minecraft != null;
		int label1Width = minecraft.font.width(label1.getMessage());
		int label2Width = minecraft.font.width(label2.getMessage());
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

	private Component enabledButtonText(boolean enabled) {
		if (enabled) {
			return Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.getRGB());
		} else {
			return Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.false").withColor(Color.RED.getRGB());
		}
	}

	/**
	 * Saves and returns to parent screen
	 */
	@Override
	public void onClose() {
		if (minecraft != null) {
			save();
			minecraft.setScreen(parent);
		}
	}

	private void save() {
		chatRule.setName(nameInput.getValue());
		chatRule.setFilter(filterInput.getValue());
		chatRule.setReplaceMessage(replaceMessageInput.getValue());

		ChatRulesHandler.chatRuleList.getData().set(chatRuleIndex, chatRule);
	}

	private MutableComponent getSoundName() {
		if (currentSoundIndex == -1) {
			return Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none");
		}

		return soundsLookup.keySet().stream().toList().get(currentSoundIndex);
	}
}
