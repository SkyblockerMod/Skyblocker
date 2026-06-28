package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

public class ButtonWidget extends Button {
	private static final Identifier NORMAL = SkyblockerMod.id("profile_viewer2/button");
	private static final Identifier NORMAL_TOGGLED = SkyblockerMod.id("profile_viewer2/button_toggled");
	private static final Identifier HIGHLIGHTED = SkyblockerMod.id("profile_viewer2/button_highlighted");
	private static final Identifier HIGHLIGHTED_TOGGLED = SkyblockerMod.id("profile_viewer2/button_toggled_highlighted");
	private static final int SIZE = 20;
	private final FlexibleItemStack icon;

	public ButtonWidget(FlexibleItemStack icon, OnPress onPress) {
		super(0, 0, SIZE, SIZE, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.icon = icon;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (this.isFocused() && this.isHovered()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HIGHLIGHTED_TOGGLED, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		} else if (this.isFocused()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NORMAL_TOGGLED, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		} else if (this.isHovered()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HIGHLIGHTED, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		} else {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, NORMAL, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}

		graphics.fakeItem(this.icon.getStackOrThrow(), this.getX() + (this.getWidth() - GuiRenderer.DEFAULT_ITEM_SIZE) / 2, this.getY() + (this.getHeight() - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);
	}
}
