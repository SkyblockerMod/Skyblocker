package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.item.ItemStack;

/**
 * Based off {@link net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget}
 */
public class SkyblockRecipeTabButton extends ToggleButtonWidget {
	protected final ItemStack icon;

	protected SkyblockRecipeTabButton(ItemStack icon) {
		super(0, 0, 35, 27, false);

		this.icon = icon;
		this.setTextures(RecipeGroupButtonWidget.TEXTURES);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.textures != null) {
			int x = this.getX();

			//Offset x
			if (this.toggled) x -= 2;

			//Render main texture
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.textures.get(true, this.toggled), x, this.getY(), this.width, this.height);

			//Render item icon
			int offset = this.toggled ? -2 : 0;
			context.drawItemWithoutEntity(this.icon, this.getX() + 9 + offset, this.getY() + 5);
		}
	}
}
