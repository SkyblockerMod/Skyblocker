package de.hysky.skyblocker.skyblock.profileviewer2;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfileResponse;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class ProfileViewerScreen extends AbstractProfileViewerScreen {
	private static final Component TITLE = Component.literal("Skyblocker Profile Viewer");
	private final ApiProfileResponse apiProfileResponse;
	private final GameProfile gameProfile;

	protected ProfileViewerScreen(ApiProfileResponse apiProfileResponse, GameProfile gameProfile) {
		super(TITLE);
		this.apiProfileResponse = apiProfileResponse;
		this.gameProfile = gameProfile;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		graphics.drawCenteredString(this.font, "The calm before the storm.", middleX, middleY, CommonColors.WHITE);
		graphics.drawCenteredString(this.font, this.gameProfile.name() + "'s profile " + this.apiProfileResponse.getSelectedProfile().cuteName + "?", middleX, middleY + this.font.lineHeight, CommonColors.WHITE);
	}
}
