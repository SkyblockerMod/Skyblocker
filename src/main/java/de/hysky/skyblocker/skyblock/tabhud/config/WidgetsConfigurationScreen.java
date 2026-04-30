package de.hysky.skyblocker.skyblock.tabhud.config;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;

public class WidgetsConfigurationScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(
				ClientCommands.literal(SkyblockerMod.NAMESPACE).then(ClientCommands.literal("hud").executes(Scheduler.queueOpenScreenCommand(WidgetsConfigurationScreen::new)))
		));
	}

	/**
	 * The currently edited location. {@link Location#UNKNOWN} if editing the global skyblock screen.
	 */
	private Location currentLocation;
	private WidgetManager.ScreenLayer currentScreenLayer;

	private final ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder();
	private ScreenConfig screenConfig;
	private ConfigLayerBuilder builder;

	private SidePanelWidget sidePanelWidget;
	private AddWidgetWidget addWidgetWidget;
	private TopBarWidget topBarWidget;

	private @Nullable PositionedWidget hoveredWidget;
	private @Nullable PositionedWidget selectedWidget;
	/**
	 * Where the user started dragging relative to the dragged widget's position. Null if not dragging.
	 */
	private @Nullable ScreenPosition dragRelative = null;
	private boolean openPanelAfterDragging = false;
	boolean autoAnchor = true;

	private @Nullable SelectWidgetPrompt selectWidgetPrompt = null;

	public WidgetsConfigurationScreen() {
		super(Component.literal("Widgets Config Screen"));
		currentLocation = Utils.getLocation();
		currentScreenLayer = WidgetManager.ScreenLayer.HUD;
		screenConfig = WidgetManager.getScreenConfig(currentLocation);
		screenBuilder.setConfig(screenConfig);
		builder = screenBuilder.get(currentScreenLayer);
		builder.update();
		builder.updateTab();
	}

	public void setCurrentLocation(Location newLocation) {
		builder.serializeConfig();
		this.currentLocation = newLocation;
		screenConfig = WidgetManager.getScreenConfig(currentLocation);
		screenBuilder.setConfig(screenConfig);
		builder = screenBuilder.get(currentScreenLayer);
		builder.update();
		builder.updateTab();
	}

	public void setCurrentScreenLayer(WidgetManager.ScreenLayer newScreenLayer) {
		builder.serializeConfig();
		this.currentScreenLayer = newScreenLayer;
		builder = screenBuilder.get(newScreenLayer);
		builder.update();
		builder.updateTab();
	}

	@Override
	protected void init() {
		super.init();
		sidePanelWidget = new SidePanelWidget(width / 4, height, this);
		addWidgetWidget = new AddWidgetWidget(minecraft, this::addWidget);
		topBarWidget = new TopBarWidget(width, this);
		addWidget(addWidgetWidget);
		addWidget(topBarWidget);
		addWidget(sidePanelWidget);
		repositionElements();
	}

	private void addWidget(HudWidget widget) {
		builder.add(widget).rule = new PositionRule(
				"screen",
				PositionRule.Point.DEFAULT,
				PositionRule.Point.DEFAULT,
				(int) (minecraft.mouseHandler.getScaledXPos(minecraft.getWindow()) / TabHud.getScaleFactor()),
				(int) (minecraft.mouseHandler.getScaledYPos(minecraft.getWindow()) / TabHud.getScaleFactor())
		);
	}

	@Override
	protected void repositionElements() {
		sidePanelWidget.setWidth(width / 4);
		sidePanelWidget.setHeight(height);
		if (sidePanelWidget.isOpen()) sidePanelWidget.setX(sidePanelWidget.rightSide ? width - sidePanelWidget.getWidth() : 0);
		topBarWidget.setWidth(width);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractBackground(context, mouseX, mouseY, deltaTicks);
		Component text = Component.translatable("skyblocker.config.hud.screen.rightClick");
		int textWidth = font.width(text);
		// FIXME transparency and shadow
		context.textRenderer().accept((width - textWidth) / 2, (height - font.lineHeight) / 2, text);
	}

	@Override
	protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
		if (minecraft.level != null && !minecraft.hasControlDown()) super.extractBlurredBackground(graphics);
	}

	@Override
	protected void extractMenuBackground(GuiGraphicsExtractor graphics) {
		if (minecraft.level != null && !minecraft.hasControlDown()) super.extractMenuBackground(graphics);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);
		Matrix3x2fStack matrices = context.pose();
		float scale = TabHud.getScaleFactor();
		matrices.pushMatrix();
		matrices.scale(scale);
		builder.extractRenderStates(context, getScreenWidth(), getScreenHeight(), true);
		matrices.popMatrix();
		hoveredWidget = null;
		double scaledMouseX = mouseX / scale;
		double scaledMouseY = mouseY / scale;
		for (PositionedWidget hudWidget : builder.getRendered()) {
			if (hudWidget.widget.isMouseOver(scaledMouseX, scaledMouseY)) {
				hoveredWidget = hudWidget;
				break;
			}
		}

		Matrix3x2f scaleMatrix = new Matrix3x2f().scale(scale);
		if (hoveredWidget != null) {
			ScreenRectangle rect = hoveredWidget.widget.getRectangle().transformAxisAligned(scaleMatrix);
			GuiHelper.border(context, rect.left() - 1, rect.top() - 1, rect.width() + 2, rect.height() + 2, CommonColors.YELLOW);
		}
		if (selectedWidget != null) {
			ScreenRectangle rect = selectedWidget.widget.getRectangle().transformAxisAligned(scaleMatrix);
			GuiHelper.border(context, rect.left() - 1, rect.top() - 1, rect.width() + 2, rect.height() + 2, CommonColors.GREEN);
		}
		topBarWidget.visible = selectedWidget == null || selectedWidget.widget.getY() >= 16;

		sidePanelWidget.extractRenderState(context, mouseX, mouseY, deltaTicks);
		// Render on top of everything
		topBarWidget.extractRenderState(context, mouseX, mouseY, deltaTicks);
		addWidgetWidget.extractRenderState(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (super.mouseDragged(click, deltaX, deltaY)) return true;
		double mouseX = click.x();
		double mouseY = click.y();
		if (selectedWidget != null && dragRelative != null) {
			PositionRule oldRule = selectedWidget.rule;
			mouseX /= TabHud.getScaleFactor();
			mouseY /= TabHud.getScaleFactor();

			PositionRule.Point parentPoint;
			PositionRule.Point thisPoint;
			if (autoAnchor && oldRule.parent().isEmpty()) {
				parentPoint = thisPoint = getPoint(selectedWidget.widget, (int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y());
			} else {
				parentPoint = oldRule.parentPoint();
				thisPoint = oldRule.thisPoint();
			}
			String newParent = null;
			OptionalInt relativeX = OptionalInt.empty();
			OptionalInt relativeY = OptionalInt.empty();
			if (minecraft.hasShiftDown()) {
				final ScreenDirection[] directions = ScreenDirection.values();

				ScreenRectangle selectedRect = new ScreenRectangle((int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y(), selectedWidget.widget.getWidth(), selectedWidget.widget.getHeight());
				ScreenRectangle[] selectedSnapBoxes = Arrays.stream(directions).map(dir -> getBorder(selectedRect, dir)).toArray(ScreenRectangle[]::new);

				int distanceToCursor = Integer.MAX_VALUE;
				for (PositionedWidget positionedWidget : builder.getRendered()) {
					if (positionedWidget == selectedWidget) continue;
					if (selectedWidget.widget.getInternalID().equals(positionedWidget.rule.parent().orElse(null))) continue;
					ScreenRectangle otherRect = positionedWidget.widget.getRectangle();
					for (ScreenDirection direction : directions) {
						ScreenRectangle otherSnapBox = getBorder(otherRect, direction);
						ScreenRectangle selectedSnapBox = selectedSnapBoxes[direction.getOpposite().ordinal()];

						int dist = direction.getAxis() == ScreenAxis.HORIZONTAL ? Math.abs((int) mouseX - otherSnapBox.getBorder(direction).getCenterInAxis(ScreenAxis.HORIZONTAL)) : Math.abs((int) mouseY - otherSnapBox.getBorder(direction).getCenterInAxis(ScreenAxis.VERTICAL));
						if (!selectedSnapBox.overlaps(otherSnapBox) || dist > distanceToCursor) continue;
						PositionRule.Point point = getPoint(positionedWidget.widget);
						switch (direction) {
							case LEFT -> {
								relativeX = OptionalInt.of(-2);
								relativeY = OptionalInt.empty();
								parentPoint = new PositionRule.Point(point.verticalPoint(), PositionRule.HorizontalPoint.LEFT);
								thisPoint = new PositionRule.Point(point.verticalPoint(), PositionRule.HorizontalPoint.RIGHT);
							}
							case RIGHT -> {
								relativeX = OptionalInt.of(1);
								relativeY = OptionalInt.empty();
								parentPoint = new PositionRule.Point(point.verticalPoint(), PositionRule.HorizontalPoint.RIGHT);
								thisPoint = new PositionRule.Point(point.verticalPoint(), PositionRule.HorizontalPoint.LEFT);
							}
							case UP -> {
								relativeY = OptionalInt.of(-2);
								relativeX = OptionalInt.empty();
								parentPoint = new PositionRule.Point(PositionRule.VerticalPoint.TOP, point.horizontalPoint());
								thisPoint = new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, point.horizontalPoint());
							}
							case DOWN -> {
								relativeY = OptionalInt.of(1);
								relativeX = OptionalInt.empty();
								parentPoint = new PositionRule.Point(PositionRule.VerticalPoint.BOTTOM, point.horizontalPoint());
								thisPoint = new PositionRule.Point(PositionRule.VerticalPoint.TOP, point.horizontalPoint());
							}
						}
						newParent = positionedWidget.widget.getInternalID();
						distanceToCursor = dist;
					}
				}
			}
			ScreenPosition startPosition = WidgetPositioner.getStartPosition(newParent, getScreenWidth(), getScreenHeight(), parentPoint);
			selectedWidget.rule = new PositionRule(
					Optional.ofNullable(newParent),
					parentPoint,
					thisPoint,
					relativeX.orElse((int) mouseX - dragRelative.x() - startPosition.x() + (int) (selectedWidget.widget.getWidth() * thisPoint.horizontalPoint().getPercentage())),
					relativeY.orElse((int) mouseY - dragRelative.y() - startPosition.y() + (int) (selectedWidget.widget.getHeight() * thisPoint.verticalPoint().getPercentage()))
			);
			updateBuilderPositions();
			ScreenRectangle sidePanel = new ScreenRectangle(sidePanelWidget.getX(), sidePanelWidget.getY(), sidePanelWidget.getWidth(), sidePanelWidget.getHeight());
			ScreenRectangle selected = new ScreenRectangle(selectedWidget.widget.getX(), selectedWidget.widget.getY(), selectedWidget.widget.getWidth(), selectedWidget.widget.getHeight());
			if (sidePanelWidget.isOpen() && sidePanel.overlaps(selected)) {
				sidePanelWidget.close();
				openPanelAfterDragging = true;
			}
			return true;
		}
		return false;
	}

	private PositionRule.Point getPoint(HudWidget widget) {
		return getPoint(widget, widget.getX(), widget.getY());
	}

	private PositionRule.Point getPoint(HudWidget widget, int x, int y) {
		int widgetCenterX = x + widget.getWidth() / 2 - getScreenWidth() / 2;
		int widgetCenterY = y + widget.getHeight() / 2 - getScreenHeight() / 2;
		PositionRule.HorizontalPoint hPoint = widgetCenterX < -25 ? PositionRule.HorizontalPoint.LEFT : widgetCenterX > 25 ? PositionRule.HorizontalPoint.RIGHT : PositionRule.HorizontalPoint.CENTER;
		PositionRule.VerticalPoint vPoint = widgetCenterY < -25 ? PositionRule.VerticalPoint.TOP : widgetCenterY > 25 ? PositionRule.VerticalPoint.BOTTOM : PositionRule.VerticalPoint.CENTER;
		return new PositionRule.Point(vPoint, hPoint);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (super.mouseClicked(click, doubled)) return true;
		double mouseX = click.x();
		double mouseY = click.y();
		if (selectWidgetPrompt != null) {
			if (hoveredWidget != null && !selectWidgetPrompt.allowItself() && hoveredWidget.equals(selectedWidget)) return true;
			selectWidgetPrompt.callback().accept(hoveredWidget == null ? null : hoveredWidget.widget);
			selectWidgetPrompt = null;
			sidePanelWidget.open();
			return true;
		}
		if (hoveredWidget == null) {
			if (sidePanelWidget.isOpen()) sidePanelWidget.close();
			selectedWidget = null;
			if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				List<HudWidget> availableWidgets = new ArrayList<>(WidgetManager.getWidgetsAvailableIn(currentLocation));
				availableWidgets.removeAll(builder.getRendered().stream().map(w -> w.widget).toList()); // remove already present widgets
				addWidgetWidget.openWith(availableWidgets);
				addWidgetWidget.setX(Math.clamp((int) mouseX, 5, width - addWidgetWidget.getWidth() - 5));
				addWidgetWidget.setY(Math.clamp((int) mouseY, 5, height - addWidgetWidget.getHeight() - 5));
				addWidgetWidget.refreshScrollAmount(); // refreshes the positions of the entries
			}
			return true;
		}
		if (!hoveredWidget.equals(selectedWidget)) {
			selectedWidget = hoveredWidget;
		}
		mouseX /= TabHud.getScaleFactor();
		mouseY /= TabHud.getScaleFactor();
		dragRelative = new ScreenPosition((int) (mouseX - selectedWidget.widget.getX()), (int) (mouseY - selectedWidget.widget.getY()));
		if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && (!sidePanelWidget.isOpen() || !selectedWidget.equals(sidePanelWidget.getHudWidget()))) {
			openSidePanel();
		} else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && sidePanelWidget.isOpen() && !selectedWidget.equals(sidePanelWidget.getHudWidget())) {
			openSidePanel();
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		dragRelative = null;
		if (openPanelAfterDragging) {
			openPanelAfterDragging = false;
			openSidePanel();
		}
		return super.mouseReleased(click);
	}

	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		if (selectedWidget != null && selectedWidget == hoveredWidget) {
			boolean move = true;
			int x = 0, y = 0;
			if (keyInput.isLeft()) x = -1;
			else if (keyInput.isRight()) x = 1;
			else if (keyInput.isUp()) y = -1;
			else if (keyInput.isDown()) y = 1;
			else move = false;

			if (move) {
				PositionRule oldRule = selectedWidget.rule;
				selectedWidget.rule = new PositionRule(
						oldRule.parent(),
						oldRule.parentPoint(),
						oldRule.thisPoint(),
						oldRule.relativeX() + x,
						oldRule.relativeY() + y
				);
				updateBuilderPositions();
				return true;
			}
			if (keyInput.key() == GLFW.GLFW_KEY_DELETE) {
				removeWidget(selectedWidget.widget);
				return true;
			}
		}
		return super.keyPressed(keyInput);
	}

	private void openSidePanel() {
		if (selectedWidget == null) return;
		boolean rightSide = selectedWidget.widget.getX() + selectedWidget.widget.getWidth() / 2 < getScreenWidth() / 2;
		sidePanelWidget.open(selectedWidget, rightSide);
	}

	@Override
	public void tick() {
		if (selectedWidget == null && sidePanelWidget.isOpen()) sidePanelWidget.close();
		for (PositionedWidget widget : builder.getRendered()) {
			if (widget.widget.getRectangle().intersects(new ScreenRectangle(0, 0, getScreenWidth(), getScreenHeight())) || widget.rule.parent().isPresent()) continue;
			widget.rule = PositionRule.DEFAULT;
			updateBuilderPositions();
		}
	}

	@Override
	public void removed() {
		builder.serializeConfig();
		WidgetManager.SCREEN_BUILDER.hud().update();
	}

	private static ScreenRectangle getBorder(ScreenRectangle rect, ScreenDirection side) {
		int extraX = rect.width() / 2;
		int extraY = rect.height() / 2;
		final int thickness = 5 + (side.getAxis() == ScreenAxis.HORIZONTAL ? extraX : extraY);
		int i = rect.getBoundInDirection(side);
		ScreenAxis otherAxis = side.getAxis().orthogonal();
		int j = rect.getBoundInDirection(otherAxis.getNegative());
		int k = rect.getLength(otherAxis);
		ScreenRectangle screenRect = ScreenRectangle.of(side.getAxis(), i, j, thickness, k);
		int offsetX = side.getAxis() == ScreenAxis.HORIZONTAL ? (side.isPositive() ? -extraX : -5) : 0;
		int offsetY = side.getAxis() == ScreenAxis.VERTICAL ? (side.isPositive() ? -extraY : -5) : 0;
		return new ScreenRectangle(screenRect.left() + offsetX, screenRect.top() + offsetY, screenRect.width(), screenRect.height());
	}

	public void promptSelectWidget(Consumer<@Nullable HudWidget> callback, boolean allowItself) {
		selectWidgetPrompt = new SelectWidgetPrompt(callback, allowItself);
		sidePanelWidget.close();
	}

	public void removeWidget(HudWidget widget) {
		builder.remove(widget);
		// FIXME
		/*PositionRule deleted = widget.getPositionRule();
		for (PositionedWidget positionedWidget : builder.getWidgets()) {
			PositionRule rule = positionedWidget.rule;
			if (rule.parent().isEmpty()) continue;
			if (rule.parent().get().equals(widget.getInternalID())) {
				positionedWidget.setPositionRule(new PositionRule(
						deleted.parent(),
						deleted.parentPoint(),
						rule.thisPoint(),
						deleted.relativeX() + rule.relativeX(),
						deleted.relativeY() + rule.relativeY()
				));
			}
		}*/
		if (selectedWidget != null && selectedWidget.widget == widget) {
			sidePanelWidget.close();
			selectedWidget = null;
		}
	}

	private void updateBuilderPositions() {
		builder.updatePositions(getScreenWidth(), getScreenHeight());
	}

	public HudWidget getEditedWidget() {
		if (selectedWidget == null) {
			LOGGER.warn("Trying to edit selected widget but nothing is selected?", new Throwable());
			return new PlaceholderWidget(""); // this shouldn't cause issues
		}
		return selectedWidget.widget;
	}

	public int getScreenWidth() {
		return (int) (width / TabHud.getScaleFactor());
	}

	public int getScreenHeight() {
		return (int) (height / TabHud.getScaleFactor());
	}

	public void openPopup(Function<Screen, Screen> popupCreator) {
		minecraft.setScreen(popupCreator.apply(this));
	}

	private record SelectWidgetPrompt(Consumer<@Nullable HudWidget> callback, boolean allowItself) {}
}
