package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public sealed class BasicInfoBoxWidget extends ProfileViewerWidget permits SkillsInfoBoxWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");

	public BasicInfoBoxWidget(int width, int height) {
		super(0, 0, width, height, Component.empty());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}
}
