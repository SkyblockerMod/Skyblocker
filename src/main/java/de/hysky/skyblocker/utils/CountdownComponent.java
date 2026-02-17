package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;

/**
 * Custom {@link Component} that counts down for a given duration of a {@link TimeUnit}.
 *
 * <p>Note that this component should not be nested in a {@code MutableComponent} otherwise the
 * countdown will not update.
 */
public final class CountdownComponent implements Component {
	private final long start;
	private final long duration;
	private final TimeUnit timeUnit;
	private final Component prefix;
	private final Style style;

	/**
	 * Creates a new {@code CountdownComponent}.
	 *
	 * @param duration the duration of the countdown
	 * @param timeUnit the {@link TimeUnit} of the {@code duration}
	 * @param style    the style of the component
	 */
	public CountdownComponent(long duration, TimeUnit timeUnit, Component prefix, Style style) {
		this.start = System.currentTimeMillis();
		this.duration = ++duration;
		this.timeUnit = timeUnit;
		this.prefix = prefix;
		this.style = style;
	}

	@Override
	public Style getStyle() {
		return this.style;
	}

	@Override
	public ComponentContents getContents() {
		long end = this.start + this.timeUnit.toMillis(this.duration);
		long timeLeft = this.timeUnit.convert(end - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		return timeLeft > 0 ? PlainTextContents.create(" (" + timeLeft + ")") : PlainTextContents.EMPTY;
	}

	@Override
	public List<Component> getSiblings() {
		return new ArrayList<>();
	}

	@Override
	public FormattedCharSequence getVisualOrderText() {
		// Always return a new instance since the content updates
		return Language.getInstance().getVisualOrder(FormattedText.composite(this.prefix, this));
	}
}
