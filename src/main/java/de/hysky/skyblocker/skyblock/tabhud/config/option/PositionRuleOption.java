package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PositionRuleOption implements WidgetOption<PositionRule> {

	private final Supplier<PositionRule> valueGetter;
	private final Consumer<PositionRule> valueSetter;

	public PositionRuleOption(Supplier<PositionRule> valueGetter, Consumer<PositionRule> valueSetter) {
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
	}

	@Override
	public @NotNull PositionRule getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(@NotNull PositionRule value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return "position_rule";
	}

	@Override
	public @NotNull JsonElement toJson() {
		return PositionRule.CODEC.encodeStart(JsonOps.INSTANCE, valueGetter.get()).getOrThrow();
	}

	@Override
	public void fromJson(@NotNull JsonElement json) {
		valueSetter.accept(PositionRule.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
	}

	@Override
	public @NotNull ClickableWidget createNewWidget(WidgetConfig config) {
		return new PositionRuleOptionWidget(config);
	}

	private class PositionRuleOptionWidget extends ContainerWidget {

		// TODO editable coords maybe?

		DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
		List<ClickableWidget> widgets = new ArrayList<>();
		private final TextWidget coordsDisplay;
		private final ButtonWidget parentButton;

		private final WidgetConfig widgetConfig;

		private PositionRuleOptionWidget(WidgetConfig config) {
			super(0, 0, 0, 0, Text.literal("hi"));
			this.widgetConfig = config;
			layout.getMainPositioner().alignHorizontalCenter();
			// TODO translatable
			parentButton = ButtonWidget.builder(Text.literal("Parent: ").append(getParentName()), ignored -> config.promptSelectWidget(this::onWidgetSelected, false)).build();
			coordsDisplay = new TextWidget(Text.literal("hi"), MinecraftClient.getInstance().textRenderer);
			coordsDisplay.setHeight(coordsDisplay.getHeight() + 6);
			AnchorSelectionWidget parentPoint = new AnchorSelectionWidget(config, Text.literal("Parent Point"), true);
			AnchorSelectionWidget thisPoint = new AnchorSelectionWidget(config, Text.literal("This Point"), false);
			add(parentButton);
			add(coordsDisplay);
			add(parentPoint);
			add(thisPoint);
			layout.refreshPositions();
			setHeight(layout.getHeight());
		}

		private void add(ClickableWidget widget) {
			widgets.add(widget);
			layout.add(widget);
		}

		private void onWidgetSelected(@Nullable HudWidget selectedWidget) {
			PositionRule oldRule = valueGetter.get();

			HudWidget editedWidget = widgetConfig.getEditedWidget();
			int thisAnchorX = (int) (editedWidget.getX() + oldRule.thisPoint().horizontalPoint().getPercentage() * editedWidget.getWidth());
			int thisAnchorY = (int) (editedWidget.getY() + oldRule.thisPoint().verticalPoint().getPercentage() * editedWidget.getHeight());

			int otherAnchorX = selectedWidget == null ? 0 : selectedWidget.getX();
			int otherAnchorY = selectedWidget == null ? 0 : selectedWidget.getY();

			PositionRule newRule = new PositionRule(
					selectedWidget == null ? "screen" : selectedWidget.getId(),
					PositionRule.Point.DEFAULT,
					oldRule.thisPoint(),
					thisAnchorX - otherAnchorX,
					thisAnchorY - otherAnchorY
			);
			if (selectedWidget != null) {
				parentButton.setMessage(Text.literal("Parent: ").append(selectedWidget.getInformation().displayName()));
			} else {
				parentButton.setMessage(Text.literal("Parent: ").append("Screen"));
			}
			valueSetter.accept(newRule);
		}

		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			for (ClickableWidget widget : widgets) {
				widget.setWidth(width);
			}
			layout.refreshPositions();
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

		private Text getParentName() {
			PositionRule rule = valueGetter.get();
			if (rule.parent().equals("screen")) {
				return Text.literal("Screen");
			} else {
				HudWidget widget = WidgetManager.getWidgetOrPlaceholder(rule.parent());
				return widget.getInformation().displayName();
			}
		}

		@Override
		public List<? extends Element> children() {
			return widgets;
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return getHeight();
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 0;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			coordsDisplay.setMessage(Text.literal("x: " + valueGetter.get().relativeX() + ", y: " + valueGetter.get().relativeY()));
			for (ClickableWidget widget : widgets) {
				widget.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private class AnchorSelectionWidget extends ClickableWidget {
		private final boolean parent;
		private @Nullable PositionRule.Point hoveredPoint = null;
		private final WidgetConfig widgetConfig;

		private AnchorSelectionWidget(WidgetConfig config, Text text, boolean parent) {
			super(0, 0, 20, 40, text);
			this.parent = parent;
			widgetConfig = config;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			hoveredPoint = null;
			context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX(), getY(), Colors.WHITE, true);
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(getX(), getY() + 10);
			// Rectangle thing
			int x = getWidth() / 6;
			int w = (int) (4 * getWidth() / 6f);
			int y = 5; // 30 / 6
			int h = 20;

			context.drawBorder(x, y + 1, w, h, Colors.WHITE);
			PositionRule rule = valueGetter.get();
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

					context.fill(squareX - 1, squareY - 1, squareX + 2, squareY + 2, hoveredAnchor ? Colors.RED : selectedAnchor ? Colors.YELLOW : Colors.WHITE);
				}
			}
			context.getMatrices().popMatrix();
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			HudWidget editedWidget = widgetConfig.getEditedWidget();
			if (hoveredPoint != null) {
				PositionRule oldRule = valueGetter.get();
				// Get the x, y of the parent's point
				float scale = SkyblockerConfigManager.get().uiAndVisuals.hud.hudScale / 100.f;
				ScreenPos startPos = WidgetPositioner.getStartPosition(oldRule.parent(), (int) (widgetConfig.getScreenWidth() / scale), (int) (widgetConfig.getScreenHeight() / scale), parent ? hoveredPoint : oldRule.parentPoint());
				// Same but for the affected widget
				PositionRule.Point thisPoint = parent ? oldRule.thisPoint() : hoveredPoint;
				ScreenPos endPos = new ScreenPos(
						(int) (editedWidget.getX() + thisPoint.horizontalPoint().getPercentage() * editedWidget.getWidth()),
						(int) (editedWidget.getY() + thisPoint.verticalPoint().getPercentage() * editedWidget.getHeight())
				);

				if (parent) {
					valueSetter.accept(new PositionRule(
							oldRule.parent(),
							hoveredPoint,
							oldRule.thisPoint(),
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y()));
				} else {
					valueSetter.accept(new PositionRule(
							oldRule.parent(),
							oldRule.parentPoint(),
							hoveredPoint,
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y()));
				}
			}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
