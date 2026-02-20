package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract sealed class ProfileViewerWidget extends AbstractWidget permits RulerWidget {

	public ProfileViewerWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}
}
