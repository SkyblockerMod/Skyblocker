package de.hysky.skyblocker.config.screens.quicknav;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.command.argumenttypes.ArgumentWithAlternatives;
import de.hysky.skyblocker.utils.command.suggestions.TextFieldSuggestions;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.AutocompleteEditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

class ItemEditPopup extends AbstractPopupScreen {

	private final Runnable onClose;
	private final QuickNavigationConfig.QuickNavItem item;
	private final QuickNavConfigScreen.ConfigItemSetter setter;
	private final LinearLayout layout = LinearLayout.vertical().spacing(4);

	private int currentCount;
	private String currentTooltip;

	protected ItemEditPopup(Screen backgroundScreen, Runnable onClose, QuickNavigationConfig.QuickNavItem item, QuickNavConfigScreen.ConfigItemSetter setter) {
		super(Component.literal("Edit button or something"), backgroundScreen);
		this.onClose = onClose;
		this.item = item;
		this.setter = setter;
		currentCount = item.itemData.count;
		currentTooltip = item.tooltip;
	}

	@Override
	protected void init() {
		CommandBuildContext context = TextFieldSuggestions.getContext();
		// tooltip
		layout.addChild(new StringWidget(Component.literal("Tooltip"), font));
		AutocompleteEditBox.Argument<Component> tooltipBox = AutocompleteEditBox.builder().width(300).autoTrim(false).buildArg(
				minecraft, font, this, Component.empty(),
				ArgumentWithAlternatives.of(ComponentArgument.textComponent(context), StringArgumentType.greedyString(), Component::literal)
		);
		tooltipBox.setMaxLength(4096);
		tooltipBox.setValue(item.tooltip);
		tooltipBox.setResponder(s -> tooltipBox.getParsedValue().ifPresent(_ -> currentTooltip = s));
		layout.addChild(tooltipBox);


		// item selection
		GridLayout itemLayout = layout.addChild(new GridLayout()).columnSpacing(4).rowSpacing(2);

		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), item.itemData.count, item.itemData.components);
		ItemWidget itemWidget = itemLayout.addChild(new ItemWidget(stack), 1, 0, l -> l.alignVerticallyMiddle().alignHorizontallyCenter());
		int itemWidth = 250;
		itemLayout.addChild(new StringWidget(Component.literal("Item"), font), 0, 1).setMaxWidth(itemWidth, StringWidget.TextOverflow.SCROLLING);
		AutocompleteEditBox.Argument<ItemInput> itemBox = AutocompleteEditBox.builder().width(250).buildArg(
				minecraft, font, this, Component.empty(),
				new ItemArgument(context)
		);
		itemLayout.addChild(itemBox, 1, 1);
		itemBox.setMaxLength(4096);
		itemBox.setValue(item.itemData.item + item.itemData.components);

		int countWidth = 30;
		itemLayout.addChild(new StringWidget(Component.literal("Count"), font), 0, 2).setMaxWidth(countWidth, StringWidget.TextOverflow.SCROLLING);
		AutocompleteEditBox.Argument<Integer> countBox = AutocompleteEditBox.builder().width(20).buildArg(
				minecraft, font, this, Component.empty(),
				IntegerArgumentType.integer(1)
		);
		itemLayout.addChild(countBox, 1, 2);
		countBox.setMaxLength(2);
		countBox.setValue(String.valueOf(item.itemData.count));

		layout.visitWidgets(this::addRenderableWidget);
		itemBox.setValueResponder(itemInput -> itemWidget.itemStack = new ItemStack(itemInput.item(), currentCount, itemInput.components()));
		countBox.setValueResponder(count -> {
			currentCount = Math.max(count, 1);
			itemWidget.itemStack = itemWidget.itemStack.copyWithCount(currentCount);
		});
		super.init();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractPopupBackground(graphics, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
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
