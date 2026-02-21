package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class PageTabWidget extends ProfileViewerWidget {
	private static final Identifier TEXTURE_1_UNSELECTED = Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1");
	private static final Identifier TEXTURE_1_SELECTED = Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_1");
	private static final Identifier TEXTURE_2_UNSELECTED = Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2");
	private static final Identifier TEXTURE_2_SELECTED = Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_2");
	private static final int WIDTH = 26;
	private static final int HEIGHT = 32;
	/** Required for the button to be aligned with the background texture. */
	private static final int HEIGHT_OFFSET = 4;
	private static final int ITEM_SIZE = 16;
	private final ItemStack icon;
	private final int index;
	private final IntConsumer pageSwitcher;
	private boolean selected = false;

	public PageTabWidget(ItemStack icon, int index, IntConsumer pageSwitcher) {
		super(0 + WIDTH * index, -HEIGHT + HEIGHT_OFFSET, WIDTH, HEIGHT, false, Component.empty());
		this.icon = icon;
		this.index = index;
		this.pageSwitcher = pageSwitcher;
		this.active = true;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		this.pageSwitcher.accept(this.index);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		Identifier textureUnselected = this.index == 0 ? TEXTURE_1_UNSELECTED : TEXTURE_2_UNSELECTED;
		Identifier textureSelected = this.index == 0 ? TEXTURE_1_SELECTED : TEXTURE_2_SELECTED;
		Identifier texture = this.selected ? textureSelected : textureUnselected;
		// Offset the y of the icon when selected by two to match the visual (only) height difference between unselected and selected;
		int iconYOffset = this.selected ? -2 : 0;

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		graphics.renderFakeItem(this.icon, this.getX() + (this.getWidth() - ITEM_SIZE) / 2, this.getY() + iconYOffset + (this.getHeight() - ITEM_SIZE) / 2);
	}
}
