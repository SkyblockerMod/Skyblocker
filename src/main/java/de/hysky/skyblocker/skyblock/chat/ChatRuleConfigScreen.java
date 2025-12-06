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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChatRuleConfigScreen extends Screen {
	private static final int COLUMN_WIDTH = 105;
	private static final int GRID_SPACING = 2;
	private static final ItemStack INVALID_ITEM = new ItemStack(Items.BARRIER);
	private static final Text YES_TEXT = ScreenTexts.YES.copy().formatted(Formatting.GREEN);
	private static final Text NO_TEXT = ScreenTexts.NO.copy().formatted(Formatting.RED);

	private final Map<@Nullable SoundEvent, Text> soundNames = Util.make(new Object2ObjectOpenHashMap<>(), map -> {
		map.put(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling").formatted(Formatting.YELLOW));
		map.put(SoundEvents.AMBIENT_CAVE.value(), Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave").formatted(Formatting.YELLOW));
		map.put(SoundEvents.ENTITY_ZOMBIE_AMBIENT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie").formatted(Formatting.YELLOW));
		map.put(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit").formatted(Formatting.YELLOW));
		map.put(SoundEvents.ENTITY_ARROW_HIT_PLAYER, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit").formatted(Formatting.YELLOW));
		map.put(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst").formatted(Formatting.YELLOW));
		map.put(SoundEvents.BLOCK_ANVIL_LAND, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil").formatted(Formatting.YELLOW));
		map.put(null, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none").formatted(Formatting.RED));
		map.defaultReturnValue(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.custom").formatted(Formatting.YELLOW));
	});

	private final int chatRuleIndex;
	private final ChatRule chatRule;

	private @Nullable ChatRule.ToastMessage previousToastMessage = null;
	private @Nullable ChatRule.AnnouncementMessage previousAnnouncementMessage = null;

	private final Screen parent;

	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
	private final GridWidget content = new GridWidget().setColumnSpacing(GRID_SPACING);

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.CHAT_RULE_LIST.getData().get(chatRuleIndex);
		this.parent = parent;
	}

	@Override
	protected void init() {
		assert client != null;
		layout.addHeader(new TextWidget(title, textRenderer));
		layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).build());
		layout.addBody(new ContentContainer());

		content.getMainPositioner().alignVerticalCenter().alignHorizontalCenter().marginTop(GRID_SPACING); // Have to separate them due to the toggleable layouts, did not think about that when I made them
		Positioner alignedLeft = content.copyPositioner().alignLeft();
		GridWidget.Adder contentAdder = content.createAdder(3);

		// Name
		contentAdder.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), 3);
		TextFieldWidget nameWidget = contentAdder.add(new TextFieldWidget(textRenderer, getWidth(3), 20, Text.empty()), 3);
		nameWidget.setText(chatRule.getName());
		nameWidget.setChangedListener(chatRule::setName);
		nameWidget.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));


		// Filter
		contentAdder.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), 3);
		TextFieldWidget filterInput = contentAdder.add(new TextFieldWidget(textRenderer, getWidth(3), 20, Text.empty()), 3);
		filterInput.setMaxLength(1024);
		filterInput.setText(chatRule.getFilter());
		filterInput.setChangedListener(chatRule::setFilter);
		filterInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").formatted(Formatting.GRAY, Formatting.ITALIC));
		filterInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));

		// Filter settings
		DirectionalLayoutWidget filtersRow1 = contentAdder.add(DirectionalLayoutWidget.horizontal().spacing(GRID_SPACING), 3);
		filtersRow1.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getRegex())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
				.build(0, 0, getWidth(1.5f), 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"), (button, value) -> chatRule.setRegex(value)));
		filtersRow1.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getIgnoreCase())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
				.build(0, 0, getWidth(1.5f), 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"), (button, value) -> chatRule.setIgnoreCase(value)));
		DirectionalLayoutWidget filtersRow2 = contentAdder.add(DirectionalLayoutWidget.horizontal().spacing(GRID_SPACING), 3);
		filtersRow2.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getPartialMatch())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
				.build(0, 0, getWidth(1.5f), 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"), (button, value) -> chatRule.setPartialMatch(value)));
		filtersRow2.add(ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations"),
						widget -> client.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
				.tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
				.width(getWidth(1.5f))
				.build());

		// ==== Outputs
		contentAdder.add(new TextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").formatted(Formatting.BOLD, Formatting.UNDERLINE), textRenderer), 3, content.copyPositioner().marginTop(4 + GRID_SPACING));
		contentAdder.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.formatting"), textRenderer).setMaxWidth(getWidth(2)), 3).setCentered(true);

		DirectionalLayoutWidget buttons = contentAdder.add(DirectionalLayoutWidget.horizontal().spacing(GRID_SPACING), 3);

		buttons.add(CyclingButtonWidget.onOffBuilder(YES_TEXT, NO_TEXT)
				.initially(chatRule.getHideMessage())
				.tooltip(b -> Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
				.build(0, 0, getWidth(1.5f), 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), (button, value) -> {
					chatRule.setHideMessage(value);
					recreateLayout();
				}));

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
		buttons.add(CyclingButtonWidget.<Optional<SoundEvent>>builder(opt -> soundNames.get(opt.orElse(null)))
				.values(() -> true, displayedValues, availableValues)
				.initially(Optional.ofNullable(chatRule.getCustomSound()))
				.build(0, 0, getWidth(1.5f), 20, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds"), (button, value) -> {
					chatRule.setCustomSound(value.orElse(null));
					value.ifPresent(soundEvent -> client.getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0F)));
				})
		);

		// Chat message
		TextFieldWidget chatMessageInput = new TextFieldWidget(textRenderer, getWidth(2), 20, Text.empty());
		chatMessageInput.setMaxLength(1024);
		contentAdder.add(new ToggleableLayoutWidget(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage"), textRenderer).setMaxWidth(getWidth(1)).setCentered(false), () -> !chatRule.getHideMessage()), alignedLeft);
		chatMessageInput.addFormatter(createRenderTextProvider(chatMessageInput::getText));
		chatMessageInput.setText(chatRule.getChatMessage() != null ? chatRule.getChatMessage() : "");
		chatMessageInput.setChangedListener(chatRule::setChatMessage);
		chatMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		contentAdder.add(new ToggleableLayoutWidget(chatMessageInput, () -> !chatRule.getHideMessage()), 2);

		// Action Bar
		contentAdder.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), textRenderer), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		TextFieldWidget actionBarMessageInput = new TextFieldWidget(textRenderer, getWidth(2), 20, Text.empty());
		actionBarMessageInput.setMaxLength(1024);
		actionBarMessageInput.addFormatter(createRenderTextProvider(actionBarMessageInput::getText));
		actionBarMessageInput.setText(chatRule.getActionBarMessage() != null ? chatRule.getActionBarMessage() : "");
		actionBarMessageInput.setChangedListener(chatRule::setActionBarMessage);
		actionBarMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		contentAdder.add(actionBarMessageInput, 2);

		// Announcement
		contentAdder.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), textRenderer), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		TextFieldWidget announcementMessageInput = new TextFieldWidget(textRenderer, getWidth(2), 20, Text.empty());
		announcementMessageInput.setMaxLength(1024);
		announcementMessageInput.addFormatter(createRenderTextProvider(announcementMessageInput::getText));
		announcementMessageInput.setText(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage().message : "");
		announcementMessageInput.setChangedListener(s -> {
			if (s.isEmpty()) {
				previousAnnouncementMessage = chatRule.getAnnouncementMessage();
				chatRule.setAnnouncementMessage(null);
			} else {
				if (chatRule.getAnnouncementMessage() == null) chatRule.setAnnouncementMessage(previousAnnouncementMessage != null ? previousAnnouncementMessage : new ChatRule.AnnouncementMessage());
				chatRule.getAnnouncementMessage().message = s;
			}
			recreateLayout();
		});
		announcementMessageInput.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")));
		announcementMessageInput.setPlaceholder(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").formatted(Formatting.GRAY, Formatting.ITALIC));
		contentAdder.add(announcementMessageInput, 2);

		contentAdder.add(EmptyWidget.ofHeight(0), content.copyPositioner().marginTop(0));
		RangedSliderWidget announcementDurationSlider = RangedSliderWidget.builder()
				.minMax(1, 10)
				.step(0.1)
				.width(getWidth(2))
				.defaultValue(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage().displayDuration / 1000d : 5000d)
				.optionFormatter(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.duration"), d -> Text.literal(Formatters.FLOAT_NUMBERS.format(d) + 's'))
				.callback(d -> {
					if (chatRule.getAnnouncementMessage() == null) return;
					chatRule.getAnnouncementMessage().displayDuration = (long) (d * 1000);
				})
				.build();
		contentAdder.add(new ToggleableLayoutWidget(
				Util.make(new SimplePositioningWidget(), w -> w.add(announcementDurationSlider, p -> p.marginTop(GRID_SPACING))),
				() -> chatRule.getAnnouncementMessage() != null
		), 2, content.copyPositioner().marginTop(0));

		// Toast
		contentAdder.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast"), textRenderer), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		TextFieldWidget toastMessageInput = new TextFieldWidget(textRenderer, getWidth(2), 20, Text.empty());
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
		contentAdder.add(toastMessageInput, 2);

		BooleanSupplier toastOptionsPredicate = () -> chatRule.getToastMessage() != null;
		// Label + preview
		// Have to do something a little more custom due to the preview.
		SimplePositioningWidget textAndIcon = new SimplePositioningWidget(getWidth(1), 0);
		contentAdder.add(new ToggleableLayoutWidget(textAndIcon, toastOptionsPredicate));

		TextFieldWidget itemInput = new TextFieldWidget(textRenderer, getWidth(1), 20, Text.empty());
		ToastIconPreview preview = textAndIcon.add(new ToastIconPreview(itemInput), Positioner::alignRight);
		textAndIcon.add(new MultilineTextWidget(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.icon"), textRenderer), Positioner::alignLeft).setMaxWidth(getWidth(1) - preview.getWidth()).setCentered(false);

		// Item input
		contentAdder.add(new ToggleableLayoutWidget(itemInput, toastOptionsPredicate));
		itemInput.setChangedListener(itemData -> {
			ItemStack stack = ItemStackComponentizationFixer.fromItemString(itemData, 1);
			if (stack.isEmpty()) stack = INVALID_ITEM;
			preview.stack = stack;
			ChatRule.ToastMessage message = chatRule.getToastMessage();
			if (message == null) return;
			message.icon = stack;
		});
		itemInput.setText(chatRule.getToastMessage() != null ? getItemString(chatRule.getToastMessage().icon) : "minecraft:painting");

		// Duration slider
		RangedSliderWidget sliderWidget = RangedSliderWidget.builder()
				.minMax(1, 10)
				.step(0.1)
				.width(getWidth(1))
				.defaultValue(chatRule.getToastMessage() != null ? chatRule.getToastMessage().displayDuration / 1000d : 5000d)
				.optionFormatter(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.duration"), d -> Text.literal(Formatters.FLOAT_NUMBERS.format(d) + 's'))
				.callback(d -> {
					if (chatRule.getToastMessage() == null) return;
					chatRule.getToastMessage().displayDuration = (long) (d * 1000);
				})
				.build();
		contentAdder.add(new ToggleableLayoutWidget(sliderWidget, toastOptionsPredicate));
		sliderWidget.setTooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.duration.@Tooltip")));
		recreateLayout();
	}

	private static String getItemString(ItemStack stack) {
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

	private static int getWidth(float occupiedColumns) {
		return (int) (COLUMN_WIDTH * occupiedColumns + GRID_SPACING * (occupiedColumns - 1));
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
		ChatRulesHandler.CHAT_RULE_LIST.getData().set(chatRuleIndex, chatRule);
	}

	private class ToastIconPreview extends ClickableWidget {
		private ItemStack stack = ItemStack.EMPTY;
		private final TextFieldWidget input;

		private ToastIconPreview(TextFieldWidget input) {
			super(0, 0, 16, 16, Text.empty());
			this.input = input;
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
			assert client != null;
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
