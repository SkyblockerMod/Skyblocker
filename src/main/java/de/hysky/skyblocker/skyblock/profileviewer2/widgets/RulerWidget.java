package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

/**
 * This widget's purpose is for debugging and positing widgets.
 */
// TODO maybe add some cool functionality to draw vertical/horizontal lines that move with scroll wheel when holding down option
// TODO lengthen ruler width
public final class RulerWidget extends ProfileViewerWidget {

	public RulerWidget() {
		super(0, 0, 0, 0, Component.empty());
		this.active = false;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + 1, CommonColors.RED);
		graphics.fill(this.getX(), this.getY() + 1, this.getX() + 1, this.getY() + 200, CommonColors.HIGH_CONTRAST_DIAMOND);
		graphics.fill(this.getX() + 1, this.getY(), this.getX() + 200, this.getY() + 1, CommonColors.HIGH_CONTRAST_DIAMOND);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}
}
