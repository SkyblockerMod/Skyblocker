package de.hysky.skyblocker.utils.render.text;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;

public record GridFormattedCharSequence(String group, List<FormattedCharSequence> columns) implements FormattedCharSequence {
	/**
	 * Logically, this should never be called. As rendering is done by {@link GridTooltipComponent}.<p>
	 * But this is present as a fail-safe.
	 */
	@Override
	public boolean accept(FormattedCharSink output) {
		return FormattedCharSequence.composite(columns).accept(output);
	}
}
