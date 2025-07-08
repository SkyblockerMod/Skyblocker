package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NonClosingPopupScreen extends PopupScreen {
	public NonClosingPopupScreen(Screen backgroundScreen, int width, @Nullable Identifier image, Text title, Text message, List<PopupScreen.Button> buttons, @Nullable Runnable onClosed) {
		super(backgroundScreen, width, image, title, message, buttons, onClosed);
	}

	@Override
	public void close() {
		if (this.onClosed != null) {
			this.onClosed.run();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Screen backgroundScreen;
		private final Text title;
		private Text message = ScreenTexts.EMPTY;
		private int width = 250;
		@Nullable
		private Identifier image;
		private final List<PopupScreen.Button> buttons = new ArrayList<>();
		@Nullable
		private Runnable onClosed = null;

		public Builder(Screen backgroundScreen, Text title) {
			this.backgroundScreen = backgroundScreen;
			this.title = title;
		}

		public NonClosingPopupScreen.Builder width(int width) {
			this.width = width;
			return this;
		}

		public NonClosingPopupScreen.Builder image(Identifier image) {
			this.image = image;
			return this;
		}

		public NonClosingPopupScreen.Builder message(Text message) {
			this.message = message;
			return this;
		}

		public NonClosingPopupScreen.Builder button(Text message, Consumer<PopupScreen> action) {
			this.buttons.add(new PopupScreen.Button(message, action));
			return this;
		}

		public NonClosingPopupScreen.Builder onClosed(Runnable onClosed) {
			this.onClosed = onClosed;
			return this;
		}

		public NonClosingPopupScreen build() {
			if (this.buttons.isEmpty()) {
				throw new IllegalStateException("Popup must have at least one button");
			} else {
				return new NonClosingPopupScreen(this.backgroundScreen, this.width, this.image, this.title, this.message, List.copyOf(this.buttons), this.onClosed);
			}
		}
	}
}
