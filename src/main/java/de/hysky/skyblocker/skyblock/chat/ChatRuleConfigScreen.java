package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.ItemSelectionPopup;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import de.hysky.skyblocker.utils.render.gui.ToggleableLayoutWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;

public class ChatRuleConfigScreen extends Screen {
	private static final ItemStack INVALID_ITEM = new ItemStack(Items.BARRIER);
	private static final Text YES_TEXT = ScreenTexts.YES.copy().formatted(Formatting.GREEN);
	private static final Text NO_TEXT = ScreenTexts.NO.copy().formatted(Formatting.RED);

	private final Map<SoundEvent, Text> soundNames = Util.make(new Object2ObjectOpenHashMap<>(), map -> {
		map.put(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling"));
		map.put(SoundEvents.AMBIENT_CAVE.value(), Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave"));
		map.put(SoundEvents.ENTITY_ZOMBIE_AMBIENT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie"));
		map.put(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit"));
		map.put(SoundEvents.ENTITY_ARROW_HIT_PLAYER, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit"));
		map.put(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst"));
		map.put(SoundEvents.BLOCK_ANVIL_LAND, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil"));
		map.put(null, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none"));
		map.defaultReturnValue(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.custom"));
	});

	private final int chatRuleIndex;
	private final ChatRule chatRule;

	private @Nullable ChatRule.ToastMessage previousToastMessage = null;

	private final Screen parent;

	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
	private DirectionalLayoutWidget content;

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
		layout.addBody(new ContentContainer());

		content = DirectionalLayoutWidget.vertical().spacing(2);

		// Name
		DirectionalLayoutWidget nameLayout = content.add(DirectionalLayoutWidget.horizontal().spacing(4), p -> p.marginBottom(4));
		nameLayout.getMainPositioner().alignVerticalCenter();
		nameLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name"), textRenderer));
		TextFieldWidget nameWidget = nameLayout.add(new TextFieldWidget(textRenderer, 150, 20, Text.empty()));
		nameWidget.setText(chatRule.getName());
		nameWidget.setChangedListener(chatRule::setName);
		nameWidget.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));

		// Filter
		content.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), Positioner::alignHorizontalCenter);
		TextFieldWidget filterInput = content.add(new TextFieldWidget(textRenderer, 300, 20, Text.empty()), Positioner::alignHorizontalCenter);
		filterInput.setMaxLength(1024);
		filterInput.setText(chatRule.getFilter());
		filterInput.setChangedListener(chatRule::setFilter);
		filterInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.GRAY, Formatting.ITALIC));
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));

		// Filter settings
		DirectionalLayoutWidget filterButtonsLayout = content.add(DirectionalLayoutWidget.horizontal().spacing(2), Positioner::alignHorizontalCenter);
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

		// ==== Outputs
		content.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), p -> p.alignHorizontalCenter().marginTop(4));
		content.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.formatting"), textRenderer).setMaxWidth(200), Positioner::alignHorizontalCenter);

		DirectionalLayoutWidget outputButtonsLayout = content.add(DirectionalLayoutWidget.horizontal().spacing(2), Positioner::alignHorizontalCenter);

		// Chat message
		TextFieldWidget chatMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		chatMessageInput.setMaxLength(1024);
		chatMessageInput.active = !chatRule.getHideMessage();
		outputButtonsLayout.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getHideMessage())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
				.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), (button, value) -> {
					chatRule.setHideMessage(value);
					chatMessageInput.active = !value;
				}));
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().alignVerticalCenter();
		GridWidget.Adder messagesLayout = content.add(gridWidget, Positioner::alignHorizontalCenter).setSpacing(4).createAdder(2);

		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage"), textRenderer));
		chatMessageInput.addFormatter(createRenderTextProvider(chatMessageInput::getText));
		chatMessageInput.setText(chatRule.getChatMessage() != null ? chatRule.getChatMessage() : "");
		chatMessageInput.setChangedListener(chatRule::setChatMessage);
		chatMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(chatMessageInput);

		// Action Bar
		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), textRenderer));
		TextFieldWidget actionBarMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		actionBarMessageInput.setMaxLength(1024);
		actionBarMessageInput.addFormatter(createRenderTextProvider(actionBarMessageInput::getText));
		actionBarMessageInput.setText(chatRule.getActionBarMessage() != null ? chatRule.getActionBarMessage() : "");
		actionBarMessageInput.setChangedListener(chatRule::setActionBarMessage);
		actionBarMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(actionBarMessageInput);

		// Announcement
		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), textRenderer));
		TextFieldWidget announcementMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		announcementMessageInput.setMaxLength(1024);
		announcementMessageInput.addFormatter(createRenderTextProvider(announcementMessageInput::getText));
		announcementMessageInput.setText(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage() : "");
		announcementMessageInput.setChangedListener(chatRule::setAnnouncementMessage);
		announcementMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")));
		announcementMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(announcementMessageInput);

		// Toast
		messagesLayout.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast"), textRenderer));
		TextFieldWidget toastMessageInput = new TextFieldWidget(textRenderer, 200, 20, Text.empty());
		toastMessageInput.setMaxLength(1024);
		toastMessageInput.addFormatter(createRenderTextProvider(toastMessageInput::getText));
		toastMessageInput.setText(chatRule.getToastMessage() != null ? chatRule.getToastMessage().message : "");
		toastMessageInput.setChangedListener(s -> {
			if (s.isEmpty()) {
				previousToastMessage = chatRule.getToastMessage();
				chatRule.setToastMessage(null);
			} else {
				if (chatRule.getToastMessage() == null) chatRule.setToastMessage(previousToastMessage != null ? previousToastMessage : new ChatRule.ToastMessage());
				chatRule.getToastMessage().message = s;
			}
			recreateLayout();
		});
		toastMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		messagesLayout.add(toastMessageInput);

		DirectionalLayoutWidget toastSettings = DirectionalLayoutWidget.horizontal().spacing(2);
		toastSettings.getMainPositioner().alignVerticalCenter();
		content.add(new ToggleableLayoutWidget(toastSettings, () -> chatRule.getToastMessage() != null), p -> p.alignHorizontalCenter().marginBottom(4));

		ToastIconPreview preview = toastSettings.add(new ToastIconPreview());
		TextFieldWidget itemInput = toastSettings.add(new TextFieldWidget(textRenderer, 150, 20, Text.empty()));
		itemInput.setChangedListener(itemData -> {
			ItemStack stack = ItemStackComponentizationFixer.fromItemString(itemData, 1);
			if (stack.isEmpty()) stack = INVALID_ITEM;
			preview.stack = stack;
			ChatRule.ToastMessage message = chatRule.getToastMessage();
			if (message == null) return;
			message.icon = stack;
		});
		preview.input = itemInput;
		itemInput.setText(chatRule.getToastMessage() != null ? getItemString(chatRule.getToastMessage().icon) : "minecraft:painting");
		toastSettings.add(RangedSliderWidget.builder()
						.minMax(1, 10)
						.step(0.1)
						.defaultValue(chatRule.getToastMessage() != null ? chatRule.getToastMessage().displayDuration / 1000d : 5000d)
						.optionFormatter(Text.literal("Display Duration"), d -> Text.literal(Formatters.FLOAT_NUMBERS.format(d) + 's'))
						.callback(d -> {
							if (chatRule.getToastMessage() == null) return;
							chatRule.getToastMessage().displayDuration = (long) (d * 1000);
						})
				.build()
		);

		// Sound
		// In case the user has a sound not in the list added to the config. We abuse the fact that we can have alternative values.
		// Add the user sound to the displayedValues, if they click it will switch to one in the list, and it will not appear again.
		List<Optional<SoundEvent>> availableValues = new ArrayList<>(soundNames.keySet().stream().map(Optional::ofNullable).toList());
		List<Optional<SoundEvent>> displayedValues;
		if (soundNames.containsKey(chatRule.getCustomSound())) displayedValues = availableValues;
		else {
			displayedValues = new ArrayList<>(availableValues);
			displayedValues.add(Optional.ofNullable(chatRule.getCustomSound()));
		}
		// using an optional since it doesn't allow null values.
		outputButtonsLayout.add(CyclingButtonWidget.<Optional<SoundEvent>>builder(opt -> soundNames.get(opt.orElse(null)))
						.values(() -> true, displayedValues, availableValues)
						.initially(Optional.ofNullable(chatRule.getCustomSound()))
						.build(0, 0, buttonWidth, 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds"), (button, value) -> {
							chatRule.setCustomSound(value.orElse(null));
							value.ifPresent(soundEvent -> client.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0F)));
						})
				);
		recreateLayout();
	}

	private static @NotNull String getItemString(ItemStack stack) {
		return Registries.ITEM.getId(stack.getItem()) + ItemStackComponentizationFixer.componentsAsString(stack);
	}

	private static TextFieldWidget.Formatter createRenderTextProvider(Supplier<String> fullTextSupplier) {
		return (s, start) -> visitor -> {
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
				if (i >= start) {
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

	private class ToastIconPreview extends ClickableWidget {
		private ItemStack stack;
		private TextFieldWidget input;

		private ToastIconPreview() {
			super(0, 0, 16, 16, Text.empty());
			setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.iconPreview.@Tooltip")));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			context.drawItemWithoutEntity(stack, getX(), getY());
		}

		@Override
		public void setX(int x) {
			super.setX(x);
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			super.onClick(click, doubled);
			client.setScreen(new ItemSelectionPopup(ChatRuleConfigScreen.this, item -> {
				if (item == null) return;
				input.setText(getItemString(item));
			}));
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private class ContentContainer extends ContainerWidget implements LayoutWidget {
		private static final int SIDE_PADDING = 10;
		private final List<ClickableWidget> children = new ArrayList<>();

		private ContentContainer() {
			super(0, 0, 0, 0, Text.empty());
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return content.getHeight();
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 10;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);

			for (ClickableWidget clickableWidget : this.children) {
				clickableWidget.render(context, mouseX, mouseY, deltaTicks);
			}

			context.disableScissor();
			this.drawScrollbar(context, mouseX, mouseY);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

		@Override
		public void forEachElement(Consumer<Widget> consumer) {
			consumer.accept(this);
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			content.setX(x + SIDE_PADDING);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			refreshScroll();
		}

		@Override
		public void setScrollY(double scrollY) {
			super.setScrollY(scrollY);
			content.setY(getY() - (int) getScrollY());
		}

		@Override
		public void refreshPositions() {
			content.refreshPositions();
			children.clear();
			content.forEachChild(children::add);
			setWidth(content.getWidth() + SIDE_PADDING * 2);
			setHeight(layout.getContentHeight());
		}

		@Override
		public void forEachChild(Consumer<ClickableWidget> consumer) {
			super.forEachChild(consumer);
		}
	}
}
