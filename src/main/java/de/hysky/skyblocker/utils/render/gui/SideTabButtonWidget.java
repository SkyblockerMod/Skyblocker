package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class SideTabButtonWidget extends TexturedButtonWidget {
	protected ItemStack icon;
	protected boolean selected = false;

	public void setIcon(ItemStack icon) {
		this.icon = icon.copy();
	}

	public SideTabButtonWidget(int x, int y, boolean toggled, ItemStack icon) {
		super(x, y, 35, 27, RecipeGroupButtonWidget.TEXTURES, _ignored -> {});
		this.icon = icon.copy();
	}

	@Override
	public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
		if (textures == null) return;
		Identifier identifier = textures.get(true, this.selected);
		int x = getX();
		if (this.selected) x -= 2;
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, x, this.getY(), this.width, this.height);
		context.drawItem(icon, x + 9, getY() + 5);
	}

	@Override
	public void onClick(Click click, boolean doubled) {
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
