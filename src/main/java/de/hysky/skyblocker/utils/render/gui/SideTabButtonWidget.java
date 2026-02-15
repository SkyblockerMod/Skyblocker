package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SideTabButtonWidget extends ImageButton {
	protected ItemStack icon;
	protected boolean selected = false;

	public void setIcon(ItemStack icon) {
		this.icon = icon.copy();
	}

	public SideTabButtonWidget(int x, int y, boolean toggled, WidgetSprites sprites, ItemStack icon) {
		super(x, y, 35, 27, sprites, _ignored -> {});
		this.icon = icon.copy();
	}

	public SideTabButtonWidget(int x, int y, boolean toggled, ItemStack icon) {
		this(x, y, toggled, RecipeBookTabButton.SPRITES, icon);
	}

	@Override
	public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
		Identifier identifier = sprites.get(true, this.selected);
		int x = getX();
		if (this.selected) x -= 2;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x, this.getY(), this.width, this.height);
		context.renderItem(icon, x + 9, getY() + 5);
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (!this.selected) this.selected = true;
	}

	public void select() {
		this.selected = true;
	}

	public void unselect() {
		this.selected = false;
	}
}
