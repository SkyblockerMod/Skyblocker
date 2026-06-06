package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.profiling.Profiler;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A {@link LayerBuilder} specialized to display fancy tab
 */
public class TabLayerBuilder extends LayerBuilder {
	private final Set<PositionedWidget> merged = new ObjectOpenHashSet<>();
	private final List<PositionedWidget> tabWidgets = new LinkedList<>();

	public void clearTab() {
		tabWidgets.clear();
		merge();
	}

	@Override
	protected void updateList() {
		merge();
		super.updateList();
	}

	protected void merge() {
		merged.clear();
		merged.addAll(widgets);
		merged.addAll(tabWidgets);

		renderedWidgets.clear();
		merged.stream().map(w -> w.widget).forEach(renderedWidgets::add);
	}

	public void updateTab(Collection<String> ignoredWidgets) {
		Profiler.get().push("skyblocker:updateTabWidgetsList");
		Set<String> currentWidgets = PlayerListManager.getCurrentWidgets();
		List<PositionedWidget> newTabWidgets = new LinkedList<>();
		for (String s : currentWidgets) {
			HudWidget widget = PlayerListManager.getTabWidget(s);
			if (widget == null) continue;
			if (ignoredWidgets.contains(widget.getInternalID())) continue;
			PositionedWidget e = new PositionedWidget(widget, PositionRule.DEFAULT);
			e.fromTab = true;
			newTabWidgets.add(e);
		}
		if (newTabWidgets.equals(tabWidgets)) return;
		tabWidgets.clear();
		tabWidgets.addAll(newTabWidgets);
		/*for (HudWidget widget : tabWidgets) {
			if (widget instanceof ComponentBasedWidget componentBasedWidget && !componentBasedWidget.shouldUpdateBeforeRendering()) componentBasedWidget.update();
		}*/
		merge();
		Profiler.get().pop();
	}

	@Override
	public Set<PositionedWidget> getRendered() {
		return merged;
	}

	@Override
	public void updatePositions(int screenWidth, int screenHeight) {
		super.updatePositions(screenWidth, screenHeight);
		if (!tabWidgets.isEmpty()) {
			WidgetPositioner positioner = SkyblockerConfigManager.get().uiAndVisuals.tabHud.defaultPositioning.getNewPositioner(0.9f, screenHeight);
			List<HudWidget> tabWidgets = getRendered().stream().filter(p -> p.fromTab).map(p -> p.widget).toList();
			tabWidgets.forEach(positioner::positionWidget);
			Vector2i dimensions = positioner.finalizePositioning();
			int x = (screenWidth - dimensions.x) / 2;
			int y = (screenHeight - dimensions.y) / 2;
			tabWidgets.forEach(widget ->
					widget.setPosition(widget.getX() + x, widget.getY() + y)
			);
		}
	}
}
