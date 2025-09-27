package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class SidePanelWidget extends ContainerWidget {
	private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_outer_space");
	private static final int TOP_MARGIN = 16; // this is kinda horribly used but this widget will only be used here so it is whatever.
	private static final int SCROLLBAR_AREA = SCROLLBAR_WIDTH + 1; // 1 for padding

	private final MinecraftClient client = MinecraftClient.getInstance();
	private final List<ClickableWidget> optionWidgets = new ArrayList<>();

	private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
	boolean rightSide = false;
	private int targetX = 0;
	private float animation = 0.0f;
	private int animationStart = 0;
	private int animationEnd = 0;
	private boolean isOpen = false;

	private @Nullable HudWidget hudWidget;

	SidePanelWidget(int width, int height) {
		super(0, TOP_MARGIN, width, height - TOP_MARGIN, Text.literal("Side Panel"));
		visible = false;
	}

	@Override
	public List<? extends Element> children() {
		return optionWidgets;
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return layout.getHeight();
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	private boolean isNotVisible(int top, int bottom) {
		return !(bottom - this.getScrollY() >= this.getY()) || !(top - this.getScrollY() <= this.getY() + this.getHeight());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if (animation >= 0) {
			if (animation < 1.0f) {
				setX(animationStart + (int) ((animationEnd - animationStart) * animation));
				animation += MinecraftClient.getInstance().getRenderTickCounter().getFixedDeltaTicks() * 50 * 7.5f / 1000.f;
			} else {
				setX(animationEnd);
				animation = -1f;
			}
		}
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() - 4, getY() - 4 - TOP_MARGIN, getWidth() + 8, getHeight() + 8 + TOP_MARGIN);
		context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

		for (ClickableWidget clickableWidget : this.optionWidgets) {
			if (isNotVisible(clickableWidget.getY(), clickableWidget.getBottom())) continue;
			clickableWidget.render(context, mouseX, mouseY, deltaTicks);
		}
		context.disableScissor();
		this.drawScrollbar(context);
	}

	public void open() {
		if (isOpen()) return;
		visible = true;
		animation = 0.0f;
		animationEnd = targetX;
		animationStart = targetX + (rightSide ? (getWidth() + 8) : (-getWidth() - 8));
		isOpen = true;
	}

	public void open(HudWidget hudWidget, WidgetConfig config, boolean rightSide, int x) {
		this.hudWidget = hudWidget;
		layout = DirectionalLayoutWidget.vertical().spacing(5);
		optionWidgets.clear();
		add(new TextWidget(0, 15, hudWidget.getInformation().displayName().copy().formatted(Formatting.UNDERLINE), client.textRenderer));
		add(ButtonWidget.builder(Text.literal("Remove"), b -> config.removeWidget(hudWidget)).build()); // TODO translatable
		layout.add(EmptyWidget.ofHeight(10));

		// Per screen options
		MultilineTextWidget textWidget = null;
		if (hudWidget.isInherited()) {
			// TODO add a goto location button
			textWidget = new MultilineTextWidget(Text.literal("This widget is from a parent screen, edit it there or create a copy."), client.textRenderer);
			add(textWidget);
			add(ButtonWidget.builder(Text.literal("Create Copy"), b -> { // TODO translatable
				hudWidget.setInherited(false);
				open(hudWidget, config, rightSide, x);
			}).build());
		} else {
			List<WidgetOption<?>> options = new ArrayList<>();
			hudWidget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				add(option.createNewWidget(config));
			}
		}

		layout.add(EmptyWidget.ofHeight(10));

		// Normal options
		List<WidgetOption<?>> options = new ArrayList<>();
		hudWidget.getOptions(options);
		for (WidgetOption<?> option : options) {
			add(option.createNewWidget(config));
		}

		// Position everything
		for (ClickableWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - SCROLLBAR_AREA);
		}
		if (textWidget != null) textWidget.setMaxWidth(getWidth() - SCROLLBAR_AREA);

		layout.setPosition(getX(), getY() - (int) getScrollY());
		layout.refreshPositions();
		if (isOpen() && (x != targetX || rightSide != this.rightSide)) {
			isOpen = false;
		}
		this.rightSide = rightSide;
		targetX = x;
		open();
	}

	public void close() {
		if (!isOpen()) return;
		animationStart = getX();
		animationEnd = getX() + (rightSide ? (getWidth() + 8) : (-getWidth() - 8));
		animation = 0;
		isOpen = false;
	}

	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void setScrollY(double scrollY) {
		super.setScrollY(scrollY);
		layout.setY(getY() - (int) getScrollY());
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		if (rightSide) {
			layout.setX(x);
		} else {
			layout.setX(x + SCROLLBAR_AREA);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (ClickableWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - SCROLLBAR_AREA); // remove 6 for scrollbar and one for a liiiitle padding
		}
		layout.refreshPositions();
	}

	public @Nullable HudWidget getHudWidget() {
		return hudWidget;
	}

	private void add(@NotNull ClickableWidget widget) {
		optionWidgets.add(widget);
		layout.add(widget);
	}


	@Override
	protected int getScrollbarX() {
		return rightSide ? super.getScrollbarX() : 0;
	}

	@Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
