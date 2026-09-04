package de.hysky.skyblocker.skyblock.tabhud.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.EditableScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.LayerConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.PlaceholderWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.ScreenUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class WidgetsConfigurationScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static Pattern SCREEN_TITLE_PATTERN = Pattern.compile("(\\(\\d/\\d]\\) )?widgets (in|on)");

	@Init
	public static void initCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(
				ClientCommands.literal(SkyblockerMod.NAMESPACE).then(ClientCommands.literal("hud").executes(Scheduler.queueOpenScreenCommand(WidgetsConfigurationScreen::new)))
		));
	}

	private final @Nullable Screen previousScreen;

	/**
	 * The currently edited location. {@link Location#UNKNOWN} if editing the global skyblock screen.
	 */
	private Location currentLocation;
	private WidgetManager.ScreenLayer currentScreenLayer;

	protected final EditableScreenBuilder screenBuilder = new EditableScreenBuilder();
	private ScreenConfig screenConfig;
	private EditableScreenBuilder.EditableLayer layer;

	private SidePanelWidget sidePanelWidget;
	private AddWidgetWidget addWidgetWidget;
	private TopBarWidget topBarWidget;

	/// A widget that covers the entire screen, used for snapping.
	/// Be very careful when using this widget, as it contains invalid nullability.
	private final PositionedWidget screenWidget = new PositionedWidget(new HudWidget(new HudWidget.Information(null, null)) {
		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, float delta) {
			w = getScreenWidth();
			h = getScreenHeight();
		}

		@Override
		protected void extractWidgetRenderStateForConfig(GuiGraphicsExtractor graphics, float delta) {
			w = getScreenWidth();
			h = getScreenHeight();
		}
	}, PositionRule.DEFAULT);
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
		this(Utils.getLocation(), null);
	}

	public WidgetsConfigurationScreen(Location location, @Nullable Screen previousScreen) {
		super(Component.literal("Widgets Config Screen"));
		this.previousScreen = previousScreen;
		currentLocation = location;
		currentScreenLayer = WidgetManager.ScreenLayer.HUD;
		screenConfig = WidgetManager.getScreenConfig(currentLocation);
		screenBuilder.setConfig(screenConfig);
		layer = screenBuilder.getLayer(currentScreenLayer);
		layer.update();
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled) {
			screenBuilder.updateFancyTab();
		}
	}

	public void setCurrentLocation(Location newLocation) {
		layer.editor().serializeConfig();
		this.currentLocation = newLocation;
		screenConfig = WidgetManager.getScreenConfig(currentLocation);
		screenBuilder.setConfig(screenConfig);
		layer = screenBuilder.getLayer(currentScreenLayer);
		layer.update();
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled) {
			screenBuilder.updateFancyTab();
		}
	}

	public void setCurrentScreenLayer(WidgetManager.ScreenLayer newScreenLayer) {
		layer.editor().serializeConfig();
		this.currentScreenLayer = newScreenLayer;
		layer = screenBuilder.getLayer(newScreenLayer);
		layer.update();
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
		layer.editor().add(widget).rule = new PositionRule(
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
		context.text(font, text, (width - textWidth) / 2, (height - font.lineHeight) / 2, 0xFFFFFFFF);
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
		layer.builder().extractRenderStates(context, getScreenWidth(), getScreenHeight(), true);
		matrices.popMatrix();
		hoveredWidget = null;
		double scaledMouseX = mouseX / scale;
		double scaledMouseY = mouseY / scale;
		for (PositionedWidget hudWidget : layer.builder().getRendered()) {
			if (hudWidget.widget.isMouseOver(scaledMouseX, scaledMouseY)) {
				hoveredWidget = hudWidget;
				break;
			}
		}

		Matrix3x2f scaleMatrix = new Matrix3x2f().scale(scale);
		if (hoveredWidget != null && getChildAt(mouseX, mouseY).isEmpty()) {
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

		if (currentLocation != Utils.getLocation()) {
			context.centeredText(font, Component.translatable("skyblocker.config.hud.screen.locationWarning[0]"), width / 2, height - 30, 0xFFFFFFFF);
			context.centeredText(font, Component.translatable("skyblocker.config.hud.screen.locationWarning[1]"), width / 2, height - 20, 0xFFFFFFFF);
		}
		if (selectWidgetPrompt != null) {
			context.setTooltipForNextFrame(selectWidgetPrompt.tooltip(), mouseX, mouseY);
		}

		// Hack to make the screen widget cover the entire screen.
		screenWidget.widget.setPosition(0, 0);
		screenWidget.widget.extractRenderState(context, deltaTicks);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (super.mouseDragged(click, deltaX, deltaY)) return true;
		double mouseX = click.x();
		double mouseY = click.y();
		if (selectedWidget != null && !selectedWidget.fromTab && dragRelative != null) {
			PositionRule oldRule = selectedWidget.rule;
			mouseX /= TabHud.getScaleFactor();
			mouseY /= TabHud.getScaleFactor();

			SnapResult pos;
			if (autoAnchor && oldRule.parent().isEmpty()) {
				PositionRule.Point point = getPoint(selectedWidget.widget, (int) mouseX - dragRelative.x(), (int) mouseY - dragRelative.y());
				pos = new SnapResult(null, point, point, OptionalInt.empty(), OptionalInt.empty());
			} else {
				pos = new SnapResult(oldRule.parent().orElse(null), oldRule.parentPoint(), oldRule.thisPoint(), OptionalInt.empty(), OptionalInt.empty());
			}
			if (minecraft.hasShiftDown()) {
				pos = snapSelectedWidget((int) mouseX, (int) mouseY).orElse(pos);
			}
			ScreenPosition startPosition = WidgetPositioner.getStartPosition(pos.parent, getScreenWidth(), getScreenHeight(), pos.parentPoint);
			selectedWidget.rule = new PositionRule(
					Optional.ofNullable(pos.parent),
					pos.parentPoint,
					pos.thisPoint,
					pos.relativeX.orElse((int) mouseX - dragRelative.x() - startPosition.x() + (int) (selectedWidget.widget.getWidth() * pos.thisPoint.horizontalPoint().getPercentage())),
					pos.relativeY.orElse((int) mouseY - dragRelative.y() - startPosition.y() + (int) (selectedWidget.widget.getHeight() * pos.thisPoint.verticalPoint().getPercentage()))
			);
			updateBuilderPositions();
			ScreenRectangle sidePanel = new ScreenRectangle(sidePanelWidget.getX(), sidePanelWidget.getY(), sidePanelWidget.getWidth(), sidePanelWidget.getHeight());
			ScreenRectangle selected = new ScreenRectangle(selectedWidget.widget.getX(), selectedWidget.widget.getY(), selectedWidget.widget.getWidth(), selectedWidget.widget.getHeight());
			if (sidePanelWidget.isOpen() && sidePanel.overlaps(selected)) {
				sidePanelWidget.close();
				openSidePanel();
				openPanelAfterDragging = true;
			}
			return true;
		}
		return false;
	}

	private Optional<SnapResult> snapSelectedWidget(int mouseX, int mouseY) {
		if (selectedWidget == null || dragRelative == null) {
			return Optional.empty();
		}

		// The integer is a Manhattan distance based "snap score", used to find the best snap candidate.
		ObjectIntPair<@Nullable SnapResult> result = ObjectIntPair.of(null, Integer.MAX_VALUE);

		ScreenRectangle selectedRect = new ScreenRectangle(mouseX - dragRelative.x(), mouseY - dragRelative.y(), selectedWidget.widget.getWidth(), selectedWidget.widget.getHeight());
		ScreenRectangle[] selectedSnapBoxes = Arrays.stream(ScreenDirection.values()).map(dir -> ScreenUtils.getSnapBox(selectedRect, dir)).toArray(ScreenRectangle[]::new);

		// Check if the selected widget should snap to the edge of the screen.
		// See screenWidget and the hack in extractRenderState.
		result = snapSelectedToWidget(selectedWidget, screenWidget, selectedSnapBoxes, selectedRect, result.rightInt()).orElse(result);
		// Check if the selected widget should snap to this widget. Closer widgets take priority based on snapScore.
		for (PositionedWidget otherWidget : layer.builder().getRendered()) {
			result = snapSelectedToWidget(selectedWidget, otherWidget, selectedSnapBoxes, selectedRect, result.rightInt()).orElse(result);
		}
		return Optional.ofNullable(result.left());
	}

	/// Returns a [SnapResult] and a snap score if the selected widget should snap to the other widget, or empty if it shouldn't snap.
	private Optional<ObjectIntPair<@Nullable SnapResult>> snapSelectedToWidget(PositionedWidget selectedWidget, PositionedWidget otherWidget, ScreenRectangle[] selectedSnapBoxes, ScreenRectangle selectedRect, int snapScore) {
		if (otherWidget == selectedWidget) return Optional.empty();
		if (selectedWidget.widget.getInternalID().equals(otherWidget.rule.parent().orElse(null))) return Optional.empty();

		ScreenRectangle otherRect = otherWidget.widget.getRectangle();
		PositionRule.Point point = getPoint(otherWidget.widget);

		int distX = 10;
		int distY = 10;

		PositionRule.HorizontalPoint parentPointH = null, thisPointH = null;
		PositionRule.VerticalPoint parentPointV = null, thisPointV = null;
		OptionalInt relativeX = OptionalInt.empty();
		OptionalInt relativeY = OptionalInt.empty();

		// Docking
		for (ScreenDirection otherEdge : ScreenDirection.values()) {
			// When docking, we snap opposite edges together.
			ScreenDirection selectEdge = otherEdge.getOpposite();
			ScreenRectangle otherSnapBox = ScreenUtils.getSnapBox(otherRect, otherEdge);
			ScreenRectangle selectedSnapBox = selectedSnapBoxes[selectEdge.ordinal()];

			if (!selectedSnapBox.overlaps(otherSnapBox)) continue;

			// Distance between the edges being snapped, in the axis orthogonal to the direction of the edge.
			int dist = Math.abs(selectedRect.getBoundInDirection(selectEdge) - otherRect.getBoundInDirection(otherEdge));

			if (otherEdge.getAxis() == ScreenAxis.HORIZONTAL && dist < distX) {
				// Dock the selectedWidget's selectEdge to otherWidget's otherEdge.
				distX = dist;
				relativeX = OptionalInt.of(otherEdge.isPositive() ? 1 : -2);
				parentPointH = otherEdge.isPositive() ? PositionRule.HorizontalPoint.RIGHT : PositionRule.HorizontalPoint.LEFT;
				thisPointH = otherEdge.isPositive() ? PositionRule.HorizontalPoint.LEFT : PositionRule.HorizontalPoint.RIGHT;
			} else if (otherEdge.getAxis() == ScreenAxis.VERTICAL && dist < distY) {
				// Dock the selectedWidget's selectEdge to otherWidget's otherEdge.
				distY = dist;
				relativeY = OptionalInt.of(otherEdge.isPositive() ? 1 : -2);
				parentPointV = otherEdge.isPositive() ? PositionRule.VerticalPoint.BOTTOM : PositionRule.VerticalPoint.TOP;
				thisPointV = otherEdge.isPositive() ? PositionRule.VerticalPoint.TOP : PositionRule.VerticalPoint.BOTTOM;
			}
		}

		// Alignment
		for (ScreenDirection edge : ScreenDirection.values()) {
			// When aligning, we snap the same edges together.
			ScreenRectangle otherSnapBox = ScreenUtils.getSnapBox(otherRect, edge);
			ScreenRectangle selectedSnapBox = selectedSnapBoxes[edge.ordinal()];

			if (!selectedSnapBox.overlaps(otherSnapBox)) continue;

			// Distance between the edges being aligned, in the axis orthogonal to the direction of the edge.
			int dist = Math.abs(selectedRect.getBoundInDirection(edge) - otherRect.getBoundInDirection(edge));

			if (edge.getAxis() == ScreenAxis.HORIZONTAL && dist < distX) {
				// Align the selectedWidget's edge to otherWidget's edge.
				distX = dist;
				relativeX = OptionalInt.of(0);
				parentPointH = thisPointH = edge.isPositive() ? PositionRule.HorizontalPoint.RIGHT : PositionRule.HorizontalPoint.LEFT;
			} else if (edge.getAxis() == ScreenAxis.VERTICAL && dist < distY) {
				// Align the selectedWidget's edge to otherWidget's edge.
				distY = dist;
				relativeY = OptionalInt.of(0);
				parentPointV = thisPointV = edge.isPositive() ? PositionRule.VerticalPoint.BOTTOM : PositionRule.VerticalPoint.TOP;
			}
		}

		// If no docking or alignment was found, skip this widget.
		if (parentPointH == null && parentPointV == null) return Optional.empty();

		// If this widget has a worse snap score than the best one found so far, skip it.
		int dist = distX + distY;
		if (dist >= snapScore) return Optional.empty();

		PositionRule.Point parentPoint = new PositionRule.Point(
				parentPointV != null ? parentPointV : point.verticalPoint(),
				parentPointH != null ? parentPointH : point.horizontalPoint()
		);
		PositionRule.Point thisPoint = new PositionRule.Point(
				thisPointV != null ? thisPointV : point.verticalPoint(),
				thisPointH != null ? thisPointH : point.horizontalPoint()
		);
		return Optional.of(ObjectIntPair.of(new SnapResult(otherWidget.widget.getInternalID(), parentPoint, thisPoint, relativeX, relativeY), dist));
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
			if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
				List<HudWidget> availableWidgets = new ArrayList<>(WidgetManager.getWidgetsAvailableIn(currentLocation));
				// remove already present widgets except those from the tab hud
				availableWidgets.removeAll(layer.builder().getRendered().stream().filter(w -> !w.fromTab).map(w -> w.widget).toList());
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
		if (!selectedWidget.fromTab) dragRelative = new ScreenPosition((int) (mouseX - selectedWidget.widget.getX()), (int) (mouseY - selectedWidget.widget.getY()));
		if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT && (!sidePanelWidget.isOpen() || !selectedWidget.equals(sidePanelWidget.getPositionedWidget()))) {
			openSidePanel();
		} else if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && sidePanelWidget.isOpen() && !selectedWidget.equals(sidePanelWidget.getPositionedWidget())) {
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
		if (selectedWidget != null) {
			if (selectedWidget == hoveredWidget) {
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
			}
			if (keyInput.key() == InputConstants.KEY_DELETE) {
				removeWidget(selectedWidget);
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
		for (PositionedWidget widget : layer.builder().getRendered()) {
			if (widget.widget.getRectangle().intersects(new ScreenRectangle(0, 0, getScreenWidth(), getScreenHeight())) || widget.rule.parent().isPresent()) continue;
			widget.rule = PositionRule.DEFAULT;
			updateBuilderPositions();
		}
	}

	public void promptSelectWidget(Consumer<@Nullable HudWidget> callback, boolean allowItself, Component tooltip) {
		selectWidgetPrompt = new SelectWidgetPrompt(callback, allowItself, tooltip);
		sidePanelWidget.close();
	}

	@Override
	public void onClose() {
		layer.editor().serializeConfig();
		WidgetManager.SCREEN_BUILDER.hud().update();
		this.minecraft.gui.setScreen(previousScreen);
	}

	public void removeWidget(PositionedWidget widget) {
		layer.editor().remove(widget);
		PositionRule deleted = widget.rule;
		for (PositionedWidget positionedWidget : layer.builder().getRendered()) {
			PositionRule rule = positionedWidget.rule;
			if (rule.parent().isEmpty()) continue;
			if (rule.parent().get().equals(widget.widget.getInternalID())) {
				positionedWidget.rule = new PositionRule(
						deleted.parent(),
						deleted.parentPoint(),
						rule.thisPoint(),
						deleted.relativeX() + rule.relativeX(),
						deleted.relativeY() + rule.relativeY()
				);
			}
		}
		Location location = getCurrentLocation();
		WidgetManager.getCopyTracker()
				.get(currentScreenLayer)
				.get(widget.widget.getInternalID())
				.ifPresent(copyTracker -> copyTracker.whereHas(location).ifPresent(set -> openPopup(screen ->
						new PopupScreen.Builder(screen, Component.translatable("skyblocker.config.hud.copy.delete"))
								.addMessage(Component.translatable("skyblocker.config.hud.copy.delete.description"))
								.addButton(CommonComponents.GUI_YES, popup -> {
									removeCopies(set, widget.widget.getInternalID());
									copyTracker.removeAll(set);
									popup.onClose();
								})
								.addButton(CommonComponents.GUI_NO, popup -> {
									copyTracker.remove(location);
									popup.onClose();
								})
								.build()
				)));
		if (selectedWidget == widget) {
			sidePanelWidget.close();
			selectedWidget = null;
		}
	}

	private void removeCopies(Set<Location> locations, String widgetId) {
		for (Location location : locations) {
			LayerConfig config = WidgetManager.getScreenConfig(location).get(currentScreenLayer);
			WidgetConfig deletedConfig = config.widgets().remove(widgetId);
			if (deletedConfig == null) continue;
			// fix up widgets that had the deleted one as parent
			for (Map.Entry<String, WidgetConfig> entry : config.widgets().entrySet()) {
				WidgetConfig widgetConfig = entry.getValue();
				Optional<PositionRule> posOpt = widgetConfig.position();
				if (posOpt.isEmpty()) continue;
				PositionRule rule = posOpt.get();
				if (rule.parent().filter(widgetId::equals).isEmpty()) continue;
				PositionRule newRule;
				if (deletedConfig.position().isPresent()) {
					PositionRule oldPosition = deletedConfig.position().get();
					newRule = new PositionRule(
							oldPosition.parent(),
							oldPosition.parentPoint(),
							oldPosition.thisPoint(),
							oldPosition.relativeX() + rule.relativeX(),
							oldPosition.relativeY() + rule.relativeY()
					);
				} else {
					newRule = PositionRule.DEFAULT;
				}
				entry.setValue(new WidgetConfig(widgetConfig.config(), Optional.of(newRule)));
			}
		}
	}

	private void updateBuilderPositions() {
		layer.builder().updatePositions(getScreenWidth(), getScreenHeight());
	}

	public HudWidget getEditedWidget() {
		if (selectedWidget == null) {
			LOGGER.warn("Trying to edit selected widget but nothing is selected?", new Throwable());
			return new PlaceholderWidget(""); // this shouldn't cause issues
		}
		return selectedWidget.widget;
	}

	public WidgetManager.ScreenLayer getCurrentScreenLayer() {
		return currentScreenLayer;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public ScreenConfig getScreenConfig() {
		return screenConfig;
	}

	public int getScreenWidth() {
		return (int) (width / TabHud.getScaleFactor());
	}

	public int getScreenHeight() {
		return (int) (height / TabHud.getScaleFactor());
	}

	public void openPopup(Function<WidgetsConfigurationScreen, Screen> popupCreator) {
		minecraft.gui.setScreen(popupCreator.apply(this));
	}

	private record SelectWidgetPrompt(Consumer<@Nullable HudWidget> callback, boolean allowItself, Component tooltip) {}

	private record SnapResult(@Nullable String parent, PositionRule.Point parentPoint, PositionRule.Point thisPoint, OptionalInt relativeX, OptionalInt relativeY) {}
}
