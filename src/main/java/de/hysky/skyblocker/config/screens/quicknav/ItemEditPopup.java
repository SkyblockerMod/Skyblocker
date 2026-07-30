package de.hysky.skyblocker.config.screens.quicknav;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ButtonWidget;
import de.hysky.skyblocker.utils.command.CommandUtils;
import de.hysky.skyblocker.utils.command.argumenttypes.RegexArgumentType;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.ComponentEditWidget;
import de.hysky.skyblocker.utils.render.gui.ItemSelectionPopup;
import de.hysky.skyblocker.utils.render.gui.SuggestionsEditBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.regex.Pattern;

class ItemEditPopup extends AbstractPopupScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SCROLLABLE_CONTENT_HEIGHT_DIFF = BACKGROUND_MARGIN * 2 + 20 + 50; // 20: height of buttons, 50: some hardcoded constant

	private final Runnable onClose;
	private final QuickNavigationConfig.QuickNavItem item;
	private final QuickNavConfigScreen.ConfigItemSetter setter;
	private final LinearLayout layout = LinearLayout.vertical().spacing(2);
	private ScrollableLayout scrollableContent = new ScrollableLayout(minecraft, LinearLayout.vertical(), 0); // placeholder

	private Component currentTooltip;

	ItemEditPopup(Screen backgroundScreen, Runnable onClose, QuickNavigationConfig.QuickNavItem item, QuickNavConfigScreen.ConfigItemSetter setter, int index) {
		super(Component.translatable("skyblocker.config.quickNav.screen.title", index + 1).withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE), backgroundScreen);
		this.onClose = onClose;
		this.item = new QuickNavigationConfig.QuickNavItem(item);
		this.setter = setter;
		try {
			currentTooltip = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(item.tooltip, JsonElement.class)).getOrThrow().getFirst();
		} catch (Exception e) {
			currentTooltip = Component.literal(item.tooltip);
		}
	}

	@Override
	protected void init() {
		layout.addChild(new StringWidget(getTitle(), font), l -> l.alignHorizontallyCenter().paddingTop(2).paddingBottom(6));
		LinearLayout content = LinearLayout.vertical().spacing(10);
		content.defaultCellSetting().padding(3);
		CommandBuildContext context = CommandUtils.newContext();
		LinearLayout commandLayout = content.addChild(createSectionLayout());
		// click event
		addTitle(commandLayout, "skyblocker.config.quickNav.button.clickEvent");
		EditBox commandBox = SuggestionsEditBox.builder().width(250).buildVanillaDispatcher(
				minecraft, font, this, Component.empty(),
				false);
		commandBox.setValue(item.clickEvent);
		commandBox.setResponder(s -> item.clickEvent = s);
		commandLayout.addChild(commandBox);

		// tooltip
		LinearLayout tooltipLayout = content.addChild(createSectionLayout());
		addTitle(tooltipLayout, "skyblocker.config.quickNav.button.tooltip");
		ComponentEditWidget editWidget = new ComponentEditWidget(this, Component.literal("Customize Tooltip"), component -> currentTooltip = component.copy());
		tooltipLayout.addChild(editWidget);
		editWidget.setText(currentTooltip.copy(), false);

		// menu regex
		LinearLayout regexLayout = content.addChild(createSectionLayout());
		addTitle(regexLayout, "skyblocker.config.quickNav.button.uiTitle");
		SuggestionsEditBox.Argument<Pattern> patternBox = SuggestionsEditBox.builder().width(250).onlyShowIfCursorPastError(false).buildArg(
				minecraft, font, this, Component.empty(),
				new RegexArgumentType()
		);
		patternBox.setTooltip(Tooltip.create(Component.literal("The button will appear pressed in the menu matching this title.\nThis supports Regex!\nCan be left empty if button doesn't open a menu.")));
		patternBox.setMaxLength(2048);
		patternBox.setValue(item.uiTitle);
		patternBox.setValueResponder(p -> item.uiTitle = p.pattern().isBlank() ? "lorem ipsum" : p.pattern());
		regexLayout.addChild(patternBox);

		// item selection
		LinearLayout iconLayout = content.addChild(createSectionLayout());
		addTitle(iconLayout, "skyblocker.config.quickNav.button.icon");
		GridLayout itemLayout = iconLayout.addChild(new GridLayout()).columnSpacing(4).rowSpacing(2);

		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), item.itemData.count, item.itemData.components);
		ItemWidget itemWidget = itemLayout.addChild(new ItemWidget(stack), 1, 0, l -> l.alignVerticallyMiddle().alignHorizontallyCenter());
		int itemWidth = 250;
		itemLayout.addChild(new StringWidget(Component.translatable("skyblocker.config.quickNav.button.item.itemName"), font), 0, 1).setMaxWidth(itemWidth, StringWidget.TextOverflow.SCROLLING);
		SuggestionsEditBox.Argument<ItemInput> itemBox = SuggestionsEditBox.builder().width(250).buildArg(
				minecraft, font, this, Component.empty(),
				new ItemArgument(context)
		);
		itemLayout.addChild(itemBox, 1, 1);
		itemBox.setMaxLength(4096);
		itemBox.setValue(item.itemData.item + item.itemData.components);
		itemBox.setValueResponder(itemInput -> {
			ItemStack itemStack = new ItemStack(itemInput.item(), item.itemData.count, itemInput.components());
			itemWidget.stack = itemStack;
			item.itemData.item = itemStack.getItem();
			item.itemData.components = ItemStackComponentizationFixer.componentsAsString(itemStack);
		});

		int countWidth = 30;
		itemLayout.addChild(new StringWidget(Component.translatable("skyblocker.config.quickNav.screen.count"), font), 0, 2).setMaxWidth(countWidth, StringWidget.TextOverflow.SCROLLING);
		SuggestionsEditBox.Argument<Integer> countBox = SuggestionsEditBox.builder().width(20).buildArg(
				minecraft, font, this, Component.empty(),
				IntegerArgumentType.integer(1)
		);
		itemLayout.addChild(countBox, 1, 2);
		countBox.setMaxLength(2);
		countBox.setValue(String.valueOf(item.itemData.count));
		countBox.setValueResponder(count -> {
			item.itemData.count = Math.max(count, 1);
			itemWidget.stack = itemWidget.stack.copyWithCount(item.itemData.count);
		});

		iconLayout.addChild(ButtonWidget.builder(Component.translatable("skyblocker.config.quickNav.button.chooseSkyblockItem"), _ -> minecraft.gui.setScreen(
				new ItemSelectionPopup(this, itemStack -> {
					if (itemStack != null) {
						itemWidget.stack = itemStack;
						item.itemData.item = itemStack.getItem();
						String components = ItemStackComponentizationFixer.componentsAsString(itemStack);
						item.itemData.components = components;
						itemBox.setValue(itemStack.getItem() + components);
					}
				}))).tooltip(Tooltip.create(Component.translatable("skyblocker.config.quickNav.button.chooseSkyblockItem.@Tooltip"))).build());

		// require double click
		LinearLayout doubleClickLayout = content.addChild(createSectionLayout());
		doubleClickLayout.addChild(Checkbox.builder(Component.translatable("skyblocker.config.quickNav.button.doubleClick"), font)
				.onValueChange((_, value) -> item.doubleClick = value)
				.selected(item.doubleClick)
				.build()
		).setTooltip(Tooltip.create(Component.translatable("skyblocker.config.quickNav.button.doubleClick.@Tooltip")));

		content.addChild(SpacerElement.height(0));
		scrollableContent = layout.addChild(new ScrollableLayout(minecraft, content, height - SCROLLABLE_CONTENT_HEIGHT_DIFF));

		// the buttons at the bottom
		LinearLayout buttonsLayout = layout.addChild(LinearLayout.horizontal().spacing(4), LayoutSettings::alignHorizontallyCenter);
		buttonsLayout.addChild(ButtonWidget.builder(CommonComponents.GUI_CANCEL, _ -> onClose()).build());
		buttonsLayout.addChild(ButtonWidget.builder(CommonComponents.GUI_DONE, _ -> {
			save();
			onClose();
		}).build());

		layout.visitWidgets(this::addRenderableWidget);
		super.init();
	}

	private void addTitle(LinearLayout layout, @Translatable String title) {
		layout.addChild(new StringWidget(Component.translatable(title).withStyle(ChatFormatting.BOLD), font), l -> l.paddingBottom(4));
	}

	private LinearLayout createSectionLayout() {
		LinearLayout linearLayout = LinearLayout.vertical().spacing(2);
		linearLayout.addChild(new BackgroundRender(linearLayout, layout));
		return linearLayout;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractLighterPopupBackground(graphics, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		scrollableContent.arrangeElements();
		scrollableContent.setMaxHeight(height - SCROLLABLE_CONTENT_HEIGHT_DIFF);
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void onClose() {
		super.onClose();
		onClose.run();
	}

	private void save() {
		item.tooltip = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, currentTooltip)
				.ifError(error -> LOGGER.error("Failed to serialize component! {}", error.message())).result()
				.map(SkyblockerMod.GSON_COMPACT::toJson).orElse(currentTooltip.getString());
		SkyblockerConfigManager.updateOnly(config -> setter.accept(config.quickNav, item));
	}

	private static class ItemWidget extends AbstractWidget {
		private ItemStack stack;

		private ItemWidget(ItemStack stack) {
			super(0, 0, 16, 16, stack.getItemName());
			this.stack = stack;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.item(stack, getX(), getY());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {}
	}

	private static class BackgroundRender extends AbstractWidget {
		private final LayoutElement heightLayout;
		private final LayoutElement widthLayout;

		public BackgroundRender(LayoutElement heightLayout, LayoutElement widthLayout) {
			super(0, 0, 0, 0, Component.empty());
			active = false;
			this.heightLayout = heightLayout;
			this.widthLayout = widthLayout;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.fill(widthLayout.getX(), heightLayout.getY() - 3, widthLayout.getX() + widthLayout.getWidth(), heightLayout.getY() + heightLayout.getHeight() + 3, ARGB.black(0.15f));
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {}
	}
}
