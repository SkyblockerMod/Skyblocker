package de.hysky.skyblocker.skyblock.item.custom.screen;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

class IdentifierTextField extends EditBox {
	private final Consumer<@Nullable Identifier> callback;
	private String lastValid = "";
	private boolean valid = false;

	IdentifierTextField(int width, int height, Consumer<@Nullable Identifier> callback) {
		super(Minecraft.getInstance().font, width, height, Component.empty());
		super.setResponder(this::onChanged);
		this.callback = callback;
		addFormatter((string, _firstCharacterIndex) -> FormattedCharSequence.forward(string, valid ? Style.EMPTY : Style.EMPTY.applyFormat(ChatFormatting.RED)));
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
		if (!focused && !lastValid.equals(getValue())) {
			setValue(lastValid);
		}
	}
}
