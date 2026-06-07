package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

/**
 * Mainly a pair of a {@link HudWidget} and a {@link PositionRule}. <p>
 * Includes a few more information for {@link LayerBuilder} and config screen
 */
public final class PositionedWidget {
	public final HudWidget widget;
	public PositionRule rule;
	public boolean fromTab = false;
	boolean positioned = false;
	boolean visible = false;

	public PositionedWidget(HudWidget widget, PositionRule rule) {
		this.widget = widget;
		this.rule = rule;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;

		PositionedWidget that = (PositionedWidget) o;
		return widget.equals(that.widget);
	}

	@Override
	public int hashCode() {
		return widget.hashCode();
	}
}
