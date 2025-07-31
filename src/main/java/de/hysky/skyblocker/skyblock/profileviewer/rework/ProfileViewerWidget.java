package de.hysky.skyblocker.skyblock.profileviewer.rework;

import net.minecraft.client.gui.DrawContext;

public interface ProfileViewerWidget {
	void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks);

	default void click(int x, int y, int mouseX, int mouseY) {}

	static Instance widget(int x, int y, ProfileViewerWidget widget) {
		return new Instance(widget, x, y);
	}

	record Instance(ProfileViewerWidget widget, int xRelative, int yRelative) {
		void render(DrawContext drawContext, int rootX, int rootY, int mouseX, int mouseY, float deltaTicks) {
			widget.render(drawContext, rootX + xRelative, rootY + yRelative, mouseX, mouseY, deltaTicks);
		}
	}
}
