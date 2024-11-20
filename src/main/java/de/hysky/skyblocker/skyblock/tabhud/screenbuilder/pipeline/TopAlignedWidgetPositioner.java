package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

import java.util.ArrayList;
import java.util.List;

public class TopAlignedWidgetPositioner extends WidgetPositioner {

	private static final int START_Y = 20;

	private int totalWidth = 0;

	private int currentWidth = 0;
	private int currentY = START_Y;

	private final List<HudWidget> widgets = new ArrayList<>();

	public TopAlignedWidgetPositioner(int screenWidth, int screenHeight) {
		super(screenWidth, screenHeight);
	}

	@Override
	public void positionWidget(HudWidget hudWidget) {
		widgets.add(hudWidget);

		if (currentY + hudWidget.getHeight() > screenHeight * 0.75f) {
			totalWidth += currentWidth + ScreenConst.WIDGET_PAD;
			currentY = START_Y;
			currentWidth = 0;
		}

		hudWidget.setPosition(totalWidth, currentY);
		currentY += hudWidget.getHeight() + ScreenConst.WIDGET_PAD;
		currentWidth = Math.max(currentWidth, hudWidget.getWidth());
	}

	@Override
	public void finalizePositioning() {
		int off = (screenWidth - totalWidth - currentWidth) / 2;
		for (HudWidget widget : widgets) {
			widget.setX(widget.getX() + off);
		}
	}
}
