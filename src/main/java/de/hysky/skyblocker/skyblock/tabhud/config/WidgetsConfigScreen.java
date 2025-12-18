package de.hysky.skyblocker.skyblock.tabhud.config;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;

public class WidgetsConfigScreen extends Screen implements WidgetConfig {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("hud").executes(Scheduler.queueOpenScreenCommand(WidgetsConfigScreen::new)))
		));
	}

	/**
	 * The currently edited location. {@link Location#UNKNOWN} if editing the global skyblock screen.
	 */
	private Location currentLocation;
	private WidgetManager.ScreenLayer currentScreenLayer;

	private ScreenBuilder builder;

	private SidePanelWidget sidePanelWidget;
	private AddWidgetWidget addWidgetWidget;
	private TopBarWidget topBarWidget;

	private @Nullable HudWidget hoveredWidget;
	private @Nullable HudWidget selectedWidget;
	/**
	 * Where the user started dragging relative to the dragged widget's position. Null if not dragging.
	 */
	private @Nullable ScreenPosition dragRelative = null;
	private boolean openPanelAfterDragging = false;
	boolean autoAnchor = true;

	private @Nullable SelectWidgetPrompt selectWidgetPrompt = null;

	public WidgetsConfigScreen() {
		super(Component.literal("Widgets Config Screen"));
		currentLocation = Utils.getLocation();
		currentScreenLayer = WidgetManager.ScreenLayer.HUD;
		builder = WidgetManager.getScreenBuilder(currentLocation, currentScreenLayer);
		builder.updateWidgetsList();
		builder.updateTabWidgetsList();
	}

	public void setCurrentLocation(Location newLocation) {
		builder.updateConfig();
		this.currentLocation = newLocation;
		builder = WidgetManager.getScreenBuilder(newLocation, currentScreenLayer);
		builder.updateWidgetsList();
		builder.updateTabWidgetsList();
	}

	public void setCurrentScreenLayer(WidgetManager.ScreenLayer newScreenLayer) {
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
		addWidgetWidget = new AddWidgetWidget(minecraft, this::addWidget);
		topBarWidget = new TopBarWidget(width, this);
		addWidget(addWidgetWidget);
		addWidget(topBarWidget);
		addWidget(sidePanelWidget);
		repositionElements();
	}

	private void addWidget(HudWidget widget) {
		builder.addWidget(widget);
		widget.renderingInformation.inherited = false;
		widget.setPositionRule(
				new PositionRule(
						"screen",
						PositionRule.Point.DEFAULT,
						PositionRule.Point.DEFAULT,
						(int) (minecraft.mouseHandler.getScaledXPos(minecraft.getWindow()) / TabHud.getScaleFactor()),
						(int) (minecraft.mouseHandler.getScaledYPos(minecraft.getWindow()) / TabHud.getScaleFactor())
				)
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
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.renderBackground(context, mouseX, mouseY, deltaTicks);
		Component text = Component.literal("Right click to add widgets and edit things."); // TODO translatable
		int textWidth = font.width(text);
		context.drawString(font, text, (width - textWidth) / 2, (height - font.lineHeight) / 2, ARGB.white(0.8f), false);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		Matrix3x2fStack matrices = context.pose();
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
			ScreenRectangle rect = hoveredWidget.getRectangle().transformAxisAligned(scaleMatrix);
			HudHelper.drawBorder(context, rect.left() - 1, rect.top() - 1, rect.width() + 2, rect.height() + 2, CommonColors.YELLOW);
		}
		if (selectedWidget != null) {
			ScreenRectangle rect = selectedWidget.getRectangle().transformAxisAligned(scaleMatrix);
			HudHelper.drawBorder(context, rect.left() - 1, rect.top() - 1, rect.width() + 2, rect.height() + 2, CommonColors.GREEN);
		}
		topBarWidget.visible = selectedWidget == null || selectedWidget.getY() >= 16;

		sidePanelWidget.render(context, mouseX, mouseY, deltaTicks);
		// Render on top of everything
		topBarWidget.render(context, mouseX, mouseY, deltaTicks);
		addWidgetWidget.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (super.mouseDragged(click, deltaX, deltaY)) return true;
		double mouseX = click.x();
		double mouseY = click.y();
		if (selectedWidget != null && dragRelative != null) {
			PositionRule oldRule = selectedWidget.getPositionRule();
			mouseX /= TabHud.getScaleFactor();
			mouseY /= TabHud.getScaleFactor();

			PositionRule.Point parentPoint;
			PositionRule.Point thisPoint;
			if (autoAnchor && oldRule.parent().isEmpty()) {
				parentPoint = thisPoint = getPoint(selectedWidget, (int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y());
			} else {
				parentPoint = oldRule.parentPoint();
				thisPoint = oldRule.thisPoint();
			}
			String newParent = null;
			OptionalInt relativeX = OptionalInt.empty();
			OptionalInt relativeY = OptionalInt.empty();
			if (minecraft.hasShiftDown()) {
				final ScreenDirection[] directions = ScreenDirection.values();

				ScreenRectangle selectedRect = new ScreenRectangle((int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y(), selectedWidget.getScaledWidth(), selectedWidget.getScaledHeight());
				ScreenRectangle[] selectedSnapBoxes = Arrays.stream(directions).map(dir -> getBorder(selectedRect, dir)).toArray(ScreenRectangle[]::new);

				int distanceToCursor = Integer.MAX_VALUE;
				for (HudWidget widget : builder.getWidgets()) {
					if (widget == selectedWidget) continue;
					if (selectedWidget.getId().equals(widget.getPositionRule().parent().orElse(null))) continue;
					ScreenRectangle otherRect = widget.getRectangle();
					for (ScreenDirection direction : directions) {
						ScreenRectangle otherSnapBox = getBorder(otherRect, direction);
						ScreenRectangle selectedSnapBox = selectedSnapBoxes[direction.getOpposite().ordinal()];

						int dist = direction.getAxis() == ScreenAxis.HORIZONTAL ? Math.abs((int) mouseX - otherSnapBox.getBorder(direction).getCenterInAxis(ScreenAxis.HORIZONTAL)) : Math.abs((int) mouseY - otherSnapBox.getBorder(direction).getCenterInAxis(ScreenAxis.VERTICAL));
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
			ScreenPosition startPosition = WidgetPositioner.getStartPosition(newParent, getScreenWidth(), getScreenHeight(), parentPoint);
			PositionRule newRule = new PositionRule(
					Optional.ofNullable(newParent),
					parentPoint,
					thisPoint,
					relativeX.orElse((int) mouseX - dragRelative.x() - startPosition.x() + (int) (selectedWidget.getScaledWidth() * thisPoint.horizontalPoint().getPercentage())),
					relativeY.orElse((int) mouseY - dragRelative.y() - startPosition.y() + (int) (selectedWidget.getScaledHeight() * thisPoint.verticalPoint().getPercentage()))
			);
			selectedWidget.setPositionRule(newRule);
			updateBuilderPositions();
			if (sidePanelWidget.isOpen() && new ScreenRectangle(sidePanelWidget.getX(), sidePanelWidget.getY(), sidePanelWidget.getWidth(), sidePanelWidget.getHeight()).overlaps(new ScreenRectangle(selectedWidget.getX(), selectedWidget.getY(), selectedWidget.getScaledWidth(), selectedWidget.getScaledHeight()))) {
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
		int widgetCenterX = x + widget.getScaledWidth() / 2 - getScreenWidth() / 2;
		int widgetCenterY = y + widget.getScaledHeight() / 2 - getScreenHeight() / 2;
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
			selectWidgetPrompt.callback().accept(hoveredWidget);
			selectWidgetPrompt = null;
			sidePanelWidget.open();
			return true;
		}
		if (hoveredWidget == null) {
			if (sidePanelWidget.isOpen()) sidePanelWidget.close();
			selectedWidget = null;
			if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				if (click.hasShiftDown() && currentScreenLayer != WidgetManager.ScreenLayer.HUD) {
					minecraft.setScreen(new ScreenConfigPopup(this, builder, true));
				} else {
					List<HudWidget> availableWidgets = new ArrayList<>(WidgetManager.getWidgetsAvailableIn(currentLocation));
					availableWidgets.removeAll(builder.getWidgets()); // remove already present widgets
					addWidgetWidget.openWith(availableWidgets);
					addWidgetWidget.setX(Math.clamp((int) mouseX, 5, width - addWidgetWidget.getWidth() - 5));
					addWidgetWidget.setY(Math.clamp((int) mouseY, 5, height - addWidgetWidget.getHeight() - 5));
					addWidgetWidget.refreshScrollAmount(); // refreshes the positions of the entries
				}
			}
			return true;
		}
		if (!hoveredWidget.equals(selectedWidget)) {
			selectedWidget = hoveredWidget;
		}
		mouseX /= TabHud.getScaleFactor();
		mouseY /= TabHud.getScaleFactor();
		dragRelative = new ScreenPosition((int) (mouseX - selectedWidget.getX()), (int) (mouseY - selectedWidget.getY()));
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
			if (keyInput.key() == GLFW.GLFW_KEY_DELETE) {
				removeWidget(selectedWidget);
				return true;
			}
		}
		return super.keyPressed(keyInput);
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
			if (widget.getRectangle().intersects(new ScreenRectangle(0, 0, getScreenWidth(), getScreenHeight())) || widget.getPositionRule().parent().isPresent()) continue;
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

	@Override
	public void notifyWidget() {
		if (selectedWidget != null) selectedWidget.optionsChanged();
	}

	@Override
	public void promptSelectWidget(Consumer<@Nullable HudWidget> callback, boolean allowItself) {
		selectWidgetPrompt = new SelectWidgetPrompt(callback, allowItself);
		sidePanelWidget.close();
	}

	@Override
	public void removeWidget(HudWidget widget) {
		builder.removeWidget(widget);
		PositionRule deleted = widget.getPositionRule();
		for (HudWidget hudWidget : builder.getWidgets()) {
			PositionRule rule = hudWidget.getPositionRule();
			if (rule.parent().isEmpty()) continue;
			if (rule.parent().get().equals(widget.getId())) {
				hudWidget.setPositionRule(new PositionRule(
						deleted.parent(),
						deleted.parentPoint(),
						rule.thisPoint(),
						deleted.relativeX() + rule.relativeX(),
						deleted.relativeY() + rule.relativeY()
				));
			}
		}
		if (selectedWidget == widget) {
			sidePanelWidget.close();
			selectedWidget = null;
		}
	}

	private void updateBuilderPositions() {
		builder.updatePositions(getScreenWidth(), getScreenHeight());
	}

	@Override
	public HudWidget getEditedWidget() {
		if (selectedWidget == null) {
			LOGGER.warn("Trying to edit selected widget but nothing is selected?", new Throwable());
			return new PlaceholderWidget("unknown"); // this shouldn't cause issues
		}
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
		minecraft.setScreen(popupCreator.apply(this));
	}

	private record SelectWidgetPrompt(Consumer<@Nullable HudWidget> callback, boolean allowItself) {}
}
