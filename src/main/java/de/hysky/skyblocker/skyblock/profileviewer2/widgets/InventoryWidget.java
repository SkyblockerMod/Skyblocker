package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.List;

import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import de.hysky.skyblocker.utils.render.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class InventoryWidget extends AbstractWidget implements HoveredItemStackProvider {
	private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final int HOTBAR_SEPARATOR_HEIGHT = 4;
	private final int rows;
	private final int columns;
	private final List<List<ItemStack>> pages;
	private final boolean hasHotbar;
	private int index = 0;

	public InventoryWidget(Component label, int rows, int columns, List<List<ItemStack>> pages, boolean hasHotbar) {
		// Each slot is 18x18 px in size
		// (7 * 2) is the width on each side
		int width = columns * 18 + (7 * 2);
		// 17 + 7 is the top height plus the bottom height
		int height = (rows * 18 + 17 + 7) + (hasHotbar ? HOTBAR_SEPARATOR_HEIGHT : 0);

		super(0, 0, width, height, label);
		this.rows = rows;
		this.columns = Math.max(columns, 3);
		this.pages = pages;
		this.hasHotbar = hasHotbar;

		// Make the widget ignore clicks
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Font font = Minecraft.getInstance().font;
		List<Component> tooltip = null;
		Identifier tooltipStyle = null;
		int x = this.getX();
		int y = this.getY();

		// Draw background
		if (this.hasHotbar) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y + this.rows + 10, 0, 136, this.columns * 18 + 7, this.rows * 18 + 17, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x + this.columns * 18 + 7, y, 169, 0, 7, this.rows * 18 + 21, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y, 0, 0, this.columns * 18 + 7, 14, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x + this.columns * 18 + 7, y + this.rows * 18 + 21, 169, 215, 7, 7, 256, 256);
		} else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y, 0, 0, this.columns * 18 + 7, this.rows * 18 + 17, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x + this.columns * 18 + 7, y, 169, 0, 7, this.rows * 18 + 17, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, y + this.rows * 18 + 17, 0, 215, this.columns * 18 + 7, 7, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x + this.columns * 18 + 7, y + this.rows * 18 + 17, 169, 215, 7, 7, 256, 256);
		}

		// Draw label
		graphics.text(font, this.getMessage(), x + 8, y + 6, 0xFF404040, false);

		// Draw Items
		List<ItemStack> stacks = this.pages.get(this.index);

		for (int i = 0; i < stacks.size(); i++) {
			int column = i % this.columns;
			int row = i / this.columns;
			int hotbarYShift = this.hasHotbar && this.rows > 1 && row + 1 == this.rows ? HOTBAR_SEPARATOR_HEIGHT : 0;

			ItemStack stack = stacks.get(i);
			int itemX = x + column * 18 + 8;
			int itemY = (y + row * 18 + 18) + hotbarYShift;

			// Draw item (rarity) backgrounds
			ItemBackgroundManager.drawBackgrounds(stack, graphics, itemX, itemY);

			// Draw item protection star icon
			if (ItemProtection.isItemProtected(stack)) {
				ItemProtection.drawSlotIcon(graphics, itemX, itemY);
			}

			// Draw item & item/slot decorations
			graphics.fakeItem(stack, itemX, itemY);
			graphics.itemDecorations(font, stack, itemX, itemY);
			SlotTextManager.extractSlotText(graphics, font, null, stack, i, itemX, itemY);

			if (!stack.isEmpty() && GuiHelper.pointIsInArea(mouseX, mouseY, itemX - 1, itemY - 1, itemX + GuiRenderer.DEFAULT_ITEM_SIZE + 1, itemY + GuiRenderer.DEFAULT_ITEM_SIZE + 1)) {
				tooltip = Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
				tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);
			}
		}

		// Draw Tooltip
		if (tooltip != null) {
			graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY, tooltipStyle);
		}
	}

	public void forwards() {
		this.index = Math.clamp(this.index + 1, 0, this.pages.size() - 1);
	}

	public void backwards() {
		this.index = Math.clamp(this.index - 1, 0, this.pages.size() - 1);
	}

	public int getPage() {
		return this.index + 1;
	}

	public int getMaxPages() {
		return this.pages.size();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}

	@Override
	public @Nullable ItemStack getFocusedItem() {
		// NYI
		return null;
	}
}
