package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SidePanelWidget extends ContainerWidget {
	private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_outer_space");

	private final MinecraftClient client = MinecraftClient.getInstance();
	private final List<ClickableWidget> optionWidgets = new ArrayList<>();

	private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
	private boolean rightSide = false;
	private int targetX = 0;
	private float animation = 0.0f;
	private int animationStart = 0;
	private int animationEnd = 0;
	private boolean isOpen = false;

	private @Nullable HudWidget hudWidget;

	public SidePanelWidget(int width, int height) {
		super(0, 0, width, height, Text.literal("Side Panel"));
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
		context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, getX() - 4, getY() - 4, getWidth() + 8, getHeight() + 8);
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
		add(new TextWidget(0, 15, hudWidget.getInformation().displayName(), client.textRenderer));
		add(ButtonWidget.builder(Text.literal("Remove"), b -> config.removeWidget(hudWidget)).build()); // TODO translatable
		layout.add(EmptyWidget.ofHeight(5));

		if (hudWidget.isInherited()) {
			add(new TextWidget(Text.literal("This widget is from a parent screen, edit it there or create a copy."), client.textRenderer));
			add(ButtonWidget.builder(Text.literal("Create Copy"), b -> { // TODO translatable
				hudWidget.setInherited(false);
				open(hudWidget, config, rightSide, x);
			}).build());
		}
		else {
			List<WidgetOption<?>> options = new ArrayList<>();
			hudWidget.getOptions(options);
			for (WidgetOption<?> option : options) {
				add(option.createNewWidget(config));
			}
		}
		for (ClickableWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - 6 - 1); // remove 6 for scrollbar and one for a liiiitle padding
		}
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
			layout.setX(x + 6 + 1);
		}
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (ClickableWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - 6 - 1); // remove 6 for scrollbar and one for a liiiitle padding
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
