package de.hysky.skyblocker.skyblock.fancybars;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.render.GuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Unit;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EditBarWidget extends AbstractContainerWidget {
	private static final int BASE_WIDTH = 100;
	private static final int DEFAULT_OPTION_HEIGHT = 11;

	private final StringWidget nameWidget;

	private final List<? extends AbstractOption<?>> options;

	private final int contentsWidth;

	public EditBarWidget(int x, int y, Screen parent) {
		super(x, y, BASE_WIDTH, 110, Component.literal("Edit bar"), AbstractScrollArea.defaultSettings(4));

		Font textRenderer = Minecraft.getInstance().font;

		nameWidget = new StringWidget(Component.empty(), textRenderer);
		LinearLayout layout = LinearLayout.vertical();

		layout.addChild(new EnumCyclingOption<>(Component.translatable("skyblocker.bars.config.icon"), StatusBar.IconPosition.class, StatusBar::getIconPosition, StatusBar::setIconPosition));
		layout.addChild(new EnumCyclingOption<>(Component.translatable("skyblocker.bars.config.text"), StatusBar.TextPosition.class, StatusBar::getTextPosition, StatusBar::setTextPosition));
		layout.addChild(new BooleanOption(Component.translatable("skyblocker.bars.config.showMax"), bar -> bar.hasMax() ? bar.showMax : null, (bar, showMax) -> bar.showMax = showMax));
		layout.addChild(new BooleanOption(Component.translatable("skyblocker.bars.config.showOverflow"), bar -> bar.hasOverflow() ? bar.showOverflow : null, (bar, showOverflow) -> bar.showOverflow = showOverflow));
		layout.addChild(new EnumCyclingOption<>(Component.translatable("skyblocker.bars.config.direction"), StatusBar.Direction.class, StatusBar::getDirection, StatusBar::setDirection));

		// COLO(u)RS
		layout.addChild(new ColorOption(Component.translatable("skyblocker.bars.config.mainColor"), parent, bar -> bar.getColors()[0], (bar, color) -> bar.getColors()[0] = color));
		layout.addChild(new ColorOption(Component.translatable("skyblocker.bars.config.overflowColor"), parent, bar -> bar.hasOverflow() ? bar.getColors()[1] : null, (bar, color) -> bar.getColors()[1] = color));
		layout.addChild(new ColorOption(Component.translatable("skyblocker.bars.config.textColor"), parent, StatusBar::getTextColor, StatusBar::setTextColor));

		layout.addChild(new RunnableOption(Component.translatable("skyblocker.bars.config.hide"), bar -> bar.enabled, bar -> {
			if (bar.anchor != null)
				FancyStatusBars.barPositioner.removeBar(bar.anchor, bar.gridY, bar);
			bar.enabled = false;
			FancyStatusBars.updatePositions(true);
		}));

		ImmutableList.Builder<AbstractOption<?>> builder = ImmutableList.builder();
		layout.visitWidgets(w -> builder.add((AbstractOption<?>) w));
		options = builder.build();
		contentsWidth = options.stream().mapToInt(AbstractOption::expectedWidth).max().orElse(BASE_WIDTH);
		layout.visitWidgets(w -> w.setWidth(contentsWidth));
		layout.arrangeElements();
		layout.setPosition(0, 11);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return options;
	}

	public int insideMouseX = 0;
	public int insideMouseY = 0;

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		if (isHovered()) {
			insideMouseX = mouseX;
			insideMouseY = mouseY;
		} else {
			int i = mouseX - insideMouseX;
			int j = mouseY - insideMouseY;
			if (i * i + j * j > 30 * 30) visible = false;
		}
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		TooltipRenderUtil.extractTooltipBackground(graphics, 0, 0, getWidth(), getHeight(), null);
		nameWidget.extractRenderState(graphics, mouseX, mouseY, a);
		for (AbstractWidget option : options) option.extractRenderState(graphics, mouseX - getX(), mouseY - getY(), a);
		matrices.popMatrix();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;
		if (!isHovered()) visible = false;
		return super.mouseClicked(new MouseButtonEvent(click.x() - getX(), click.y() - getY(), click.buttonInfo()), doubled);
	}

	public void setStatusBar(StatusBar statusBar) {
		options.forEach(opt -> opt.updateFromBar(statusBar));
		MutableComponent formatted = statusBar.getName().copy().withStyle(ChatFormatting.BOLD);
		nameWidget.setMessage(formatted);
		setWidth(Math.max(Minecraft.getInstance().font.width(formatted), contentsWidth));
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (AbstractWidget option : options) option.setWidth(width);
		nameWidget.setWidth(width);

	}

	public class RunnableOption extends AbstractOption<Unit> {

		public RunnableOption(Component message, Predicate<StatusBar> isEnabled, Consumer<StatusBar> action) {
			super(message, bar -> isEnabled.test(bar) ? Unit.INSTANCE : null, (s, _) -> action.accept(s));
		}

		@Override
		protected int extractValue(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			return 0;
		}

		@Override
		protected int expectedValueWidth() {
			return 0;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			super.onClick(click, doubled);
			EditBarWidget.this.visible = false;
			setAndUpdate(Unit.INSTANCE);
		}
	}

	private abstract static class AbstractOption<T> extends AbstractWidget {

		protected @Nullable T current;
		protected final Function<StatusBar, @Nullable T> getter;
		protected final BiConsumer<StatusBar, T> setter;
		protected @Nullable StatusBar activeBar;

		private AbstractOption(int height, Component message, Function<StatusBar, @Nullable T> getter, BiConsumer<StatusBar, T> setter) {
			super(0, 0, BASE_WIDTH, height, message);
			this.getter = getter;
			this.setter = setter;
		}

		private AbstractOption(Component message, Function<StatusBar, @Nullable T> getter, BiConsumer<StatusBar, T> setter) {
			this(DEFAULT_OPTION_HEIGHT, message, getter, setter);
		}

		public void updateFromBar(StatusBar statusBar) {
			T apply = getter.apply(statusBar);
			if (apply != null) {
				current = apply;
				active = true;
			} else {
				current = null;
				active = false;
			}
			this.activeBar = statusBar;
		}

		protected void setAndUpdate(T value) {
			this.current = value;
			if (activeBar != null) setter.accept(activeBar, value);
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			if (isMouseOver(mouseX, mouseY)) {
				graphics.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			Font font = Minecraft.getInstance().font;
			ActiveTextCollector textRenderer = graphics.textRenderer();
			int valueWidth = extractValue(graphics, mouseX, mouseY, a);
			Component message = getMessage().copy().withColor(active ? CommonColors.WHITE : CommonColors.GRAY);
			if (font.width(getMessage()) > getWidth() - valueWidth - 2) {
				textRenderer.acceptScrollingWithDefaultCenter(message, getX() + 1, getRight() - valueWidth - 1, getY(), getBottom());
			} else {
				textRenderer.accept(getX() + 1, getY() + 1, message);
			}
		}

		/**
		 * @return the width taken by the value
		 */
		protected abstract int extractValue(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);

		protected abstract int expectedValueWidth();

		public final int expectedWidth() {
			return expectedValueWidth() + Minecraft.getInstance().font.width(getMessage()) + 2;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {

		}
	}

	public static class EnumCyclingOption<T extends Enum<T>> extends AbstractOption<T> {
		private final T[] values;

		public EnumCyclingOption(Component message, Class<T> enumClass, Function<StatusBar, @Nullable T> getter, BiConsumer<StatusBar, T> setter) {
			super(message, getter, setter);
			values = enumClass.getEnumConstants();
			current = values[0];
		}

		@Override
		protected int extractValue(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			Font textRenderer = Minecraft.getInstance().font;
			String string = current != null ? current.toString() : "???";
			int valueWidth = textRenderer.width(string) + 1;
			graphics.text(textRenderer, string, getRight() - valueWidth, getY() + 1, CommonColors.WHITE, true);
			return valueWidth;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			setAndUpdate(current != null ? EnumUtils.cycle(current) : values[0]);
			super.onClick(click, doubled);
		}

		@Override
		protected int expectedValueWidth() {
			int m = 0;
			for (T value : values) {
				int i = Minecraft.getInstance().font.width(value.toString());
				m = Math.max(m, i);
			}
			return m;
		}
	}

	public static class BooleanOption extends AbstractOption<Boolean> {

		public BooleanOption(Component message, Function<StatusBar, @Nullable Boolean> getter, BiConsumer<StatusBar, Boolean> setter) {
			super(message, getter, setter);
		}

		@Override
		protected int extractValue(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			GuiHelper.border(graphics, getRight() - 10, getY() + 1, 9, 9, active ? -1 : CommonColors.GRAY);
			if (active && Boolean.TRUE.equals(current)) graphics.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, CommonColors.WHITE);
			return 10;
		}

		@Override
		protected int expectedValueWidth() {
			return 10;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			setAndUpdate(current == null || !current);
			super.onClick(click, doubled);
		}

		public void setCurrent(boolean current) {
			this.current = current;
		}
	}

	public static class ColorOption extends AbstractOption<Color> {
		private final Screen parent;

		public ColorOption(Component message, Screen parent, Function<StatusBar, @Nullable Color> getter, BiConsumer<StatusBar, Color> setter) {
			super(message, getter, setter);
			this.parent = parent;
		}

		@Override
		protected int extractValue(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			GuiHelper.border(graphics, getRight() - 10, getY() + 1, 9, 9, active ? -1 : CommonColors.GRAY);
			graphics.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, active && current != null ? current.getRGB() : CommonColors.GRAY);
			return 10;
		}

		@Override
		protected int expectedValueWidth() {
			return 10;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			super.onClick(click, doubled);
			Minecraft.getInstance().gui.setScreen(new EditBarColorPopup(Component.literal("Edit ").append(getMessage()), parent, this::setAndUpdate, current != null ? current.getRGB() : -1));
		}
	}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	protected double scrollRate() {
		return 0;
	}
}
