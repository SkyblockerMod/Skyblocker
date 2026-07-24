package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class LoadingProfileViewerScreen extends AbstractProfileViewerScreen {
	private final long start = System.currentTimeMillis();

	protected LoadingProfileViewerScreen(String name) {
		super(Component.literal("Skyblocker Profile Viewer - Loading"));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		int centreX = this.getBackgroundX() + (BACKGROUND_WIDTH / 2);
		int centreY = this.getBackgroundY() + (BACKGROUND_HEIGHT / 2);
		long timeLoading = System.currentTimeMillis() - this.start;

		graphics.centeredText(this.font, "Loading", centreX, centreY - this.font.lineHeight, CommonColors.WHITE);
		graphics.centeredText(this.font, LoadingDotsText.get(timeLoading), centreX, centreY + this.font.lineHeight, CommonColors.WHITE);
	}
}
