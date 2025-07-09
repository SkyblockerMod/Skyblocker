package de.hysky.skyblocker.utils.render.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class NonClosingPopupScreen extends PopupScreen {
	public NonClosingPopupScreen(Screen backgroundScreen, int width, @Nullable Identifier image, Text title, Text message, List<PopupScreen.Button> buttons, @Nullable Runnable onClosed) {
		super(backgroundScreen, width, image, title, message, buttons, onClosed);
	}

	@Override
	public void close() {
		if (super.onClosed != null) {
			super.onClosed.run();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Builder extends PopupScreen.Builder {
		public Builder(Screen backgroundScreen, Text title) {
			super(backgroundScreen, title);
		}

		@Override
		public Builder width(int width) {
			super.width(width);
			return this;
		}

		@Override
		public Builder image(Identifier image) {
			super.image(image);
			return this;
		}

		@Override
		public Builder message(Text message) {
			super.message(message);
			return this;
		}

		@Override
		public Builder button(Text message, Consumer<PopupScreen> action) {
			super.button(message, action);
			return this;
		}

		@Override
		public Builder onClosed(Runnable onClosed) {
			super.onClosed(onClosed);
			return this;
		}

		@Override
		public NonClosingPopupScreen build() {
			if (super.buttons.isEmpty()) {
				throw new IllegalStateException("Popup must have at least one button");
			} else {
				return new NonClosingPopupScreen(super.backgroundScreen, super.width, super.image, super.title, super.message, List.copyOf(super.buttons), super.onClosed);
			}
		}
	}
}
