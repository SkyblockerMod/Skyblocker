package de.hysky.skyblocker.skyblock.tabhud.config.preview;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.InGameHudInvoker;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The preview widget that captures clicks and displays the current state of the widgets.
 */
public class PreviewWidget extends ClickableWidget {
	private final PreviewTab tab;
	float ratio = 1f;
	private float scaledRatio = 1f;
	private float scaledScreenWidth;
	private float scaledScreenHeight;
	/**
	 * The widget the user is hovering with the mouse
	 */
	private @Nullable HudWidget hoveredWidget = null;
	/**
	 * The selected widget, settings for it show on the right, and it can be dragged around
	 */
	@Nullable HudWidget selectedWidget = null;
	/**
	 * The original pos, of the selected widget, it is set when you click on it. So when it's done being dragged it can be compared.
	 * Effectively, if this ain't null, the user is dragging a widget around.
	 */
	private @Nullable ScreenPos selectedOriginalPos = null;
	protected boolean pickParent = false;

	public PreviewWidget(PreviewTab tab) {
		super(0, 0, 0, 0, Text.literal("Preview widget"));
		this.tab = tab;
		scaledScreenWidth = tab.parent.width;
		scaledScreenHeight = tab.parent.height;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		hoveredWidget = null;
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
		scaledRatio = ratio * scale;
		scaledScreenWidth = tab.parent.width / scale;
		scaledScreenHeight = tab.parent.height / scale;

		ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(tab.getCurrentLocation());
		context.drawBorder(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, -1);
		context.enableScissor(getX(), getY(), getRight(), getBottom());
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(scaledRatio, scaledRatio);

		screenBuilder.renderWidgets(context, tab.getCurrentScreenLayer());

		float localMouseX = (mouseX - getX()) / scaledRatio;
		float localMouseY = (mouseY - getY()) / scaledRatio;

		if (selectedWidget != null && selectedWidget.isMouseOver(localMouseX, localMouseY)) {
			hoveredWidget = selectedWidget;
		} else for (HudWidget hudWidget : screenBuilder.getHudWidgets(tab.getCurrentScreenLayer())) {
			if (hudWidget.isMouseOver(localMouseX, localMouseY)) {
				hoveredWidget = hudWidget;
				break;
			}
		}

		// HOVERED
		if (hoveredWidget != null && !hoveredWidget.equals(selectedWidget)) {
			context.drawBorder(
					hoveredWidget.getX() - 1,
					hoveredWidget.getY() - 1,
					hoveredWidget.getWidth() + 2,
					hoveredWidget.getHeight() + 2,
					Colors.LIGHT_YELLOW);
		}

		// SELECTED
		if (selectedWidget != null) {
			//noinspection DataFlowIssue
			context.drawBorder(
					selectedWidget.getX() - 1,
					selectedWidget.getY() - 1,
					selectedWidget.getWidth() + 2,
					selectedWidget.getHeight() + 2,
					Formatting.GREEN.getColorValue() | 0xFF000000);

			PositionRule rule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
			if (rule != null) {
				// This is the difference between the position before dragging and the current position
				int deltaX = 0;
				int deltaY = 0;
				if (selectedOriginalPos != null) {
					deltaX = selectedWidget.getX() - selectedOriginalPos.x();
					deltaY = selectedWidget.getY() - selectedOriginalPos.y();
				}
				int thisAnchorX = (int) (selectedWidget.getX() + rule.thisPoint().horizontalPoint().getPercentage() * selectedWidget.getWidth());
				int thisAnchorY = (int) (selectedWidget.getY() + rule.thisPoint().verticalPoint().getPercentage() * selectedWidget.getHeight());

				int translatedX = Math.min(thisAnchorX - rule.relativeX() - deltaX, (int) scaledScreenWidth - 2);
				int translatedY = Math.min(thisAnchorY - rule.relativeY() - deltaY, (int) scaledScreenHeight - 2);

				renderUnits(context, rule, deltaX, deltaY, thisAnchorX, thisAnchorY, translatedX, translatedY);

				context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY + 1, 0xAAAA0000);
				context.drawVerticalLine(translatedX + 1, translatedY, thisAnchorY, 0xAAAA0000);


				context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY, Colors.RED);
				context.drawVerticalLine(translatedX, translatedY, thisAnchorY, Colors.RED);
			}
		}

		matrices.popMatrix();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(ratio, ratio);
		((InGameHudInvoker) MinecraftClient.getInstance().inGameHud).skyblocker$renderSidebar(context, tab.placeHolderObjective);
		matrices.popMatrix();
		context.disableScissor();
	}

	private void renderUnits(DrawContext context, PositionRule rule, int deltaX, int deltaY, int thisAnchorX, int thisAnchorY, int translatedX, int translatedY) {
		boolean xUnitOnTop = rule.relativeY() > 0;
		if (xUnitOnTop && thisAnchorY < 10) xUnitOnTop = false;
		if (!xUnitOnTop && thisAnchorY > scaledScreenHeight - 10) xUnitOnTop = true;

		int xUnit = rule.relativeX() + deltaX;
		int yUnit = rule.relativeY() + deltaY;
		String xUnitText = String.valueOf(xUnit);
		String yUnitText = String.valueOf(yUnit);
		int yUnitTextWidth = tab.client.textRenderer.getWidth(yUnitText);
		boolean yUnitOnRight = rule.relativeX() > 0;
		if (yUnitOnRight && translatedX + 2 + yUnitTextWidth >= scaledScreenWidth) yUnitOnRight = false;
		if (!yUnitOnRight && translatedX - 2 - yUnitTextWidth <= 0) yUnitOnRight = true;

		if (Math.abs(xUnit) < 15 || Math.abs(yUnit) < 15) {
			String text = "x: " + xUnitText + " y: " + yUnitText;
			int textX = thisAnchorX < scaledScreenWidth / 2 ? (int) (scaledScreenWidth - tab.client.textRenderer.getWidth(text) - 5) : 5;
			context.drawTextWithShadow(tab.client.textRenderer, text, textX, 2, Colors.RED);
		}
		// X
		context.drawCenteredTextWithShadow(tab.client.textRenderer, xUnitText, thisAnchorX - (xUnit) / 2, xUnitOnTop ? thisAnchorY - 9 : thisAnchorY + 2, Colors.LIGHT_RED);
		// Y
		context.drawText(tab.client.textRenderer, yUnitText, yUnitOnRight ? translatedX + 2 : translatedX - 1 - yUnitTextWidth, thisAnchorY - (yUnit - 9) / 2, Colors.LIGHT_RED, true);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	private double bufferedDeltaX = 0;
	private double bufferedDeltaY = 0;

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		double localDeltaX = deltaX / scaledRatio + bufferedDeltaX;
		double localDeltaY = deltaY / scaledRatio + bufferedDeltaY;

		bufferedDeltaX = localDeltaX - (int) localDeltaX;
		bufferedDeltaY = localDeltaY - (int) localDeltaY;

		if (selectedWidget != null && selectedOriginalPos != null) {
			selectedWidget.setX(selectedWidget.getX() + (int) localDeltaX);
			selectedWidget.setY(selectedWidget.getY() + (int) localDeltaY);
		}
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (pickParent) {
			pickParent = false;
			return;
		}
		if (!Objects.equals(hoveredWidget, selectedWidget)) {
			tab.onHudWidgetSelected(hoveredWidget);
		}
		// TODO releasing a widget outside of the area causes weird behavior, might wanna look into that
		// Update positioning real
		if (selectedWidget != null && selectedOriginalPos != null) {
			ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(tab.getCurrentLocation());
			PositionRule oldRule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
			if (oldRule == null) oldRule = PositionRule.DEFAULT;
			int relativeX = selectedWidget.getX() - selectedOriginalPos.x();
			int relativeY = selectedWidget.getY() - selectedOriginalPos.y();
			screenBuilder.setPositionRule(selectedWidget.getInternalID(), new PositionRule(
					oldRule.parent(),
					oldRule.parentPoint(),
					oldRule.thisPoint(),
					oldRule.relativeX() + relativeX,
					oldRule.relativeY() + relativeY,
					oldRule.screenLayer()));
			tab.updateWidgets();
		}

		selectedWidget = hoveredWidget;
		selectedOriginalPos = null;
		super.onRelease(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!(this.active && this.visible && isMouseOver(mouseX, mouseY))) return false;
		double localMouseX = (mouseX - getX()) / scaledRatio;
		double localMouseY = (mouseY - getY()) / scaledRatio;
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			List<HudWidget> hoveredThingies = new ArrayList<>();
			for (HudWidget hudWidget : WidgetManager.getScreenBuilder(tab.getCurrentLocation()).getHudWidgets(tab.getCurrentScreenLayer())) {
				if (hudWidget.isMouseOver(localMouseX, localMouseY)) hoveredThingies.add(hudWidget);
			}
			if (hoveredThingies.size() == 1) selectedWidget = hoveredThingies.getFirst();
			else if (!hoveredThingies.isEmpty()) {
				for (int i = 0; i < hoveredThingies.size(); i++) {
					if (hoveredThingies.get(i).equals(hoveredWidget)) {
						selectedWidget = hoveredThingies.get((i + 1) % hoveredThingies.size());
					}
				}
			}
			return true;
		}
		ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(tab.getCurrentLocation());
		if (pickParent && selectedWidget != null && !selectedWidget.equals(hoveredWidget)) {
			PositionRule oldRule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
			if (oldRule == null) oldRule = PositionRule.DEFAULT;

			int thisAnchorX = (int) (selectedWidget.getX() + oldRule.thisPoint().horizontalPoint().getPercentage() * selectedWidget.getWidth());
			int thisAnchorY = (int) (selectedWidget.getY() + oldRule.thisPoint().verticalPoint().getPercentage() * selectedWidget.getHeight());

			int otherAnchorX = hoveredWidget == null ? 0 : hoveredWidget.getX();
			int otherAnchorY = hoveredWidget == null ? 0 : hoveredWidget.getY();

			PositionRule newRule = new PositionRule(
					hoveredWidget == null ? "screen" : hoveredWidget.getInternalID(),
					PositionRule.Point.DEFAULT,
					oldRule.thisPoint(),
					thisAnchorX - otherAnchorX,
					thisAnchorY - otherAnchorY,
					oldRule.screenLayer()
			);
			screenBuilder.setPositionRule(selectedWidget.getInternalID(), newRule);
			tab.updateWidgets();
			tab.onHudWidgetSelected(selectedWidget);
			return true;
		}


		if (selectedWidget != null && selectedWidget.isMouseOver(localMouseX, localMouseY) &&
				screenBuilder.getPositionRule(selectedWidget.getInternalID()) != null) {
			selectedOriginalPos = new ScreenPos(selectedWidget.getX(), selectedWidget.getY());
		}
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (hoveredWidget != null && hoveredWidget.equals(selectedWidget)) {
			int multiplier = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 ? 5 : 1;
			int x = 0, y = 0;
			switch (keyCode) {
				case GLFW.GLFW_KEY_UP -> y = -multiplier;
				case GLFW.GLFW_KEY_DOWN -> y = multiplier;
				case GLFW.GLFW_KEY_LEFT -> x = -multiplier;
				case GLFW.GLFW_KEY_RIGHT -> x = multiplier;
			}
			ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(tab.getCurrentLocation());
			PositionRule oldRule = screenBuilder.getPositionRuleOrDefault(selectedWidget.getInternalID());

			screenBuilder.setPositionRule(selectedWidget.getInternalID(), new PositionRule(
					oldRule.parent(),
					oldRule.parentPoint(),
					oldRule.thisPoint(),
					oldRule.relativeX() + x,
					oldRule.relativeY() + y,
					oldRule.screenLayer()));
			tab.updateWidgets();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
