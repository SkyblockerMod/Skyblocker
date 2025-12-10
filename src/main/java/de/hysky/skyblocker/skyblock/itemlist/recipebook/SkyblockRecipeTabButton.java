package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;

/**
 * Based off {@link net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget}
 */
public class SkyblockRecipeTabButton extends TexturedButtonWidget {
	protected final ItemStack icon;
	private boolean selected;

	protected SkyblockRecipeTabButton(ItemStack icon) {
		super(0, 0, 35, 27, RecipeGroupButtonWidget.TEXTURES, _ignored -> {});
		this.icon = icon;
	}

	@Override
	public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.textures != null) {
			int x = this.getX();

			//Offset x
			if (this.selected) {
				x -= 2;
			}

			//Render main texture
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.textures.get(true, this.selected), x, this.getY(), this.width, this.height);

			//Render item icon
			int offset = this.selected ? -2 : 0;
			context.drawItemWithoutEntity(this.icon, this.getX() + 9 + offset, this.getY() + 5);

			if (this.isHovered()) {
				context.setCursor(StandardCursors.POINTING_HAND);
			}
		}
	}

	@Override
	protected void setCursor(DrawContext context) {
		if (!this.selected) {
			super.setCursor(context);
		}
	}

	public void select() {
		this.selected = true;
	}

	public void unselect() {
		this.selected = false;
	}
}
