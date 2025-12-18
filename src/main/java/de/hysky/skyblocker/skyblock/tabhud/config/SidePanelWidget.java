package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

class SidePanelWidget extends AbstractContainerWidget {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private static final int TOP_MARGIN = 16; // this is kinda horribly used but this widget will only be used here so it is whatever.
	private static final int SCROLLBAR_AREA = SCROLLBAR_WIDTH + 1; // 1 for padding

	private final Minecraft client = Minecraft.getInstance();
	private final List<AbstractWidget> optionWidgets = new ArrayList<>();

	private LinearLayout layout = LinearLayout.vertical();
	boolean rightSide = false;
	private int targetX = 0;
	private float animation = 0.0f;
	private int animationStart = 0;
	private int animationEnd = 0;
	private boolean isOpen = false;

	private @Nullable HudWidget hudWidget;

	SidePanelWidget(int width, int height) {
		super(0, TOP_MARGIN, width, height - TOP_MARGIN, Component.literal("Side Panel"));
		visible = false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return optionWidgets;
	}

	@Override
	protected int contentHeight() {
		return layout.getHeight();
	}

	@Override
	protected double scrollRate() {
		return 5;
	}

	private boolean isNotVisible(int top, int bottom) {
		return !(bottom - this.scrollAmount() >= this.getY()) || !(top - this.scrollAmount() <= this.getY() + this.getHeight());
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		if (animation >= 0) {
			if (animation < 1.0f) {
				setX(animationStart + (int) ((animationEnd - animationStart) * animation));
				animation += Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 50 * 7.5f / 1000.f;
			} else {
				setX(animationEnd);
				animation = -1f;
			}
		}
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() - 4, getY() - 4 - TOP_MARGIN, getWidth() + 8, getHeight() + 8 + TOP_MARGIN);
		context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

		for (AbstractWidget clickableWidget : this.optionWidgets) {
			if (isNotVisible(clickableWidget.getY(), clickableWidget.getBottom())) continue;
			clickableWidget.render(context, mouseX, mouseY, deltaTicks);
		}
		context.disableScissor();
		this.renderScrollbar(context, mouseX, mouseY);
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
		layout = LinearLayout.vertical().spacing(5);
		layout.defaultCellSetting().alignHorizontallyCenter();
		optionWidgets.clear();
		add(new StringWidget(0, 15, hudWidget.getInformation().displayName().copy().withStyle(ChatFormatting.UNDERLINE), client.font) {
			@Override
			public void setWidth(int width) {
				setMaxWidth(width, TextOverflow.SCROLLING);
			}
		});
		add(Button.builder(Component.literal("Remove"), b -> config.removeWidget(hudWidget)).build()); // TODO translatable
		layout.addChild(SpacerElement.height(10));

		// Per screen options
		MultiLineTextWidget textWidget = null;
		if (hudWidget.renderingInformation.inherited) {
			// TODO add a goto location button
			textWidget = new MultiLineTextWidget(Component.literal("This widget is from a parent screen, edit it there or create a copy."), client.font);
			add(textWidget);
			add(Button.builder(Component.literal("Create Copy"), b -> { // TODO translatable
				hudWidget.renderingInformation.inherited = false;
				open(hudWidget, config, rightSide, x);
			}).build());
		} else {
			List<WidgetOption<?>> options = new ArrayList<>();
			hudWidget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				add(option.createNewWidget(config));
			}
		}

		layout.addChild(SpacerElement.height(10));

		// Normal options
		List<WidgetOption<?>> options = new ArrayList<>();
		hudWidget.getOptions(options);
		for (WidgetOption<?> option : options) {
			add(option.createNewWidget(config));
		}

		// Position everything
		for (AbstractWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - SCROLLBAR_AREA);
		}
		if (textWidget != null) textWidget.setMaxWidth(getWidth() - SCROLLBAR_AREA);

		layout.setPosition(getX(), getY() - (int) scrollAmount());
		layout.arrangeElements();
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
	public void setScrollAmount(double scrollY) {
		super.setScrollAmount(scrollY);
		layout.setY(getY() - (int) scrollAmount());
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
		if (this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (AbstractWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - SCROLLBAR_AREA); // remove 6 for scrollbar and one for a liiiitle padding
		}
		layout.arrangeElements();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height - TOP_MARGIN);
	}

	public @Nullable HudWidget getHudWidget() {
		return hudWidget;
	}

	private void add(AbstractWidget widget) {
		optionWidgets.add(widget);
		layout.addChild(widget);
	}


	@Override
	protected int scrollBarX() {
		return rightSide ? super.scrollBarX() : 0;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
