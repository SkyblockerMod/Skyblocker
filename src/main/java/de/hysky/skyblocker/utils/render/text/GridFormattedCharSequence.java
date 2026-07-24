package de.hysky.skyblocker.utils.render.text;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;

public record GridFormattedCharSequence(GridComponent.Contents gridContents, FormattedCharSequence composite) implements FormattedCharSequence {

	public GridFormattedCharSequence(GridComponent.Contents gridContents) {
		List<FormattedCharSequence> columns = gridContents.components().stream()
				.map(Component::getVisualOrderText)
				.toList();
		FormattedCharSequence composite = FormattedCharSequence.composite(columns);

		this(gridContents, composite);
	}

	/**
	 * Logically, this should never be called. As rendering is done by {@link GridTooltipComponent}.<p>
	 * But this is present as a fail-safe.
	 */
	@Override
	public boolean accept(FormattedCharSink output) {
		return this.composite.accept(output);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof GridFormattedCharSequence other && this.gridContents.equals(other.gridContents);
	}

	@Override
	public int hashCode() {
		return this.gridContents.hashCode();
	}
}
