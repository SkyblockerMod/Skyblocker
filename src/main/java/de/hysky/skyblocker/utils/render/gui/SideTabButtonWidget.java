package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SideTabButtonWidget extends StateSwitchingButton {
	private static final WidgetSprites TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/tab"), Identifier.withDefaultNamespace("recipe_book/tab_selected"));
	protected ItemStack icon;

	public void setIcon(ItemStack icon) {
		this.icon = icon.copy();
	}

	public SideTabButtonWidget(int x, int y, boolean toggled, ItemStack icon) {
		super(x, y, 35, 27, toggled);
		this.icon = icon.copy();
		initTextureValues(TEXTURES);
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (sprites == null) return;
		Identifier identifier = sprites.get(true, this.isStateTriggered);
		int x = getX();
		if (isStateTriggered) x -= 2;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x, this.getY(), this.width, this.height);
		context.renderItem(icon, x + 9, getY() + 5);
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (!isStateTriggered()) this.setStateTriggered(true);
	}
}
