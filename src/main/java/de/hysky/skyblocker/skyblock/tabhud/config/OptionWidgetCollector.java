package de.hysky.skyblocker.skyblock.tabhud.config;

import java.text.NumberFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;

public record OptionWidgetCollector(List<AbstractWidget> collectorList, Runnable onOptionChange, Location editingFor) {

	public <T extends AbstractWidget> T addWidget(T widget) {
		collectorList.add(widget);
		return widget;
	}

	public void buildDeferred() {
		collectorList.replaceAll(widget -> widget instanceof DeferredWidget deferred ? deferred.get() : widget);
	}

	public void yesNoButton(Component label, Consumer<Boolean> callback, boolean initialValue, @Nullable Component tooltip) {
		Consumer<Boolean> consumer = callback.andThen(_ -> onOptionChange.run());
		CycleButton.Builder<Boolean> builder = CycleButton.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.GUI_NO, initialValue);
		if (tooltip != null) builder.withTooltip(_ -> Tooltip.create(tooltip));
		addWidget(builder.create(label, (_, value) -> consumer.accept(value)));
	}

	public void yesNoButton(Component label, Consumer<Boolean> callback, boolean initialValue) {
		yesNoButton(label, callback, initialValue, null);
	}

	/**
	 * Creates an enum button, returns a builder with optional parameters. The builder will be automatically built.
	 */
	public <E extends Enum<E>> EnumButtonBuilder<E> enumButton(Class<E> enumClass, Component label, Consumer<E> callback, E initialValue) {
		EnumButtonBuilder<E> builder = new EnumButtonBuilder<>(enumClass, label, callback.andThen(_ -> onOptionChange.run()), initialValue);
		addWidget(new DeferredWidget(builder::build));
		return builder;
	}

	public void slider(Component label, DoubleConsumer callback, double initialValue, double step, double min, double max, NumberFormat format) {
		DoubleConsumer consumer = callback.andThen(_ -> onOptionChange.run());
		addWidget(RangedSliderWidget.builder()
				.minMax(min, max)
				.step(step)
				.optionFormatter(label, format)
				.defaultValue(initialValue)
				.callback(consumer)
				.build()
		);
	}

	public void slider(Component label, DoubleConsumer callback, double initialValue, double step, double min, double max) {
		slider(label, callback, initialValue, step, min, max, Formatters.FLOAT_NUMBERS);
	}

	public static class EnumButtonBuilder<E extends Enum<E>> {
		private final Class<E> enumClass;
		private final Component label;
		private final Consumer<E> callback;
		private final E initialValue;
		private Function<E, Component> display = e -> Component.literal(e.toString());
		private @Nullable Component tooltip;

		private EnumButtonBuilder(Class<E> enumClass, Component label, Consumer<E> callback, E initialValue) {
			this.enumClass = enumClass;
			this.label = label;
			this.callback = callback;
			this.initialValue = initialValue;
		}

		public EnumButtonBuilder<E> display(Function<E, Component> display) {
			this.display = display;
			return this;
		}

		public EnumButtonBuilder<E> tooltip(Component tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		private CycleButton<E> build() {
			CycleButton.Builder<E> builder = CycleButton.builder(display, initialValue);
			if (tooltip != null) builder.withTooltip(_ -> Tooltip.create(tooltip));
			return builder
					.withValues(enumClass.getEnumConstants())
					.create(label, (_, value) -> callback.accept(value));
		}
	}

	private static class DeferredWidget extends AbstractWidget implements Supplier<AbstractWidget> {
		private final Supplier<AbstractWidget> supplier;

		private DeferredWidget(Supplier<AbstractWidget> supplier) {
			super(0, 0, 0, 0, Component.empty());
			this.supplier = supplier;
		}

		@Override
		public AbstractWidget get() {
			return supplier.get();
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			throw new UnsupportedOperationException("Be sure to call OptionWidgetCollector#buildDeferred!");
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {

		}
	}
}
