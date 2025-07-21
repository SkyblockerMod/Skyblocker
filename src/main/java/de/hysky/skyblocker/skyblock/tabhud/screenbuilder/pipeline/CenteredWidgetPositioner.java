package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class CenteredWidgetPositioner extends WidgetPositioner {

	int totalWidth = 0;
	int totalHeight = 0;

	final int maxY;

	// each column is a pair containing a list of widgets for the rows and an int for the width of the column
	List<ObjectIntPair<List<HudWidget>>> columns = new ArrayList<>();


	int currentY = 0;
	int currentWidth = 0;

	public CenteredWidgetPositioner(float maxHeight, int screenHeight) {
		super(maxHeight, screenHeight);
		columns.add(new ObjectIntMutablePair<>(new ArrayList<>(), 0));
		maxY = Math.min(400, (int) (screenHeight * maxHeight));
	}

	@Override
	public void positionWidget(HudWidget hudWidget) {

		if (currentY + hudWidget.getHeight() > maxY) {
			totalHeight = Math.max(totalHeight, currentY);
			currentY = 0;
			currentWidth = 0;
			columns.add(new ObjectIntMutablePair<>(new ArrayList<>(), 0));
		}
		hudWidget.setY(currentY);
		currentY += hudWidget.getHeight() + ScreenConst.WIDGET_PAD;
		currentWidth = Math.max(currentWidth, hudWidget.getWidth());
		columns.getLast().right(currentWidth);
		columns.getLast().left().add(hudWidget);
	}

	@Override
	public Vector2i finalizePositioning() {
		for (int i = 0; i < columns.size(); i++) {
			ObjectIntPair<List<HudWidget>> listObjectIntPair = columns.get(i);
			int columnWidth = listObjectIntPair.rightInt();
			List<HudWidget> column = listObjectIntPair.left();

			// calculate the height of the column
			int height = (column.size() - 1) * ScreenConst.WIDGET_PAD;
			for (HudWidget tabHudWidget : column) {
				height += tabHudWidget.getHeight();
			}
			// set x and y of the widgets!
			int offset = (totalHeight - height) / 2;
			for (HudWidget tabHudWidget : column) {
				tabHudWidget.setY(tabHudWidget.getY() + offset);
				if (i < columns.size() / 2) {
					tabHudWidget.setX(totalWidth + columnWidth - tabHudWidget.getWidth());
				} else {
					tabHudWidget.setX(totalWidth);
				}
			}
			totalWidth += columnWidth + ScreenConst.WIDGET_PAD;
		}
		return new Vector2i(totalWidth, totalHeight);
	}
}
