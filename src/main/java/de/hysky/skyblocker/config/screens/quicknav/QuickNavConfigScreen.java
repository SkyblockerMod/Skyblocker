package de.hysky.skyblocker.config.screens.quicknav;

import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuickNavConfigScreen extends Screen {
	private static final int INVENTORY_WIDTH = 176;
	private static final int INVENTORY_HEIGHT = 166;
	private static final ConfigItemSupplier[] ITEM_SUPPLIERS = new ConfigItemSupplier[]{
			config -> config.button1,
			config -> config.button2,
			config -> config.button3,
			config -> config.button4,
			config -> config.button5,
			config -> config.button6,
			config -> config.button7,
			config -> config.button8,
			config -> config.button9,
			config -> config.button10,
			config -> config.button11,
			config -> config.button12,
			config -> config.button13,
			config -> config.button14
	};
	private static final ConfigItemSetter[] SETTERS = new ConfigItemSetter[]{
			(config, item) -> config.button1 = item,
			(config, item) -> config.button2 = item,
			(config, item) -> config.button3 = item,
			(config, item) -> config.button4 = item,
			(config, item) -> config.button5 = item,
			(config, item) -> config.button6 = item,
			(config, item) -> config.button7 = item,
			(config, item) -> config.button8 = item,
			(config, item) -> config.button9 = item,
			(config, item) -> config.button10 = item,
			(config, item) -> config.button11 = item,
			(config, item) -> config.button12 = item,
			(config, item) -> config.button13 = item,
			(config, item) -> config.button14 = item
	};

	private final QuickNavConfigButton[] buttons = new QuickNavConfigButton[ITEM_SUPPLIERS.length];
	private final @Nullable Screen parent;
	private @Nullable QuickNavConfigButton highlightedButton;
	private final @Nullable LivingEntity entityToRender;

	public QuickNavConfigScreen() {
		this(null);
	}

	public QuickNavConfigScreen(@Nullable Screen parent) {
		super(Component.literal("Quick Navigation Config"));
		if (Math.random() < 0.001 && minecraft.level != null) {
			entityToRender = new Cat(EntityTypes.CAT, minecraft.level);
			entityToRender.setId("meow".hashCode());
		} else {
			entityToRender = minecraft.player;
		}
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		for (int i = 0; i < buttons.length; i++) {
			QuickNavigationConfig.QuickNavItem item = ITEM_SUPPLIERS[i].apply(SkyblockerConfigManager.get().quickNav);
			buttons[i] = addWidget(new QuickNavConfigButton(
					item.render,
					i,
					ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), Math.clamp(item.itemData.count, 1, 99), item.itemData.components),
					item.tooltip
			));
		}
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		for (QuickNavConfigButton button : buttons) {
			button.setPositionFrom((width - INVENTORY_WIDTH) / 2, (height - INVENTORY_HEIGHT) / 2, INVENTORY_WIDTH, INVENTORY_HEIGHT);
		}
	}

	private void refreshButton(int index) {
		QuickNavigationConfig.QuickNavItem item = ITEM_SUPPLIERS[index].apply(SkyblockerConfigManager.get().quickNav);
		removeWidget(buttons[index]);
		buttons[index] = addWidget(new QuickNavConfigButton(
				item.render,
				index,
				ItemStackComponentizationFixer.fromComponentsString(item.itemData.item.toString(), Math.clamp(item.itemData.count, 1, 99), item.itemData.components),
				item.tooltip
		));
		repositionElements();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.centeredText(font, "Right click to disable/enable buttons.", width / 2, 4, CommonColors.GRAY);
		graphics.centeredText(font, "Left click to edit button options.", width / 2, 4 + font.lineHeight, CommonColors.GRAY);
		QuickNavConfigButton dragged = null;
		for (QuickNavConfigButton button : buttons) {
			if (button.dragging && dragged == null) {
				dragged = button;
				continue;
			}
			button.extractRenderState(graphics, mouseX, mouseY, a);
		}
		if (highlightedButton != null) {
			graphics.fill(highlightedButton.getX(), highlightedButton.getY(), highlightedButton.getRight(), highlightedButton.getBottom(), ARGB.color(0.3f, CommonColors.YELLOW));
		}
		// render dragged in front of everything
		if (dragged != null) dragged.extractRenderState(graphics, mouseX, mouseY, a);
		if (entityToRender != null) {
			int xo = (width - INVENTORY_WIDTH) / 2;
			int yo = (height - INVENTORY_HEIGHT) / 2;
			InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, xo + 26, yo + 8, xo + 75, yo + 78, 30, 0.0625F, mouseX, mouseY, entityToRender);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		int backgroundX = (width - INVENTORY_WIDTH) / 2;
		int backgroundY = (height - INVENTORY_HEIGHT) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, InventoryScreen.INVENTORY_LOCATION, backgroundX, backgroundY, 0.0F, 0.0F, INVENTORY_WIDTH, INVENTORY_HEIGHT, 256, 256);
	}

	@Override
	public void removed() {
		super.removed();
		SkyblockerConfigManager.update(_ -> {});
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}

	private class QuickNavConfigButton extends QuickNavButton {
		private boolean enabled;
		private double dragX, dragY;
		private boolean dragging;

		/**
		 * Constructs a new QuickNavButton with the given parameters.
		 *
		 * @param index   the index of the button.
		 * @param icon    the icon to display on the button.
		 * @param tooltip the tooltip to show when hovered
		 */
		public QuickNavConfigButton(boolean enabled, int index, ItemStack icon, String tooltip) {
			super(index, true, "", icon, tooltip);
			this.enabled = enabled;
			setRenderInFront(true);
		}

		@Override
		protected void updateCoordinates() {}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
				enabled = !enabled;
				SkyblockerConfigManager.updateOnly(config -> ITEM_SUPPLIERS[index].apply(config.quickNav).render = enabled);
			}
			dragX = click.x();
			dragY = click.y();
		}

		@Override
		protected void setPositionFrom(int backgroundX, int backgroundY, int imageWidth, int imageHeight) {
			super.setPositionFrom(backgroundX, backgroundY, imageWidth, imageHeight);
		}

		@Override
		protected void onDrag(MouseButtonEvent event, double dx, double dy) {
			super.onDrag(event, dx, dy);
			if (!dragging && enabled) {
				double diffX = event.x() - dragX;
				double diffY = event.y() - dragY;
				if (diffX * diffX + diffY * diffY > 10 * 10) {
					dragging = true;
					setTooltip(null);
				}
			}
			if (!dragging) return;
			setPosition((int) (event.x() - getWidth() / 2d), (int) (event.y() - getHeight() / 2d));
			highlightedButton = null;
			for (QuickNavConfigButton button : buttons) {
				if (button == this) continue;
				if (button.isMouseOver(event.x(), event.y())) {
					highlightedButton = button;
					break;
				}
			}
			if (highlightedButton != null) setTooltip(Tooltip.create(Component.literal("Release to swap")));
			else setTooltip(null);
		}

		@Override
		public void onRelease(MouseButtonEvent event) {
			super.onRelease(event);
			if (dragging) {
				if (highlightedButton != null) {
					int i1 = highlightedButton.index;
					int i2 = this.index;
					SkyblockerConfigManager.updateOnly(config -> {
						QuickNavigationConfig.QuickNavItem item1 = ITEM_SUPPLIERS[i1].apply(config.quickNav);
						QuickNavigationConfig.QuickNavItem item2 = ITEM_SUPPLIERS[i2].apply(config.quickNav);
						SETTERS[i1].accept(config.quickNav, item2);
						SETTERS[i2].accept(config.quickNav, item1);
					});
					refreshButton(i1);
					refreshButton(i2);
					highlightedButton = null;
				} else {
					repositionElements();
				}
				dragging = false;
			} else {
				minecraft.gui.setScreen(new ItemEditPopup(QuickNavConfigScreen.this, () -> refreshButton(index), ITEM_SUPPLIERS[index].apply(SkyblockerConfigManager.get().quickNav), SETTERS[index], index));
			}
			setTooltip(tooltip);
		}

		@Override
		public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			if (enabled) {
				super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
			}
		}

		@Override
		protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
			return super.isValidClickButton(buttonInfo) || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
		}
	}

	// Little interface because type inference is wacky on arrays
	@FunctionalInterface
	interface ConfigItemSupplier extends Function<QuickNavigationConfig, QuickNavigationConfig.QuickNavItem> {}

	@FunctionalInterface
	interface ConfigItemSetter extends BiConsumer<QuickNavigationConfig, QuickNavigationConfig.QuickNavItem> {}
}
