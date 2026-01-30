package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import org.joml.Vector2i;

public class TopAlignedWidgetPositioner extends WidgetPositioner {

	private static final int START_Y = 0;

	private int totalWidth = 0;
	private int totalHeight = 0;

	private int currentWidth = 0;
	private int currentY = START_Y;

	public TopAlignedWidgetPositioner(float maxHeight, int screenHeight) {
		super(maxHeight, screenHeight);
	}

	@Override
	public void positionWidget(HudWidget hudWidget) {
		if (currentY + hudWidget.getHeight() > screenHeight * maxHeight) {
			totalHeight = Math.max(totalHeight, currentY);
			totalWidth += currentWidth + ScreenConst.WIDGET_PAD;
			currentY = START_Y;
			currentWidth = 0;
		}

		hudWidget.setPosition(totalWidth, currentY);
		currentY += hudWidget.getHeight() + ScreenConst.WIDGET_PAD;
		currentWidth = Math.max(currentWidth, hudWidget.getWidth());
	}

	@Override
	public Vector2i finalizePositioning() {
		totalHeight = Math.max(totalHeight, currentY);
		totalWidth += currentWidth + ScreenConst.WIDGET_PAD;
		return new Vector2i(totalWidth, totalHeight);
	}
}
