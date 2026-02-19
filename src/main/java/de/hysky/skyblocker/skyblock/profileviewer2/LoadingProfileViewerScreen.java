package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class LoadingProfileViewerScreen extends AbstractProfileViewerScreen {
	private static final Component TITLE = Component.literal("Skyblocker Profile Viewer - Loading");
	private final long start = System.currentTimeMillis();

	protected LoadingProfileViewerScreen(String name) {
		super(TITLE);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		long timeLoading = System.currentTimeMillis() - this.start;
		String loadingText = "Loading " + LoadingDotsText.get(timeLoading);

		graphics.drawCenteredString(this.font, loadingText, middleX, middleY, CommonColors.WHITE);
	}
}
