package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.function.*;

import static java.util.Map.entry;

public class ChatRuleConfigScreen extends Screen {
	private static final Text YES_TEXT = ScreenTexts.YES.copy().formatted(Formatting.GREEN);
	private static final Text NO_TEXT = ScreenTexts.NO.copy().formatted(Formatting.RED);

	private final Map<MutableText, SoundEvent> soundsLookup = Map.ofEntries(
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling"), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave"), SoundEvents.AMBIENT_CAVE.value()),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie"), SoundEvents.ENTITY_ZOMBIE_AMBIENT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit"), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit"), SoundEvents.ENTITY_ARROW_HIT_PLAYER),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst"), SoundEvents.BLOCK_AMETHYST_BLOCK_HIT),
			entry(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil"), SoundEvents.BLOCK_ANVIL_LAND)
	);

	private final int chatRuleIndex;
	private final ChatRule chatRule;

	private int currentSoundIndex;

	private final Screen parent;

	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.chatRuleList.getData().get(chatRuleIndex);
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.addHeader(new TextWidget(title, textRenderer));
		layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).build());

		DirectionalLayoutWidget mainLayout = layout.addBody(DirectionalLayoutWidget.vertical());

		DirectionalLayoutWidget nameLayout = mainLayout.add(DirectionalLayoutWidget.horizontal().spacing(4));
		nameLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name"), textRenderer));
		TextFieldWidget nameWidget = new TextFieldWidget(textRenderer, 150, 20, Text.empty());
		nameWidget.setText(chatRule.getName());
		nameWidget.setChangedListener(chatRule::setName);
		nameWidget.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));

		mainLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), Positioner::alignHorizontalCenter);
		TextFieldWidget filterInput = new TextFieldWidget(textRenderer, 300, 20, Text.empty());
		filterInput.setText(chatRule.getFilter());
		filterInput.setChangedListener(chatRule::setFilter);
		filterInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.GRAY, Formatting.ITALIC));
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));

		DirectionalLayoutWidget filterButtonsLayout = mainLayout.add(DirectionalLayoutWidget.horizontal().spacing(2), Positioner::alignHorizontalCenter);
		int buttonWidth = 98;
		filterButtonsLayout.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getRegex())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
				.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"), (button, value) -> chatRule.setRegex(value)));
		filterButtonsLayout.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getIgnoreCase())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
				.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"), (button, value) -> chatRule.setIgnoreCase(value)));
		filterButtonsLayout.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getPartialMatch())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
				.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"), (button, value) -> chatRule.setPartialMatch(value)));

		mainLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), Positioner::alignHorizontalCenter);
		mainLayout.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.formatting"), textRenderer).setMaxWidth(200), Positioner::alignHorizontalCenter);

		TextFieldWidget chatMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		chatMessageInput.active = !chatRule.getHideMessage();
		mainLayout.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getHideMessage())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
				.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), (button, value) -> {
					chatRule.setHideMessage(value);
					chatMessageInput.active = !value;
				}));
		GridWidget.Adder messagesLayout = mainLayout.add(new GridWidget(), Positioner::alignHorizontalCenter).setSpacing(4).createAdder(2);

		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage"), textRenderer));
		chatMessageInput.setRenderTextProvider(createRenderTextProvider(chatMessageInput::getText));
		chatMessageInput.setText(chatRule.getChatMessage() != null ? chatRule.getChatMessage() : "");
		chatMessageInput.setChangedListener(chatRule::setChatMessage);
		chatMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(chatMessageInput);

		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), textRenderer));
		TextFieldWidget actionBarMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		actionBarMessageInput.setRenderTextProvider(createRenderTextProvider(actionBarMessageInput::getText));
		actionBarMessageInput.setText(chatRule.getActionBarMessage() != null ? chatRule.getActionBarMessage() : "");
		actionBarMessageInput.setChangedListener(chatRule::setActionBarMessage);
		actionBarMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(actionBarMessageInput);

		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), textRenderer));
		TextFieldWidget announcementMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		announcementMessageInput.setRenderTextProvider(createRenderTextProvider(announcementMessageInput::getText));
		announcementMessageInput.setText(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage() : "");
		announcementMessageInput.setChangedListener(chatRule::setAnnouncementMessage);
		announcementMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")));
		announcementMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(announcementMessageInput);

		recreateLayout();
	}

	private static BiFunction<String, Integer, OrderedText> createRenderTextProvider(Supplier<String> fullTextSupplier) {
		return (s, integer) -> visitor -> {
			String fullText = fullTextSupplier.get();
			char prefix = fullText.contains("§") ? '§' : '&';
			Style style = Style.EMPTY;
			for (int i = 0; i < fullText.length(); i++) {
				if (fullText.charAt(i) == prefix) {
					if (i + 1 < fullText.length()) {
						Formatting formatting = Formatting.byCode(fullText.charAt(i + 1));
						if (formatting != null) {
							style = formatting == Formatting.RESET ? Style.EMPTY : style.withExclusiveFormatting(formatting);
						}
					}
				}
				int codePoint = fullText.codePointAt(i);
				if (i >= integer) {
					visitor.accept(i, style, codePoint);
				}
			}
			return true;
		};
	}

	private void recreateLayout() {
		clearChildren();
		refreshWidgetPositions();
		layout.forEachChild(this::addDrawableChild);
	}

	@Override
	protected void refreshWidgetPositions() {
		layout.refreshPositions();
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

		ChatRulesHandler.chatRuleList.getData().set(chatRuleIndex, chatRule);
	}
}
