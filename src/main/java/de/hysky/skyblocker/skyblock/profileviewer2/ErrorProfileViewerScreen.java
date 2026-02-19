package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class ErrorProfileViewerScreen extends AbstractProfileViewerScreen {
	private static final Component TITLE = Component.literal("Skyblocker Profile Viewer - Error");
	private final String reason;

	protected ErrorProfileViewerScreen(String reason) {
		super(TITLE);
		this.reason = reason;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		graphics.drawCenteredString(this.font, "Error ;( " + this.reason, middleX, middleY, CommonColors.WHITE);
	}
}
