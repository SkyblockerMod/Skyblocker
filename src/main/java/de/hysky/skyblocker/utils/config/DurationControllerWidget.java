package de.hysky.skyblocker.utils.config;

import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class DurationControllerWidget extends StringControllerElement {

	public DurationControllerWidget(IStringController<?> control, YACLScreen screen, Dimension<Integer> dim) {
		super(control, screen, dim, false);
	}

	@Override
	public void unfocus() {
		if (control.isInputValid(inputField)) super.unfocus();
		else modifyInput(stringBuilder -> stringBuilder.replace(0, stringBuilder.length(), control.getString()));
	}

	@Override
	public boolean modifyInput(Consumer<StringBuilder> consumer) {
		StringBuilder temp = new StringBuilder(inputField);
		consumer.accept(temp);
		inputField = temp.toString();
		return true;
	}

	@Override
	protected Component getValueText() {
		Component valueText = super.getValueText();
		boolean inputValid = control.isInputValid(valueText.getString());
		return valueText.copy().withStyle(inputValid ? ChatFormatting.WHITE : ChatFormatting.RED);
	}
}
