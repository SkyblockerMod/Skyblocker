package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.utils.EnumUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class EditBarWidget extends ContainerWidget {

	private final EnumCyclingOption<StatusBar.IconPosition> iconOption;
	private final EnumCyclingOption<StatusBar.TextPosition> textOption;

	private final BooleanOption showMaxOption;
	private final BooleanOption showOverflowOption;

	private final ColorOption color1;
	private final ColorOption color2;
	private final ColorOption textColor;

	private final RunnableOption hideOption;

	private final TextWidget nameWidget;

	private final List<? extends ClickableWidget> options;

	private int contentsWidth = 0;

	public EditBarWidget(int x, int y, Screen parent) {
		super(x, y, 100, 99, Text.literal("Edit bar"));

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		nameWidget = new TextWidget(Text.empty(), textRenderer);

		MutableText translatable = Text.translatable("skyblocker.bars.config.icon");
		iconOption = new EnumCyclingOption<>(0, 11, getWidth(), translatable, StatusBar.IconPosition.class);
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + iconOption.getLongestOptionWidth() + 10);

		translatable = Text.translatable("skyblocker.bars.config.text");
		textOption = new EnumCyclingOption<>(0, 22, getWidth(), translatable, StatusBar.TextPosition.class);
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + textOption.getLongestOptionWidth() + 10);

		translatable = Text.translatable("skyblocker.bars.config.showMax");
		showMaxOption = new BooleanOption(0, 33, getWidth(), translatable);
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);

		translatable = Text.translatable("skyblocker.bars.config.showOverflow");
		showOverflowOption = new BooleanOption(0, 44, getWidth(), translatable);
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);

		// COLO(u)RS
		translatable = Text.translatable("skyblocker.bars.config.mainColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
		color1 = new ColorOption(0, 55, getWidth(), translatable, parent);

		translatable = Text.translatable("skyblocker.bars.config.overflowColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
		color2 = new ColorOption(0, 66, getWidth(), translatable, parent);

		translatable = Text.translatable("skyblocker.bars.config.textColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
		textColor = new ColorOption(0, 77, getWidth(), translatable, parent);

		translatable = Text.translatable("skyblocker.bars.config.hide");
		contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
		hideOption = new RunnableOption(0, 88, getWidth(), translatable);

		options = List.of(iconOption, textOption, showMaxOption, showOverflowOption, color1, color2, textColor, hideOption);

		setWidth(contentsWidth);
	}

	@Override
	public List<? extends Element> children() {
		return options;
	}

	public int insideMouseX = 0;
	public int insideMouseY = 0;

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (isHovered()) {
			insideMouseX = mouseX;
			insideMouseY = mouseY;
		} else {
			int i = mouseX - insideMouseX;
			int j = mouseY - insideMouseY;
			if (i * i + j * j > 30 * 30) visible = false;
		}
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(getX(), getY(), 200.f);
		TooltipBackgroundRenderer.render(context, 0, 0, getWidth(), getHeight(), 0, null);
		nameWidget.render(context, mouseX, mouseY, delta);
		for (ClickableWidget option : options) option.render(context, mouseX - getX(), mouseY - getY(), delta);
		matrices.pop();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!visible) return false;
		if (!isHovered()) visible = false;
		return super.mouseClicked(mouseX - getX(), mouseY - getY(), button);
	}

	public void setStatusBar(StatusBar statusBar) {
		iconOption.setCurrent(statusBar.getIconPosition());
		iconOption.setOnChange(statusBar::setIconPosition);
		textOption.setCurrent(statusBar.getTextPosition());
		textOption.setOnChange(statusBar::setTextPosition);

		color1.setCurrent(statusBar.getColors()[0].getRGB());
		color1.setOnChange(color -> statusBar.getColors()[0] = color);

		showMaxOption.active = statusBar.hasMax();
		showMaxOption.setCurrent(statusBar.showMax);
		showOverflowOption.active = statusBar.hasOverflow();
		showOverflowOption.setCurrent(statusBar.showOverflow);
		showMaxOption.setOnChange(showMax -> statusBar.showMax = showMax);
		showOverflowOption.setOnChange(showOverflow -> statusBar.showOverflow = showOverflow);

		color2.active = statusBar.hasOverflow();
		if (color2.active) {
			color2.setCurrent(statusBar.getColors()[1].getRGB());
			color2.setOnChange(color -> statusBar.getColors()[1] = color);
		}

		if (statusBar.getTextColor() != null) {
			textColor.setCurrent(statusBar.getTextColor().getRGB());
		}
		textColor.setOnChange(statusBar::setTextColor);
		hideOption.active = statusBar.anchor != null;
		hideOption.setRunnable(() -> {
			if (statusBar.anchor != null)
				FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
			FancyStatusBars.updatePositions(true);
		});

		MutableText formatted = statusBar.getName().copy().formatted(Formatting.BOLD);
		nameWidget.setMessage(formatted);
		setWidth(Math.max(MinecraftClient.getInstance().textRenderer.getWidth(formatted), contentsWidth));
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (ClickableWidget option : options) option.setWidth(width);
		nameWidget.setWidth(width);

	}

	public class RunnableOption extends ClickableWidget {

		private Runnable runnable;

		public RunnableOption(int x, int y, int width, Text message) {
			super(x, y, width, 11, message);
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? -1: Colors.GRAY, true);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			super.onClick(mouseX, mouseY);
			EditBarWidget.this.visible = false;
			if (runnable != null) runnable.run();
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	public static class EnumCyclingOption<T extends Enum<T>> extends ClickableWidget {

		private T current;
		private final T[] values;
		private Consumer<T> onChange = null;

		public EnumCyclingOption(int x, int y, int width, Text message, Class<T> enumClass) {
			super(x, y, width, 11, message);
			values = enumClass.getEnumConstants();
			current = values[0];
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, -1, true);
			String string = current.toString();
			context.drawText(textRenderer, string, getRight() - textRenderer.getWidth(string) - 1, getY() + 1, -1, true);
		}

		public void setCurrent(T current) {
			this.current = current;
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			current = EnumUtils.cycle(current);
			if (onChange != null) onChange.accept(current);
			super.onClick(mouseX, mouseY);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}

		public void setOnChange(Consumer<T> onChange) {
			this.onChange = onChange;
		}

		int getLongestOptionWidth() {
			int m = 0;
			for (T value : values) {
				int i = MinecraftClient.getInstance().textRenderer.getWidth(value.toString());
				m = Math.max(m, i);
			}
			return m;
		}
	}

	public static class BooleanOption extends ClickableWidget {

		private boolean current = false;
		private BooleanConsumer onChange = null;

		public BooleanOption(int x, int y, int width, Text message) {
			super(x, y, width, 11, message);
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? -1 : Colors.GRAY, true);
			context.drawBorder(getRight() - 10, getY() + 1, 9, 9, active ? -1 : Colors.GRAY);
			if (current && active) context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, -1);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			current = !current;
			if (onChange != null) onChange.accept(current);
			super.onClick(mouseX, mouseY);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}

		public void setCurrent(boolean current) {
			this.current = current;
		}

		public void setOnChange(BooleanConsumer onChange) {
			this.onChange = onChange;
		}
	}

	public static class ColorOption extends ClickableWidget {

		public void setCurrent(int current) {
			this.current = current;
		}

		private int current = 0;
		private Consumer<Color> onChange = null;
		private final Screen parent;

		public ColorOption(int x, int y, int width, Text message, Screen parent) {
			super(x, y, width, 11, message);
			this.parent = parent;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? -1 : Colors.GRAY, true);
			context.drawBorder(getRight() - 10, getY() + 1, 9, 9, active ? -1 : Colors.GRAY);
			context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, active ? current : Colors.GRAY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			super.onClick(mouseX, mouseY);
			MinecraftClient.getInstance().setScreen(new EditBarColorPopup(Text.literal("Edit ").append(getMessage()), parent, this::set));
		}

		private void set(Color color) {
			current = color.getRGB();
			if (onChange != null) onChange.accept(color);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}

		public void setOnChange(Consumer<Color> onChange) {
			this.onChange = onChange;
		}
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return 0;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}
}
