package de.hysky.skyblocker.utils;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class ChildScreen extends Screen {
	protected final @Nullable Screen parent;

	protected ChildScreen(Component title, @Nullable Screen parent) {
		super(title);
		this.parent = parent;
	}

	protected void reopenParent() {
		this.minecraft.setScreen(parent);
	}

	@Override
	public void onClose() {
		reopenParent();
	}
}
