package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

/**
 * Based off {@link net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton}
 */
public class SkyblockRecipeTabButton extends StateSwitchingButton {
	protected final ItemStack icon;

	protected SkyblockRecipeTabButton(ItemStack icon) {
		super(0, 0, 35, 27, false);

		this.icon = icon;
		this.initTextureValues(RecipeBookTabButton.SPRITES);
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (this.sprites != null) {
			int x = this.getX();

			//Offset x
			if (this.isStateTriggered) x -= 2;

			//Render main texture
			context.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(true, this.isStateTriggered), x, this.getY(), this.width, this.height);

			//Render item icon
			int offset = this.isStateTriggered ? -2 : 0;
			context.renderFakeItem(this.icon, this.getX() + 9 + offset, this.getY() + 5);

			if (this.isHovered()) {
				context.requestCursor(CursorTypes.POINTING_HAND);
			}
		}
	}
}
