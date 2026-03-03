package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.render.HudHelper;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2fStack;

public class EditBarWidget extends AbstractContainerWidget {

	private final EnumCyclingOption<StatusBar.IconPosition> iconOption;
	private final EnumCyclingOption<StatusBar.TextPosition> textOption;

	private final BooleanOption showMaxOption;
	private final BooleanOption showOverflowOption;

	private final ColorOption color1;
	private final ColorOption color2;
	private final ColorOption textColor;

	private final RunnableOption hideOption;

	private final StringWidget nameWidget;

	private final List<? extends AbstractWidget> options;

	private int contentsWidth = 0;

	public EditBarWidget(int x, int y, Screen parent) {
		super(x, y, 100, 99, Component.literal("Edit bar"));

		Font textRenderer = Minecraft.getInstance().font;

		nameWidget = new StringWidget(Component.empty(), textRenderer);

		MutableComponent translatable = Component.translatable("skyblocker.bars.config.icon");
		iconOption = new EnumCyclingOption<>(0, 11, getWidth(), translatable, StatusBar.IconPosition.class);
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + iconOption.getLongestOptionWidth() + 10);

		translatable = Component.translatable("skyblocker.bars.config.text");
		textOption = new EnumCyclingOption<>(0, 22, getWidth(), translatable, StatusBar.TextPosition.class);
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + textOption.getLongestOptionWidth() + 10);

		translatable = Component.translatable("skyblocker.bars.config.showMax");
		showMaxOption = new BooleanOption(0, 33, getWidth(), translatable);
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);

		translatable = Component.translatable("skyblocker.bars.config.showOverflow");
		showOverflowOption = new BooleanOption(0, 44, getWidth(), translatable);
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);

		// COLO(u)RS
		translatable = Component.translatable("skyblocker.bars.config.mainColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);
		color1 = new ColorOption(0, 55, getWidth(), translatable, parent);

		translatable = Component.translatable("skyblocker.bars.config.overflowColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);
		color2 = new ColorOption(0, 66, getWidth(), translatable, parent);

		translatable = Component.translatable("skyblocker.bars.config.textColor");
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);
		textColor = new ColorOption(0, 77, getWidth(), translatable, parent);

		translatable = Component.translatable("skyblocker.bars.config.hide");
		contentsWidth = Math.max(contentsWidth, textRenderer.width(translatable) + 9 + 10);
		hideOption = new RunnableOption(0, 88, getWidth(), translatable);

		options = List.of(iconOption, textOption, showMaxOption, showOverflowOption, color1, color2, textColor, hideOption);

		setWidth(contentsWidth);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return options;
	}

	public int insideMouseX = 0;
	public int insideMouseY = 0;

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (isHovered()) {
			insideMouseX = mouseX;
			insideMouseY = mouseY;
		} else {
			int i = mouseX - insideMouseX;
			int j = mouseY - insideMouseY;
			if (i * i + j * j > 30 * 30) visible = false;
		}
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		TooltipRenderUtil.renderTooltipBackground(context, 0, 0, getWidth(), getHeight(), null);
		nameWidget.render(context, mouseX, mouseY, delta);
		for (AbstractWidget option : options) option.render(context, mouseX - getX(), mouseY - getY(), delta);
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
		hideOption.active = statusBar.enabled;
		hideOption.setRunnable(() -> {
			if (statusBar.anchor != null)
				FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
			statusBar.enabled = false;
			FancyStatusBars.updatePositions(true);
		});

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

	public class RunnableOption extends AbstractWidget {

		private Runnable runnable;

		public RunnableOption(int x, int y, int width, Component message) {
			super(x, y, width, 11, message);
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			Font textRenderer = Minecraft.getInstance().font;
			context.drawString(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? CommonColors.WHITE : CommonColors.GRAY, true);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			super.onClick(click, doubled);
			EditBarWidget.this.visible = false;
			if (runnable != null) runnable.run();
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	public static class EnumCyclingOption<T extends Enum<T>> extends AbstractWidget {

		private T current;
		private final T[] values;
		private Consumer<T> onChange = null;

		public EnumCyclingOption(int x, int y, int width, Component message, Class<T> enumClass) {
			super(x, y, width, 11, message);
			values = enumClass.getEnumConstants();
			current = values[0];
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			Font textRenderer = Minecraft.getInstance().font;
			context.drawString(textRenderer, getMessage(), getX() + 1, getY() + 1, CommonColors.WHITE, true);
			String string = current.toString();
			context.drawString(textRenderer, string, getRight() - textRenderer.width(string) - 1, getY() + 1, CommonColors.WHITE, true);
		}

		public void setCurrent(T current) {
			this.current = current;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			current = EnumUtils.cycle(current);
			if (onChange != null) onChange.accept(current);
			super.onClick(click, doubled);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {
		}

		public void setOnChange(Consumer<T> onChange) {
			this.onChange = onChange;
		}

		int getLongestOptionWidth() {
			int m = 0;
			for (T value : values) {
				int i = Minecraft.getInstance().font.width(value.toString());
				m = Math.max(m, i);
			}
			return m;
		}
	}

	public static class BooleanOption extends AbstractWidget {

		private boolean current = false;
		private BooleanConsumer onChange = null;

		public BooleanOption(int x, int y, int width, Component message) {
			super(x, y, width, 11, message);
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			Font textRenderer = Minecraft.getInstance().font;
			context.drawString(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? -1 : CommonColors.GRAY, true);
			HudHelper.drawBorder(context, getRight() - 10, getY() + 1, 9, 9, active ? -1 : CommonColors.GRAY);
			if (current && active) context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, CommonColors.WHITE);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			current = !current;
			if (onChange != null) onChange.accept(current);
			super.onClick(click, doubled);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {
		}

		public void setCurrent(boolean current) {
			this.current = current;
		}

		public void setOnChange(BooleanConsumer onChange) {
			this.onChange = onChange;
		}
	}

	public static class ColorOption extends AbstractWidget {

		public void setCurrent(int current) {
			this.current = current;
		}

		private int current = 0;
		private Consumer<Color> onChange = null;
		private final Screen parent;

		public ColorOption(int x, int y, int width, Component message, Screen parent) {
			super(x, y, width, 11, message);
			this.parent = parent;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			if (isMouseOver(mouseX, mouseY)) {
				context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
			}
			Font textRenderer = Minecraft.getInstance().font;
			context.drawString(textRenderer, getMessage(), getX() + 1, getY() + 1, active ? -1 : CommonColors.GRAY, true);
			HudHelper.drawBorder(context, getRight() - 10, getY() + 1, 9, 9, active ? -1 : CommonColors.GRAY);
			context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, active ? current : CommonColors.GRAY);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			super.onClick(click, doubled);
			Minecraft.getInstance().setScreen(new EditBarColorPopup(Component.literal("Edit ").append(getMessage()), parent, this::set));
		}

		private void set(Color color) {
			current = color.getRGB();
			if (onChange != null) onChange.accept(color);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}

		public void setOnChange(Consumer<Color> onChange) {
			this.onChange = onChange;
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
