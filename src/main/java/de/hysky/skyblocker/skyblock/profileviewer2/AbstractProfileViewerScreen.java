package de.hysky.skyblocker.skyblock.profileviewer2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract sealed class AbstractProfileViewerScreen extends Screen permits ErrorProfileViewerScreen, LoadingProfileViewerScreen, ProfileViewerScreen {

	protected AbstractProfileViewerScreen(Component title) {
		super(title);
	}

	@Override
	public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float a);
}
