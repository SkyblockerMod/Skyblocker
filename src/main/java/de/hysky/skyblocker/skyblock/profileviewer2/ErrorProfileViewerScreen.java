package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class ErrorProfileViewerScreen extends AbstractProfileViewerScreen {
	private final String reason;

	protected ErrorProfileViewerScreen(String reason) {
		super(Component.literal("Skyblocker Profile Viewer - Error"));
		this.reason = reason;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		graphics.centeredText(this.font, "Encountered an error.", middleX, middleY, CommonColors.WHITE);
		graphics.centeredText(this.font, this.reason, middleX, middleY + 9, CommonColors.WHITE);
	}
}
