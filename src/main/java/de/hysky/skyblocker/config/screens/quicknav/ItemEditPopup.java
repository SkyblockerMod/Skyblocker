package de.hysky.skyblocker.config.screens.quicknav;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.command.argumenttypes.RegexArgumentType;
import de.hysky.skyblocker.utils.command.suggestions.TextFieldSuggestions;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.SuggestionsEditBox;
import de.hysky.skyblocker.utils.render.gui.ComponentEditWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Pattern;

class ItemEditPopup extends AbstractPopupScreen {

	private final Runnable onClose;
	private final QuickNavigationConfig.QuickNavItem item;
	private final QuickNavConfigScreen.ConfigItemSetter setter;
	private final LinearLayout layout = LinearLayout.vertical().spacing(10);

	private int currentCount;
	private Component currentTooltip;

	protected ItemEditPopup(Screen backgroundScreen, Runnable onClose, QuickNavigationConfig.QuickNavItem item, QuickNavConfigScreen.ConfigItemSetter setter) {
		super(Component.literal("Edit button or something"), backgroundScreen);
		this.onClose = onClose;
		this.item = item;
		this.setter = setter;
		currentCount = item.itemData.count;
		try {
			currentTooltip = ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(item.tooltip, JsonElement.class)).getOrThrow().getFirst();
		} catch (Exception e) {
			currentTooltip = Component.literal(item.tooltip);
		}
	}

	@Override
	protected void init() {
		layout.defaultCellSetting().padding(3);
		CommandBuildContext context = TextFieldSuggestions.getContext();
		LinearLayout commandLayout = layout.addChild(LinearLayout.vertical().spacing(2));
		addTitle(commandLayout, "Tooltip");
		SuggestionsEditBox commandBox = SuggestionsEditBox.builder().autoTrim(false).width(250).onlyShowIfCursorPastError(false).build(
				minecraft, font, this, Component.empty(),
				minecraft.player.connection.getCommands().getRoot()
		);
		commandLayout.addChild(commandBox);
		renderAroundLayout(commandLayout);

		// tooltip
		LinearLayout tooltipLayout = layout.addChild(LinearLayout.vertical().spacing(2));
		addTitle(tooltipLayout, "Tooltip");
		ComponentEditWidget editWidget = new ComponentEditWidget(this, Component.literal("Customize Tooltip"), component -> currentTooltip = component.copy());
		tooltipLayout.addChild(editWidget);
		editWidget.setText(currentTooltip.copy(), false);
		renderAroundLayout(tooltipLayout);

		// menu regex
		LinearLayout regexLayout = layout.addChild(LinearLayout.vertical().spacing(2));
		addTitle(regexLayout, "Menu Title");
		SuggestionsEditBox.Argument<Pattern> patternBox = SuggestionsEditBox.builder().autoTrim(false).width(250).onlyShowIfCursorPastError(false).buildArg(
				minecraft, font, this, Component.empty(),
				new RegexArgumentType()
		);
		patternBox.setTooltip(Tooltip.create(Component.literal("The button will appear pressed in the menu matching this title. This supports Regex!")));
		patternBox.setMaxLength(2048);
		patternBox.setValue(item.uiTitle);
		regexLayout.addChild(patternBox);
		renderAroundLayout(regexLayout);


		// item selection
		LinearLayout iconLayout = layout.addChild(LinearLayout.vertical().spacing(2));
		addTitle(iconLayout, "Icon");
		GridLayout itemLayout = iconLayout.addChild(new GridLayout()).columnSpacing(4).rowSpacing(2);

		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), item.itemData.count, item.itemData.components);
		ItemWidget itemWidget = itemLayout.addChild(new ItemWidget(stack), 1, 0, l -> l.alignVerticallyMiddle().alignHorizontallyCenter());
		int itemWidth = 250;
		itemLayout.addChild(new StringWidget(Component.literal("Item"), font), 0, 1).setMaxWidth(itemWidth, StringWidget.TextOverflow.SCROLLING);
		SuggestionsEditBox.Argument<ItemInput> itemBox = SuggestionsEditBox.builder().width(250).buildArg(
				minecraft, font, this, Component.empty(),
				new ItemArgument(context)
		);
		itemLayout.addChild(itemBox, 1, 1);
		itemBox.setMaxLength(4096);
		itemBox.setValue(item.itemData.item + item.itemData.components);

		int countWidth = 30;
		itemLayout.addChild(new StringWidget(Component.literal("Count"), font), 0, 2).setMaxWidth(countWidth, StringWidget.TextOverflow.SCROLLING);
		SuggestionsEditBox.Argument<Integer> countBox = SuggestionsEditBox.builder().width(20).buildArg(
				minecraft, font, this, Component.empty(),
				IntegerArgumentType.integer(1)
		);
		itemLayout.addChild(countBox, 1, 2);
		countBox.setMaxLength(2);
		countBox.setValue(String.valueOf(item.itemData.count));
		renderAroundLayout(iconLayout);

		layout.visitWidgets(this::addRenderableWidget);
		itemBox.setValueResponder(itemInput -> itemWidget.itemStack = new ItemStack(itemInput.item(), currentCount, itemInput.components()));
		countBox.setValueResponder(count -> {
			currentCount = Math.max(count, 1);
			itemWidget.itemStack = itemWidget.itemStack.copyWithCount(currentCount);
		});
		super.init();
	}

	private void addTitle(LinearLayout layout, String title) {
		layout.addChild(new StringWidget(Component.literal(title).withStyle(ChatFormatting.BOLD), font), l -> l.paddingBottom(4));
	}

	private void renderAroundLayout(Layout target) {
		addRenderableOnly(((graphics, _, _, _) -> graphics.fill(layout.getX(), target.getY() - 3, layout.getX() + layout.getWidth(), target.getY() + target.getHeight() + 3, ARGB.black(0.15f))));
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
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void onClose() {
		super.onClose();
		onClose.run();
	}

	private static class ItemWidget extends AbstractWidget {
		private ItemStack itemStack;

		private ItemWidget(ItemStack stack) {
			super(0, 0, 16, 16, stack.getItemName());
			this.itemStack = stack;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.item(itemStack, getX(), getY());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {

		}
	}
}
