package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class BasicInfoBoxWidget extends ProfileViewerWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");

	public BasicInfoBoxWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}
}
