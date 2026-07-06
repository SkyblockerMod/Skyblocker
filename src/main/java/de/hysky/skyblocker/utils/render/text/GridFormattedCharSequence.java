package de.hysky.skyblocker.utils.render.text;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;

public record GridFormattedCharSequence(String group, List<FormattedCharSequence> columns) implements FormattedCharSequence {
	@Override
	public boolean accept(FormattedCharSink output) {
		return FormattedCharSequence.composite(columns).accept(output);
	}
}
