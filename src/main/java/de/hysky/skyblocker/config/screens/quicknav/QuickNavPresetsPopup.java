package de.hysky.skyblocker.config.screens.quicknav;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;

class QuickNavPresetsPopup extends AbstractPopupScreen {
	private final LinearLayout layout = LinearLayout.vertical().spacing(2);
	private final Consumer<QuickNavigationConfig.QuickNavItem> consumer;

	QuickNavPresetsPopup(Screen backgroundScreen, Consumer<QuickNavigationConfig.QuickNavItem> consumer) {
		super(Component.literal("Presets").withStyle(ChatFormatting.BOLD), backgroundScreen);
		layout.defaultCellSetting().alignHorizontallyCenter();
		this.consumer = consumer;
	}

	@Override
	protected void init() {
		layout.addChild(new StringWidget(getTitle(), font));
		layout.addChild(new PresetsList(minecraft, i -> {
			consumer.accept(i);
			onClose();
		}));
		layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose()).build());
		layout.visitWidgets(this::addRenderableWidget);
		layout.arrangeElements();
		super.init();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractPopupBackground(graphics, layout.getX(),  layout.getY(), layout.getWidth(), layout.getHeight(), true);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	private static class PresetsList extends ContainerObjectSelectionList<PresetEntry> {

		private PresetsList(Minecraft minecraft, Consumer<QuickNavigationConfig.QuickNavItem> onClick) {
			super(minecraft, 250, 150, 0, 24);
			for (QuickNavigationConfig.QuickNavItem preset : QuickNavigationConfig.Presets.ALL_PRESETS) {
				addEntry(new PresetEntry(preset, () -> onClick.accept(preset)));
			}
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			refreshScrollAmount();
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			refreshScrollAmount();
		}
	}

	// very useful class
	private static class PresetEntry extends ContainerObjectSelectionList.Entry<PresetEntry> {
		private final Button button;

		private PresetEntry(QuickNavigationConfig.QuickNavItem item, Runnable onClick) {
			this.button = new PresetButton(item, _ -> onClick.run());
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			button.setPosition(getContentX(), getContentY());
			button.setSize(getContentWidth(), getContentHeight());
			button.extractRenderState(graphics, mouseX, mouseY, a);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(button);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(button);
		}
	}

	private static class PresetButton extends Button {
		private final QuickNavigationConfig.QuickNavItem item;
		private final ItemStack itemStack;

		private PresetButton(QuickNavigationConfig.QuickNavItem item, OnPress onPress) {
			super(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, item.getParsedTooltip(), onPress, DEFAULT_NARRATION);
			this.item = item;
			setTooltip(Tooltip.create(getMessage()));
			itemStack = ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), item.itemData.count, item.itemData.components);
		}
		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			extractDefaultSprite(graphics);
			final int edgePadding = 4;
			final int itemAreaWidth = 20;
			final int textSeparation = 4;
			graphics.item(itemStack, getX() + edgePadding, getY() + 2);
			final int availableWidth = (getWidth() - edgePadding * 2 - itemAreaWidth - textSeparation) / 2;
			Font font = Minecraft.getInstance().font;
			ActiveTextCollector textRenderer = graphics.textRenderer();
			int tooltipWidth = font.width(getMessage());
			final int tooltipLeft = getX() + edgePadding + itemAreaWidth;
			if (tooltipWidth > availableWidth) {
				textRenderer.acceptScrollingWithDefaultCenter(getMessage(), tooltipLeft, tooltipLeft + availableWidth, getY(), getBottom());
			} else {
				textRenderer.accept(tooltipLeft, getY() + 5, getMessage());
			}
			int commandWidth = font.width(item.clickEvent);
			final int commandLeft = getX() + edgePadding + itemAreaWidth + availableWidth + textSeparation;
			if (commandWidth > availableWidth) {
				textRenderer.acceptScrollingWithDefaultCenter(Component.literal(item.clickEvent), commandLeft, commandLeft + availableWidth, getY(), getBottom());
			} else {
				textRenderer.accept(commandLeft, getY() + 5, Component.literal(item.clickEvent));
			}

		}
	}
}
