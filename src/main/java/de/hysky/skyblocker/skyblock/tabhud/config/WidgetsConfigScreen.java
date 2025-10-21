package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;

public class WidgetsConfigScreen extends Screen implements WidgetConfig {

	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("hud").executes(Scheduler.queueOpenScreenCommand(WidgetsConfigScreen::new)))
		));
	}

	/**
	 * The currently edited location. {@link Location#UNKNOWN} if editing the global skyblock screen.
	 */
	private @NotNull Location currentLocation;
	private @NotNull WidgetManager.ScreenLayer currentScreenLayer;

	private @NotNull ScreenBuilder builder;

	private SidePanelWidget sidePanelWidget;
	private AddWidgetWidget addWidgetWidget;
	private TopBarWidget topBarWidget;

	private @Nullable HudWidget hoveredWidget;
	private @Nullable HudWidget selectedWidget;
	/**
	 * Where the user started dragging relative to the widget's position. Null if not dragging.
	 */
	private @Nullable ScreenPos dragRelative = null;
	private boolean openPanelAfterDragging = false;
	boolean autoAnchor = true;
	boolean snapping = true;

	private @Nullable SelectWidgetPrompt selectWidgetPrompt = null;

	public WidgetsConfigScreen() {
		super(Text.literal("Widgets Config Screen"));
		currentLocation = Utils.getLocation();
		currentScreenLayer = WidgetManager.ScreenLayer.HUD;
		builder = WidgetManager.getScreenBuilder(currentLocation, currentScreenLayer);
		builder.updateWidgetsList();
		builder.updateTabWidgetsList();
	}

	public void setCurrentLocation(@NotNull Location newLocation) {
		builder.updateConfig();
		this.currentLocation = newLocation;
		builder = WidgetManager.getScreenBuilder(newLocation, currentScreenLayer);
		builder.updateWidgetsList();
		builder.updateTabWidgetsList();
	}

	public void setCurrentScreenLayer(@NotNull WidgetManager.ScreenLayer newScreenLayer) {
		builder.updateConfig();
		this.currentScreenLayer = newScreenLayer;
		builder = WidgetManager.getScreenBuilder(currentLocation, newScreenLayer);
		builder.updateWidgetsList();
		builder.updateTabWidgetsList();
	}

	@Override
	protected void init() {
		super.init();
		sidePanelWidget = new SidePanelWidget(width / 4, height);
		addWidgetWidget = new AddWidgetWidget(client, this::addWidget);
		topBarWidget = new TopBarWidget(width, this);
		addSelectableChild(addWidgetWidget);
		addSelectableChild(topBarWidget);
		addSelectableChild(sidePanelWidget);
		refreshWidgetPositions();
	}

	private void addWidget(HudWidget widget) {
		builder.addWidget(widget);
		widget.setInherited(false);
		widget.setPositionRule(
				new PositionRule(
						"screen",
						PositionRule.Point.DEFAULT,
						PositionRule.Point.DEFAULT,
						(int) (client.mouse.getScaledX(client.getWindow()) / TabHud.getScaleFactor()),
						(int) (client.mouse.getScaledY(client.getWindow()) / TabHud.getScaleFactor())
				)
		);
		builder.updateRenderedWidgets();
	}

	@Override
	protected void refreshWidgetPositions() {
		sidePanelWidget.setWidth(width / 4);
		sidePanelWidget.setHeight(height);
		if (sidePanelWidget.isOpen()) sidePanelWidget.setX(sidePanelWidget.rightSide ? width - sidePanelWidget.getWidth() : 0);
		topBarWidget.setWidth(width);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.renderBackground(context, mouseX, mouseY, deltaTicks);
		Text text = Text.literal("Left click to add and move widgets, right click to edit stuff."); // TODO translatable
		int textWidth = textRenderer.getWidth(text);
		context.drawText(textRenderer, text, (width - textWidth) / 2, (height - textRenderer.fontHeight) / 2, ColorHelper.getWhite(0.8f), false);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		Matrix3x2fStack matrices = context.getMatrices();
		float scale = TabHud.getScaleFactor();
		matrices.pushMatrix();
		matrices.scale(scale);
		builder.render(context, getScreenWidth(), getScreenHeight(), true);
		matrices.popMatrix();
		hoveredWidget = null;
		double scaledMouseX = mouseX / scale;
		double scaledMouseY = mouseY / scale;
		for (HudWidget hudWidget : builder.getWidgets()) {
			if (hudWidget.isMouseOver(scaledMouseX, scaledMouseY)) {
				hoveredWidget = hudWidget;
				break;
			}
		}

		Matrix3x2f scaleMatrix = new Matrix3x2f().scale(scale);
		if (hoveredWidget != null) {
			ScreenRect rect = hoveredWidget.getNavigationFocus().transform(scaleMatrix);
			context.drawBorder(rect.getLeft() - 1, rect.getTop() - 1, rect.width() + 2, rect.height() + 2, Colors.YELLOW);
		}
		if (selectedWidget != null) {
			ScreenRect rect = selectedWidget.getNavigationFocus().transform(scaleMatrix);
			context.drawBorder(rect.getLeft() - 1, rect.getTop() - 1, rect.width() + 2, rect.height() + 2, Colors.GREEN);
		}
		topBarWidget.visible = selectedWidget == null || selectedWidget.getY() >= 16;

		sidePanelWidget.render(context, mouseX, mouseY, deltaTicks);
		// Render on top of everything
		topBarWidget.render(context, mouseX, mouseY, deltaTicks);
		addWidgetWidget.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
		if (selectedWidget != null && dragRelative != null) {
			PositionRule oldRule = selectedWidget.getPositionRule();
			mouseX /= TabHud.getScaleFactor();
			mouseY /= TabHud.getScaleFactor();

			PositionRule.Point parentPoint;
			PositionRule.Point thisPoint;
			if (autoAnchor && "screen".equals(oldRule.parent())) {
				parentPoint = thisPoint = getPoint(selectedWidget, (int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y());
			} else {
				parentPoint = oldRule.parentPoint();
				thisPoint = oldRule.thisPoint();
			}
			String newParent = oldRule.parent();
			OptionalInt relativeX = OptionalInt.empty();
			OptionalInt relativeY = OptionalInt.empty();
			if (snapping) {
				final NavigationDirection[] directions = NavigationDirection.values();

				ScreenRect selectedRect = new ScreenRect((int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y(), selectedWidget.getScaledWidth(), selectedWidget.getScaledHeight());
				ScreenRect[] selectedSnapBoxes = Arrays.stream(directions).map(dir -> getBorder(selectedRect, dir)).toArray(ScreenRect[]::new);

				int distanceToCursor = Integer.MAX_VALUE;
				for (HudWidget widget : builder.getWidgets()) {
					if (widget == selectedWidget) continue;
					if (widget.getPositionRule().parent().equals(selectedWidget.getId())) continue;
					ScreenRect otherRect = widget.getNavigationFocus();
					for (NavigationDirection direction : directions) {
						ScreenRect otherSnapBox = getBorder(otherRect, direction);
						ScreenRect selectedSnapBox = selectedSnapBoxes[direction.getOpposite().ordinal()];

						int dist = direction.getAxis() == NavigationAxis.HORIZONTAL ? Math.abs((int) mouseX - otherSnapBox.getBorder(direction).getCenter(NavigationAxis.HORIZONTAL)) : Math.abs((int) mouseY - otherSnapBox.getBorder(direction).getCenter(NavigationAxis.VERTICAL));
						if (!selectedSnapBox.overlaps(otherSnapBox) || dist > distanceToCursor) continue;
						PositionRule.Point point = getPoint(widget);
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
						newParent = widget.getId();
						distanceToCursor = dist;
					}
				}
			}
			ScreenPos startPosition =  WidgetPositioner.getStartPosition(newParent, getScreenWidth(), getScreenHeight(), parentPoint);
			PositionRule newRule = new PositionRule(
					newParent,
					parentPoint,
					thisPoint,
					relativeX.orElse((int) mouseX - dragRelative.x() - startPosition.x() + (int) (selectedWidget.getScaledWidth() * thisPoint.horizontalPoint().getPercentage())),
					relativeY.orElse((int) mouseY - dragRelative.y() - startPosition.y() + (int) (selectedWidget.getScaledHeight() * thisPoint.verticalPoint().getPercentage()))
			);
			selectedWidget.setPositionRule(newRule);
			updateBuilderPositions();
			if (sidePanelWidget.isOpen() && new ScreenRect(sidePanelWidget.getX(), sidePanelWidget.getY(), sidePanelWidget.getWidth(), sidePanelWidget.getHeight()).overlaps(new ScreenRect(selectedWidget.getX(), selectedWidget.getY(), selectedWidget.getScaledWidth(), selectedWidget.getScaledHeight()))) {
				sidePanelWidget.close();
				openPanelAfterDragging = true;
			}
			return true;
		}
		return false;
	}

	private @NotNull PositionRule.Point getPoint(@NotNull HudWidget widget) {
		return getPoint(widget, widget.getX(), widget.getY());
	}

	private @NotNull PositionRule.Point getPoint(@NotNull HudWidget widget, int x, int y) {
		int widgetCenterX = x + widget.getScaledWidth() / 2 - getScreenWidth() / 2;
		int widgetCenterY = y + widget.getScaledHeight() / 2 - getScreenHeight() / 2;
		PositionRule.HorizontalPoint hPoint = widgetCenterX < -25 ? PositionRule.HorizontalPoint.LEFT : widgetCenterX > 25 ? PositionRule.HorizontalPoint.RIGHT : PositionRule.HorizontalPoint.CENTER;
		PositionRule.VerticalPoint vPoint = widgetCenterY < -25 ? PositionRule.VerticalPoint.TOP : widgetCenterY > 25 ? PositionRule.VerticalPoint.BOTTOM : PositionRule.VerticalPoint.CENTER;
		return new PositionRule.Point(vPoint, hPoint);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		if (selectWidgetPrompt != null) {
			if (hoveredWidget != null && !selectWidgetPrompt.allowItself() && hoveredWidget.equals(selectedWidget)) return true;
			selectWidgetPrompt.callback().accept(hoveredWidget);
			selectWidgetPrompt = null;
			sidePanelWidget.open();
			return true;
		}
		if (hoveredWidget == null) {
			if (sidePanelWidget.isOpen()) sidePanelWidget.close();
			selectedWidget = null;
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				List<HudWidget> availableWidgets = new ArrayList<>(WidgetManager.getWidgetsAvailableIn(currentLocation));
				availableWidgets.removeAll(builder.getWidgets()); // remove already present widgets
				addWidgetWidget.openWith(availableWidgets);
				addWidgetWidget.setX(Math.clamp((int) mouseX, 5, width - addWidgetWidget.getWidth() - 5));
				addWidgetWidget.setY(Math.clamp((int) mouseY, 5, height - addWidgetWidget.getHeight() - 5));
			} else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && currentScreenLayer != WidgetManager.ScreenLayer.HUD) {
				client.setScreen(new ScreenConfigPopup(this, builder, true));
			}
			return true;
		}
		if (!hoveredWidget.equals(selectedWidget)) {
			selectedWidget = hoveredWidget;
		}
		mouseX /= TabHud.getScaleFactor();
		mouseY /= TabHud.getScaleFactor();
		dragRelative = new ScreenPos((int) (mouseX - selectedWidget.getX()), (int) (mouseY - selectedWidget.getY()));
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && (!sidePanelWidget.isOpen() || !selectedWidget.equals(sidePanelWidget.getHudWidget()))) {
			openSidePanel();
		} else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && sidePanelWidget.isOpen() && !selectedWidget.equals(sidePanelWidget.getHudWidget())) {
			openSidePanel();
		}
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragRelative = null;
		if (openPanelAfterDragging) {
			openPanelAfterDragging = false;
			openSidePanel();
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (selectedWidget != null && selectedWidget == hoveredWidget) {
			boolean move = true;
			int x = 0, y = 0;
			switch (keyCode) {
				case GLFW.GLFW_KEY_LEFT -> x = -1;
				case GLFW.GLFW_KEY_RIGHT -> x = 1;
				case GLFW.GLFW_KEY_UP -> y = -1;
				case GLFW.GLFW_KEY_DOWN -> y = 1;
				default -> move = false;
			}
			if (move) {
				PositionRule oldRule = selectedWidget.getPositionRule();
				PositionRule newRule = new PositionRule(
						oldRule.parent(),
						oldRule.parentPoint(),
						oldRule.thisPoint(),
						oldRule.relativeX() + x,
						oldRule.relativeY() + y
				);
				selectedWidget.setPositionRule(newRule);
				updateBuilderPositions();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_DELETE) {
				removeWidget(selectedWidget);
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void openSidePanel() {
		if (selectedWidget == null) return;
		boolean rightSide = selectedWidget.getX() + selectedWidget.getWidth() / 2 < getScreenWidth() / 2;
		sidePanelWidget.open(selectedWidget, this, rightSide, rightSide ? width - sidePanelWidget.getWidth() : 0);
	}

	@Override
	public void tick() {
		if (selectedWidget == null && sidePanelWidget.isOpen()) sidePanelWidget.close();
		for (HudWidget widget : builder.getWidgets()) {
			if (widget.getNavigationFocus().intersects(new ScreenRect(0, 0, getScreenWidth(), getScreenHeight())) || !widget.getPositionRule().parent().equals("screen")) continue;
			widget.setPositionRule(PositionRule.DEFAULT);
			updateBuilderPositions();
		}
	}

	@Override
	public void removed() {
		builder.updateConfig();
		builder.updateWidgetsList();
		if (currentLocation == Location.UNKNOWN) {
			WidgetManager.getScreenBuilder(Utils.getLocation(), WidgetManager.ScreenLayer.HUD).updateWidgetsList();
		}
	}

	private static ScreenRect getBorder(ScreenRect rect, NavigationDirection side) {
		int extraX = rect.width() / 2;
		int extraY = rect.height() / 2;
		final int thickness = 5 + (side.getAxis() == NavigationAxis.HORIZONTAL ? extraX : extraY);
		int i = rect.getBoundingCoordinate(side);
		NavigationAxis otherAxis = side.getAxis().getOther();
		int j = rect.getBoundingCoordinate(otherAxis.getNegativeDirection());
		int k = rect.getLength(otherAxis);
		ScreenRect screenRect = ScreenRect.of(side.getAxis(), i, j, thickness, k);
		int offsetX = side.getAxis() == NavigationAxis.HORIZONTAL ? (side.isPositive() ? -extraX : -5) : 0;
		int offsetY = side.getAxis() == NavigationAxis.VERTICAL ? (side.isPositive() ? -extraY : -5) : 0;
		return new ScreenRect(screenRect.getLeft() + offsetX, screenRect.getTop() + offsetY, screenRect.width(), screenRect.height());
	}

	@Override
	public void notifyWidget() {
		if (selectedWidget != null) selectedWidget.optionsChanged();
	}

	@Override
	public void promptSelectWidget(@NotNull Consumer<@Nullable HudWidget> callback, boolean allowItself) {
		selectWidgetPrompt = new SelectWidgetPrompt(callback, allowItself);
		sidePanelWidget.close();
	}

	@Override
	public void removeWidget(@NotNull HudWidget widget) {
		builder.removeWidget(widget);
		if (selectedWidget == widget) {
			sidePanelWidget.close();
			selectedWidget = null;
		}
		builder.updateRenderedWidgets();
	}

	private void updateBuilderPositions() {
		builder.updatePositions(getScreenWidth(), getScreenHeight());
	}

	@Override
	public HudWidget getEditedWidget() {
		return selectedWidget;
	}

	@Override
	public int getScreenWidth() {
		return (int) (width / TabHud.getScaleFactor());
	}

	@Override
	public int getScreenHeight() {
		return (int) (height / TabHud.getScaleFactor());
	}

	@Override
	public void openPopup(Function<Screen, Screen> popupCreator) {
		client.setScreen(popupCreator.apply(this));
	}

	private record SelectWidgetPrompt(@NotNull Consumer<@Nullable HudWidget> callback, boolean allowItself) {}
}
