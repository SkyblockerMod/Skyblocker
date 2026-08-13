package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

/**
 * Based off {@link net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton}
 */
public class SkyblockRecipeTabButton extends ImageButton {
	protected final ItemStack icon;
	private boolean selected;

	protected SkyblockRecipeTabButton(ItemStack icon) {
		super(0, 0, 35, 27, RecipeBookTabButton.SPRITES, _ -> {});
		this.icon = icon;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.sprites != null) {
			int x = this.getX();

			//Offset x
			if (this.selected) {
				x -= 2;
			}

			//Render main texture
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(true, this.selected), x, this.getY(), this.width, this.height);

			//Render item icon
			int offset = this.selected ? -2 : 0;
			graphics.fakeItem(this.icon, this.getX() + 9 + offset, this.getY() + 5);
		}
	}

	@Override
	protected void handleCursor(GuiGraphicsExtractor context) {
		if (!this.selected) {
			super.handleCursor(context);
		}
	}

	public void select() {
		this.selected = true;
	}

	public void unselect() {
		this.selected = false;
	}
}
