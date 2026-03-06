package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class ErrorProfileViewerScreen extends AbstractProfileViewerScreen {
	private final String reason;

	protected ErrorProfileViewerScreen(String reason) {
		super(Component.literal("Skyblocker Profile Viewer - Error"));
		this.reason = reason;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);

		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		graphics.drawCenteredString(this.font, "Encountered an error.", middleX, middleY, CommonColors.WHITE);
		graphics.drawCenteredString(this.font, this.reason, middleX, middleY + 9, CommonColors.WHITE);
	}
}
