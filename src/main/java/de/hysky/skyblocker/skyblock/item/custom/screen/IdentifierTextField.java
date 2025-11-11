package de.hysky.skyblocker.skyblock.item.custom.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

class IdentifierTextField extends TextFieldWidget {

	private final Consumer<@Nullable Identifier> callback;
	private @NotNull String lastValid = "";
	private boolean valid = false;

	IdentifierTextField(int width, int height, Consumer<@Nullable Identifier> callback) {
		super(MinecraftClient.getInstance().textRenderer, width, height, Text.empty());
		super.setChangedListener(this::onChanged);
		//setRenderTextProvider((s, integer) -> OrderedText.styledForwardsVisitedString(s, valid ? Style.EMPTY : Style.EMPTY.withFormatting(Formatting.RED)));
		this.callback = callback;
	}

	private void onChanged(String s) {
		Identifier identifier = Identifier.tryParse(s);
		valid = true;
		if (s.isBlank()) {
			callback.accept(null);
			lastValid = "";
		} else if (identifier != null) {
			callback.accept(identifier);
			lastValid = s;
		} else valid = false;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused && !lastValid.equals(getText())) {
			setText(lastValid);
		}
	}
}
