package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.render.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class PositionRuleWidget extends AbstractContainerWidget {

	// TODO editable coords maybe?

	LinearLayout layout = LinearLayout.vertical();
	List<AbstractWidget> widgets = new ArrayList<>();
	private final StringWidget coordsDisplay;
	private final Button parentButton;
	private final PositionedWidget modifiedWidget;

	private final WidgetsConfigurationScreen widgetConfig;

	PositionRuleWidget(WidgetsConfigurationScreen config, PositionedWidget modifiedWidget) {
		super(0, 0, 0, 0, Component.literal("hi"), defaultSettings(0));
		this.widgetConfig = config;
		this.modifiedWidget = modifiedWidget;
		layout.defaultCellSetting().alignHorizontallyCenter();
		parentButton = Button.builder(Component.translatable("skyblocker.config.hud.position.parent", getParentName()), _ -> config.promptSelectWidget(this::onWidgetSelected, false)).build();
		coordsDisplay = new StringWidget(Component.literal("hi"), Minecraft.getInstance().font);
		coordsDisplay.setHeight(coordsDisplay.getHeight() + 6);
		AnchorSelectionWidget parentPoint = new AnchorSelectionWidget(Component.translatable("skyblocker.config.hud.position.pointParent"), true);
		AnchorSelectionWidget thisPoint = new AnchorSelectionWidget(Component.translatable("skyblocker.config.hud.position.pointThis"), false);
		add(new StringWidget(Component.literal("Positioning"), Minecraft.getInstance().font));
		add(parentButton);
		add(coordsDisplay);
		add(parentPoint);
		add(thisPoint);
		layout.arrangeElements();
		setHeight(layout.getHeight());
	}

	private void add(AbstractWidget widget) {
		widgets.add(widget);
		layout.addChild(widget);
	}

	private void onWidgetSelected(@Nullable HudWidget selectedWidget) {
		PositionRule oldRule = modifiedWidget.rule;

		HudWidget editedWidget = widgetConfig.getEditedWidget();
		int thisAnchorX = (int) (editedWidget.getX() + oldRule.thisPoint().horizontalPoint().getPercentage() * editedWidget.getWidth());
		int thisAnchorY = (int) (editedWidget.getY() + oldRule.thisPoint().verticalPoint().getPercentage() * editedWidget.getHeight());

		int otherAnchorX = selectedWidget == null ? 0 : selectedWidget.getX();
		int otherAnchorY = selectedWidget == null ? 0 : selectedWidget.getY();

		PositionRule newRule = new PositionRule(
				Optional.ofNullable(selectedWidget).map(HudWidget::getInternalID),
				PositionRule.Point.DEFAULT,
				oldRule.thisPoint(),
				thisAnchorX - otherAnchorX,
				thisAnchorY - otherAnchorY
		);
		if (selectedWidget != null) {
			parentButton.setMessage(Component.translatable("skyblocker.config.hud.position.parent", selectedWidget.getInformation().displayName()));
		} else {
			parentButton.setMessage(Component.translatable("skyblocker.config.hud.position.parent", Component.translatable("skyblocker.config.hud.position.parent.screen")));
		}
		modifiedWidget.rule = newRule;
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (AbstractWidget widget : widgets) {
			widget.setWidth(width);
		}
		coordsDisplay.setMaxWidth(width, StringWidget.TextOverflow.SCROLLING);
		layout.arrangeElements();
		setHeight(layout.getHeight());
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		layout.setX(x);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		layout.setY(y);
	}

	private Component getParentName() {
		PositionRule rule = modifiedWidget.rule;
		if (rule.parent().isEmpty()) {
			return Component.translatable("skyblocker.config.hud.position.parent.screen");
		} else {
			HudWidget widget = WidgetManager.getWidgetOrPlaceholder(rule.parent().get());
			return widget.getInformation().displayName();
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	protected int contentHeight() {
		return getHeight();
	}

	@Override
	protected double scrollRate() {
		return 0;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		coordsDisplay.setMessage(Component.literal("x: " + modifiedWidget.rule.relativeX() + ", y: " + modifiedWidget.rule.relativeY()));
		layout.arrangeElements();
		for (AbstractWidget widget : widgets) {
			widget.extractRenderState(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	private class AnchorSelectionWidget extends AbstractWidget {
		private final boolean parent;
		private PositionRule.@Nullable Point hoveredPoint = null;

		private AnchorSelectionWidget(Component text, boolean parent) {
			super(0, 0, 20, 40, text);
			this.parent = parent;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
			hoveredPoint = null;
			context.text(Minecraft.getInstance().font, getMessage(), getX(), getY(), CommonColors.WHITE, true);
			context.pose().pushMatrix();
			context.pose().translate(getX(), getY() + 10);
			// Rectangle thing
			int x = getWidth() / 6;
			int w = (int) (4 * getWidth() / 6f);
			int y = 5; // 30 / 6
			int h = 20;

			GuiHelper.border(context, x, y + 1, w, h, CommonColors.WHITE);
			PositionRule rule = modifiedWidget.rule;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int squareX = x + (i * getWidth()) / 3;
					int squareY = y + (j * 30) / 3;
					PositionRule.Point point = (parent ? rule.parentPoint() : rule.thisPoint());
					boolean selectedAnchor = point.horizontalPoint().ordinal() == i && point.verticalPoint().ordinal() == j;
					boolean hoveredAnchor = mouseX >= getX() + i * getWidth() / 3 &&
							mouseX < getX() + (i + 1) * getWidth() / 3 &&
							mouseY >= getY() + 10 + j * 10 &&
							mouseY < getY() + 10 + (j + 1) * 10;

					if (hoveredAnchor) {
						PositionRule.VerticalPoint[] verticalPoints = PositionRule.VerticalPoint.values();
						PositionRule.HorizontalPoint[] horizontalPoints = PositionRule.HorizontalPoint.values();
						hoveredPoint = new PositionRule.Point(verticalPoints[j], horizontalPoints[i]);
					}

					context.fill(squareX - 1, squareY - 1, squareX + 2, squareY + 2, hoveredAnchor ? CommonColors.RED : selectedAnchor ? CommonColors.YELLOW : CommonColors.WHITE);
				}
			}
			context.pose().popMatrix();
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			HudWidget editedWidget = widgetConfig.getEditedWidget();
			if (hoveredPoint != null) {
				PositionRule oldRule = modifiedWidget.rule;
				// Get the x, y of the parent's point
				ScreenPosition startPos = WidgetPositioner.getStartPosition(oldRule.parent().orElse(null), widgetConfig.getScreenWidth(), widgetConfig.getScreenHeight(), parent ? hoveredPoint : oldRule.parentPoint());
				// Same but for the affected widget
				PositionRule.Point thisPoint = parent ? oldRule.thisPoint() : hoveredPoint;
				ScreenPosition endPos = new ScreenPosition(
						(int) (editedWidget.getX() + thisPoint.horizontalPoint().getPercentage() * editedWidget.getWidth()),
						(int) (editedWidget.getY() + thisPoint.verticalPoint().getPercentage() * editedWidget.getHeight())
				);

				if (parent) {
					modifiedWidget.rule = new PositionRule(
							oldRule.parent(),
							hoveredPoint,
							oldRule.thisPoint(),
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y());
				} else {
					modifiedWidget.rule = new PositionRule(
							oldRule.parent(),
							oldRule.parentPoint(),
							hoveredPoint,
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y());
				}
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}
}
