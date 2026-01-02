package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.ItemSelectionPopup;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import de.hysky.skyblocker.utils.render.gui.ToggleableLayoutWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChatRuleConfigScreen extends Screen {
	private static final int COLUMN_WIDTH = 105;
	private static final int GRID_SPACING = 2;
	private static final ItemStack INVALID_ITEM = new ItemStack(Items.BARRIER);
	private static final Component YES_TEXT = CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN);
	private static final Component NO_TEXT = CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED);

	private final Map<@Nullable SoundEvent, Component> soundNames = Util.make(new Object2ObjectOpenHashMap<>(), map -> {
		map.put(SoundEvents.NOTE_BLOCK_PLING.value(), Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.pling").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.AMBIENT_CAVE.value(), Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.cave").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.ZOMBIE_AMBIENT, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.zombie").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.PLAYER_ATTACK_CRIT, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.crit").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.ARROW_HIT_PLAYER, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.arrowHit").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.AMETHYST_BLOCK_HIT, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.amethyst").withStyle(ChatFormatting.YELLOW));
		map.put(SoundEvents.ANVIL_LAND, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.anvil").withStyle(ChatFormatting.YELLOW));
		map.put(null, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.none").withStyle(ChatFormatting.RED));
		map.defaultReturnValue(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds.custom").withStyle(ChatFormatting.YELLOW));
	});

	private final int chatRuleIndex;
	private final ChatRule chatRule;

	private ChatRule.@Nullable ToastMessage previousToastMessage = null;
	private ChatRule.@Nullable AnnouncementMessage previousAnnouncementMessage = null;

	private final Screen parent;

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final GridLayout content = new GridLayout().columnSpacing(GRID_SPACING);

	public ChatRuleConfigScreen(Screen parent, int chatRuleIndex) {
		super(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.chatRuleIndex = chatRuleIndex;
		this.chatRule = ChatRulesHandler.CHAT_RULE_LIST.getData().get(chatRuleIndex);
		this.parent = parent;
	}

	@Override
	protected void init() {
		Objects.requireNonNull(minecraft);
		layout.addToHeader(new StringWidget(title, font));
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build());
		layout.addToContents(new ContentContainer());

		content.defaultCellSetting().alignVerticallyMiddle().alignHorizontallyCenter().paddingTop(GRID_SPACING); // Have to separate them due to the toggleable layouts, did not think about that when I made them
		LayoutSettings alignedLeft = content.newCellSettings().alignHorizontallyLeft();
		GridLayout.RowHelper contentAdder = content.createRowHelper(3);

		// Name
		contentAdder.addChild(new StringWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE), font), 3);
		EditBox nameWidget = contentAdder.addChild(new EditBox(font, getWidth(3), 20, Component.empty()), 3);
		nameWidget.setValue(chatRule.getName());
		nameWidget.setResponder(chatRule::setName);
		nameWidget.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.name.@Tooltip")));


		// Filter
		contentAdder.addChild(new StringWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE), font), 3);
		EditBox filterInput = contentAdder.addChild(new EditBox(font, getWidth(3), 20, Component.empty()), 3);
		filterInput.setMaxLength(1024);
		filterInput.setValue(chatRule.getFilter());
		filterInput.setResponder(chatRule::setFilter);
		filterInput.setHint(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		filterInput.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.filter.@Tooltip")));

		// Filter settings
		LinearLayout filtersRow1 = contentAdder.addChild(LinearLayout.horizontal().spacing(GRID_SPACING), 3);
		filtersRow1.addChild(CycleButton.booleanBuilder(YES_TEXT, NO_TEXT)
				.withInitialValue(chatRule.getRegex())
				.withTooltip(b -> Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex.@Tooltip")))
				.create(0, 0, getWidth(1.5f), 20, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.regex"), (button, value) -> chatRule.setRegex(value)));
		filtersRow1.addChild(CycleButton.booleanBuilder(YES_TEXT, NO_TEXT)
				.withInitialValue(chatRule.getIgnoreCase())
				.withTooltip(b -> Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase.@Tooltip")))
				.create(0, 0, getWidth(1.5f), 20, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.ignoreCase"), (button, value) -> chatRule.setIgnoreCase(value)));
		LinearLayout filtersRow2 = contentAdder.addChild(LinearLayout.horizontal().spacing(GRID_SPACING), 3);
		filtersRow2.addChild(CycleButton.booleanBuilder(YES_TEXT, NO_TEXT)
				.withInitialValue(chatRule.getPartialMatch())
				.withTooltip(b -> Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch.@Tooltip")))
				.create(0, 0, getWidth(1.5f), 20, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.partialMatch"), (button, value) -> chatRule.setPartialMatch(value)));
		filtersRow2.addChild(Button.builder(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations"),
						widget -> minecraft.setScreen(new ChatRuleLocationConfigScreen(this, chatRule)))
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locations.@Tooltip")))
				.width(getWidth(1.5f))
				.build());

		// ==== Outputs
		contentAdder.addChild(new StringWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputs").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE), font), 3, content.newCellSettings().paddingTop(4 + GRID_SPACING));
		contentAdder.addChild(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.formatting"), font).setMaxWidth(getWidth(2)), 3).setCentered(true);

		LinearLayout buttons = contentAdder.addChild(LinearLayout.horizontal().spacing(GRID_SPACING), 3);

		buttons.addChild(CycleButton.booleanBuilder(YES_TEXT, NO_TEXT)
				.withInitialValue(chatRule.getHideMessage())
				.withTooltip(b -> Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage.@Tooltip")))
				.create(0, 0, getWidth(1.5f), 20, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.hideMessage"), (button, value) -> {
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
		buttons.addChild(CycleButton.<Optional<SoundEvent>>builder(opt -> soundNames.get(opt.orElse(null)))
				.withValues(() -> true, displayedValues, availableValues)
				.withInitialValue(Optional.ofNullable(chatRule.getCustomSound()))
				.create(0, 0, getWidth(1.5f), 20, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.sounds"), (button, value) -> {
					chatRule.setCustomSound(value.orElse(null));
					value.ifPresent(soundEvent -> minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F)));
				})
		);

		// Chat message
		EditBox chatMessageInput = new EditBox(font, getWidth(2), 20, Component.empty());
		chatMessageInput.setMaxLength(1024);
		contentAdder.addChild(new ToggleableLayoutWidget(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage"), font).setMaxWidth(getWidth(1)).setCentered(false), () -> !chatRule.getHideMessage()), alignedLeft);
		chatMessageInput.addFormatter(createRenderTextProvider(chatMessageInput::getValue));
		chatMessageInput.setValue(chatRule.getChatMessage() != null ? chatRule.getChatMessage() : "");
		chatMessageInput.setResponder(chatRule::setChatMessage);
		chatMessageInput.setHint(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.chatMessage.@Placeholder").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		contentAdder.addChild(new ToggleableLayoutWidget(chatMessageInput, () -> !chatRule.getHideMessage()), 2);

		// Action Bar
		contentAdder.addChild(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.actionBar"), font), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		EditBox actionBarMessageInput = new EditBox(font, getWidth(2), 20, Component.empty());
		actionBarMessageInput.setMaxLength(1024);
		actionBarMessageInput.addFormatter(createRenderTextProvider(actionBarMessageInput::getValue));
		actionBarMessageInput.setValue(chatRule.getActionBarMessage() != null ? chatRule.getActionBarMessage() : "");
		actionBarMessageInput.setResponder(chatRule::setActionBarMessage);
		actionBarMessageInput.setHint(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		contentAdder.addChild(actionBarMessageInput, 2);

		// Announcement
		contentAdder.addChild(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement"), font), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		EditBox announcementMessageInput = new EditBox(font, getWidth(2), 20, Component.empty());
		announcementMessageInput.setMaxLength(1024);
		announcementMessageInput.addFormatter(createRenderTextProvider(announcementMessageInput::getValue));
		announcementMessageInput.setValue(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage().message : "");
		announcementMessageInput.setResponder(s -> {
			if (s.isEmpty()) {
				previousAnnouncementMessage = chatRule.getAnnouncementMessage();
				chatRule.setAnnouncementMessage(null);
			} else {
				if (chatRule.getAnnouncementMessage() == null) chatRule.setAnnouncementMessage(previousAnnouncementMessage != null ? previousAnnouncementMessage : new ChatRule.AnnouncementMessage());
				chatRule.getAnnouncementMessage().message = s;
			}
			recreateLayout();
		});
		announcementMessageInput.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.@Tooltip")));
		announcementMessageInput.setHint(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		contentAdder.addChild(announcementMessageInput, 2);

		contentAdder.addChild(SpacerElement.height(0), content.newCellSettings().paddingTop(0));
		RangedSliderWidget announcementDurationSlider = RangedSliderWidget.builder()
				.minMax(1, 10)
				.step(0.1)
				.width(getWidth(2))
				.defaultValue(chatRule.getAnnouncementMessage() != null ? chatRule.getAnnouncementMessage().displayDuration / 1000d : 5000d)
				.optionFormatter(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.announcement.duration"), d -> Component.literal(Formatters.FLOAT_NUMBERS.format(d) + 's'))
				.callback(d -> {
					if (chatRule.getAnnouncementMessage() == null) return;
					chatRule.getAnnouncementMessage().displayDuration = (long) (d * 1000);
				})
				.build();
		contentAdder.addChild(new ToggleableLayoutWidget(
				Util.make(new FrameLayout(), w -> w.addChild(announcementDurationSlider, p -> p.paddingTop(GRID_SPACING))),
				() -> chatRule.getAnnouncementMessage() != null
		), 2, content.newCellSettings().paddingTop(0));

		// Toast
		contentAdder.addChild(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast"), font), alignedLeft).setMaxWidth(getWidth(1)).setCentered(false);
		EditBox toastMessageInput = new EditBox(font, getWidth(2), 20, Component.empty());
		toastMessageInput.setMaxLength(1024);
		toastMessageInput.addFormatter(createRenderTextProvider(toastMessageInput::getValue));
		toastMessageInput.setValue(chatRule.getToastMessage() != null ? chatRule.getToastMessage().message : "");
		toastMessageInput.setResponder(s -> {
			if (s.isEmpty()) {
				previousToastMessage = chatRule.getToastMessage();
				chatRule.setToastMessage(null);
			} else {
				if (chatRule.getToastMessage() == null) chatRule.setToastMessage(previousToastMessage != null ? previousToastMessage : new ChatRule.ToastMessage());
				chatRule.getToastMessage().message = s;
			}
			recreateLayout();
		});
		toastMessageInput.setHint(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.outputField.@Placeholder").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		contentAdder.addChild(toastMessageInput, 2);

		BooleanSupplier toastOptionsPredicate = () -> chatRule.getToastMessage() != null;
		// Label + preview
		// Have to do something a little more custom due to the preview.
		FrameLayout textAndIcon = new FrameLayout(getWidth(1), 0);
		contentAdder.addChild(new ToggleableLayoutWidget(textAndIcon, toastOptionsPredicate));

		EditBox itemInput = new EditBox(font, getWidth(1), 20, Component.empty());
		ToastIconPreview preview = textAndIcon.addChild(new ToastIconPreview(itemInput), LayoutSettings::alignHorizontallyRight);
		textAndIcon.addChild(new MultiLineTextWidget(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.icon"), font), LayoutSettings::alignHorizontallyLeft).setMaxWidth(getWidth(1) - preview.getWidth()).setCentered(false);

		// Item input
		contentAdder.addChild(new ToggleableLayoutWidget(itemInput, toastOptionsPredicate));
		itemInput.setResponder(itemData -> {
			ItemStack stack = ItemStackComponentizationFixer.fromItemString(itemData, 1);
			if (stack.isEmpty()) stack = INVALID_ITEM;
			preview.stack = stack;
			ChatRule.ToastMessage message = chatRule.getToastMessage();
			if (message == null) return;
			message.icon = stack;
		});
		itemInput.setValue(chatRule.getToastMessage() != null ? getItemString(chatRule.getToastMessage().icon) : "minecraft:painting");

		// Duration slider
		RangedSliderWidget sliderWidget = RangedSliderWidget.builder()
				.minMax(1, 10)
				.step(0.1)
				.width(getWidth(1))
				.defaultValue(chatRule.getToastMessage() != null ? chatRule.getToastMessage().displayDuration / 1000d : 5000d)
				.optionFormatter(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.duration"), d -> Component.literal(Formatters.FLOAT_NUMBERS.format(d) + 's'))
				.callback(d -> {
					if (chatRule.getToastMessage() == null) return;
					chatRule.getToastMessage().displayDuration = (long) (d * 1000);
				})
				.build();
		contentAdder.addChild(new ToggleableLayoutWidget(sliderWidget, toastOptionsPredicate));
		sliderWidget.setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.duration.@Tooltip")));
		recreateLayout();
	}

	private static String getItemString(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()) + ItemStackComponentizationFixer.componentsAsString(stack);
	}

	private static EditBox.TextFormatter createRenderTextProvider(Supplier<String> fullTextSupplier) {
		return (s, start) -> visitor -> {
			String fullText = fullTextSupplier.get();
			char prefix = fullText.contains("§") ? '§' : '&';
			Style style = Style.EMPTY;
			for (int i = 0; i < fullText.length(); i++) {
				if (fullText.charAt(i) == prefix) {
					if (i + 1 < fullText.length()) {
						ChatFormatting formatting = ChatFormatting.getByCode(fullText.charAt(i + 1));
						if (formatting != null) {
							style = formatting == ChatFormatting.RESET ? Style.EMPTY : style.applyLegacyFormat(formatting);
						}
					}
				}
				int codePoint = fullText.codePointAt(i);
				if (i >= start && i < start + s.length()) {
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
		clearWidgets();
		repositionElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
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
		ChatRulesHandler.CHAT_RULE_LIST.getData().set(chatRuleIndex, chatRule);
	}

	private class ToastIconPreview extends AbstractWidget {
		private ItemStack stack = ItemStack.EMPTY;
		private final EditBox input;

		private ToastIconPreview(EditBox input) {
			super(0, 0, 16, 16, Component.empty());
			this.input = input;
			setTooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.toast.iconPreview.@Tooltip")));
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			context.renderFakeItem(stack, getX(), getY());
		}

		@Override
		public void setX(int x) {
			super.setX(x);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			super.onClick(click, doubled);
			Objects.requireNonNull(minecraft);
			minecraft.setScreen(new ItemSelectionPopup(ChatRuleConfigScreen.this, item -> {
				if (item == null) return;
				input.setValue(getItemString(item));
			}));
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	private class ContentContainer extends AbstractContainerWidget implements Layout {
		private static final int SIDE_PADDING = 10;
		private final List<AbstractWidget> children = new ArrayList<>();

		private ContentContainer() {
			super(0, 0, 0, 0, Component.empty());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		protected int contentHeight() {
			return content.getHeight();
		}

		@Override
		protected double scrollRate() {
			return 10;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);

			for (AbstractWidget clickableWidget : this.children) {
				clickableWidget.render(context, mouseX, mouseY, deltaTicks);
			}

			context.disableScissor();
			this.renderScrollbar(context, mouseX, mouseY);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}

		@Override
		public void visitChildren(Consumer<LayoutElement> consumer) {
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
			refreshScrollAmount();
		}

		@Override
		public void setScrollAmount(double scrollY) {
			super.setScrollAmount(scrollY);
			content.setY(getY() - (int) scrollAmount());
		}

		@Override
		public void arrangeElements() {
			content.arrangeElements();
			children.clear();
			content.visitWidgets(children::add);
			setWidth(content.getWidth() + SIDE_PADDING * 2);
			setHeight(layout.getContentHeight());
		}

		@Override
		public void visitWidgets(Consumer<AbstractWidget> consumer) {
			super.visitWidgets(consumer);
		}
	}
}
