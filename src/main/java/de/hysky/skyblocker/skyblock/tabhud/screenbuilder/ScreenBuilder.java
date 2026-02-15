package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import org.jspecify.annotations.Nullable;

public class ScreenBuilder {
	// TODO: eliminate this static field completely?
	// 	we can get rid of this field by moving the widget dimensions check into `updateWidgetLists`
	private static boolean positionsNeedsUpdating = true;

	private final Map<String, PositionRule> positioning = new Object2ObjectOpenHashMap<>();
	private Map<String, PositionRule> positioningBackup = null;
	private final Location location;

	private List<HudWidget> hudScreen = new ArrayList<>();
	private List<HudWidget> mainTabScreen = new ArrayList<>();
	private List<HudWidget> secondaryTabScreen = new ArrayList<>();

	/**
	 * Create a ScreenBuilder from a json.
	 */
	public ScreenBuilder(Location location) {
		this.location = location;
	}

	public @Nullable PositionRule getPositionRule(String widgetInternalId) {
		return positioning.get(widgetInternalId);
	}

	public void forEachPositionRuleEntry(BiConsumer<String, PositionRule> action) {
		positioning.forEach(action);
	}

	public PositionRule getPositionRuleOrDefault(String widgetInternalId) {
		PositionRule positionRule = getPositionRule(widgetInternalId);
		return positionRule == null ? PositionRule.DEFAULT : positionRule;
	}

	public void setPositionRule(String widgetInternalId, @Nullable PositionRule newPositionRule) {
		if (newPositionRule == null) positioning.remove(widgetInternalId);
		else positioning.put(widgetInternalId, newPositionRule);
	}

	public void backupPositioning() {
		positioningBackup = Map.copyOf(positioning);
	}

	public void restorePositioningFromBackup() {
		if (positioningBackup == null) return;
		positioning.clear();
		positioning.putAll(positioningBackup);
	}

	public static void markDirty() {
		positionsNeedsUpdating = true;
	}

	/**
	 * Updates the lists of widgets that should be rendered. This method runs every frame to check if any widgets have changed visibility (shouldRender).
	 * @param config whether this render in happening in the config screen
	 * @return true if the lists have changed and positioners should run, false if they are the same as before and repositioning is not needed
	 */
	public boolean updateWidgetLists(boolean config) {
		// Save the hud widgets that should be rendered to new lists
		final List<HudWidget> hudNew = new ArrayList<>();
		final List<HudWidget> mainTabNew = new ArrayList<>();
		final List<HudWidget> secondaryTabNew = new ArrayList<>();

		for (HudWidget widget : WidgetManager.widgetInstances.values()) {
			widget.setVisible(false);
			if (config ? widget.isEnabledIn(location) : widget.shouldRender(location)) { // TabHudWidget has this at false
				// TODO maybe behavior to change? (having no position rule on a normal hud widget shouldn't quite be possible)
				PositionRule rule = getPositionRule(widget.getInternalID());
				if (rule == null) {
					hudNew.add(widget);
				} else {
					switch (rule.screenLayer()) {
						case MAIN_TAB -> mainTabNew.add(widget);
						case SECONDARY_TAB -> secondaryTabNew.add(widget);
						case null, default -> hudNew.add(widget);
					}
				}
				widget.setVisible(true);
				widget.setPositioned(false);
			}
		}

		for (TabHudWidget widget : PlayerListManager.tabWidgetsToShow) {
			if (!config && widget.isEmpty()) continue;
			PositionRule rule = getPositionRule(widget.getInternalID());
			widget.setVisible(true);
			if (rule == null) {
				mainTabNew.add(widget);
			} else {
				widget.setPositioned(false);
				switch (rule.screenLayer()) {
					case HUD -> hudNew.add(widget);
					case SECONDARY_TAB -> secondaryTabNew.add(widget);
					case null, default -> mainTabNew.add(widget);
				}
			}
		}

		// Compare the newly generated lists with the old ones
		if (hudScreen.equals(hudNew) && mainTabScreen.equals(mainTabNew) && secondaryTabScreen.equals(secondaryTabNew)) {
			return false;
		}
		hudScreen = hudNew;
		mainTabScreen = mainTabNew;
		secondaryTabScreen = secondaryTabNew;

		return true;
	}

	/**
	 * Updates the widgets (if needed) after the new widget list has been generated and before positioners run.
	 */
	public void updateWidgets(WidgetManager.ScreenLayer screenLayer) {
		for (HudWidget widget : getHudWidgets(screenLayer)) {
			if (widget.shouldUpdateBeforeRendering()) widget.update();
		}
	}

	public void positionWidgets(int screenW, int screenH) {
		WidgetPositioner newPositioner = SkyblockerConfigManager.get().uiAndVisuals.tabHud.defaultPositioning.getNewPositioner(screenW, screenH);

		// Auto positioning
		for (HudWidget widget : mainTabScreen) {

			if (getPositionRule(widget.getInternalID()) != null) {
				widget.setPositioned(false);
			} else {
				newPositioner.positionWidget(widget);
				widget.setPositioned(true);
			}
		}
		newPositioner.finalizePositioning();
		// Custom positioning
		for (HudWidget widget : mainTabScreen) {
			if (!widget.isPositioned()) {
				WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
			}
		}

		for (HudWidget widget : hudScreen) {
			if (!widget.isPositioned()) {
				WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
			}
		}
		for (HudWidget widget : secondaryTabScreen) {
			if (!widget.isPositioned()) {
				WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
			}
		}
	}

	/**
	 * Renders the widgets present on the specified layer. Doesn't scale with the config option.
	 */
	public void renderWidgets(GuiGraphics context, WidgetManager.ScreenLayer screenLayer) {
		List<HudWidget> widgetsToRender = getHudWidgets(screenLayer);

		for (HudWidget widget : widgetsToRender) {
			widget.render(context);
		}
	}

	public List<HudWidget> getHudWidgets(WidgetManager.ScreenLayer screenLayer) {
		return switch (screenLayer) {
			case MAIN_TAB -> mainTabScreen;
			case SECONDARY_TAB -> secondaryTabScreen;
			case HUD -> hudScreen;
			case null, default -> List.of();
		};
	}

	/**
	 * Builds and renders the given {@link de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager.ScreenLayer WidgetManager.ScreenLayer}, which
	 * {@link #updateWidgetLists(boolean) updates the widget lists (for all screen layers)}, {@link #updateWidgets(WidgetManager.ScreenLayer) updates the widgets (for the current screen layer)},
	 * {@link #positionWidgets(int, int) positions the widgets}, and {@link #renderWidgets(GuiGraphics, WidgetManager.ScreenLayer) renders the widgets}.
	 */
	public void run(GuiGraphics context, int screenW, int screenH, WidgetManager.ScreenLayer screenLayer) {
		boolean widgetListsChanged = updateWidgetLists(false);

		updateWidgets(screenLayer);

		if (widgetListsChanged || positionsNeedsUpdating) {
			positionsNeedsUpdating = false;
			positionWidgets(screenW, screenH);
		}

		renderWidgets(context, screenLayer);
	}


	public enum DefaultPositioner {
		TOP(TopAlignedWidgetPositioner::new),
		CENTERED(CenteredWidgetPositioner::new);

		private final BiFunction<Integer, Integer, WidgetPositioner> function;

		DefaultPositioner(BiFunction<Integer, Integer, WidgetPositioner> widgetPositionerSupplier) {
			function = widgetPositionerSupplier;
		}

		public WidgetPositioner getNewPositioner(int screenWidth, int screenHeight) {
			return function.apply(screenWidth, screenHeight);
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.uiAndVisuals.tabHud.defaultPosition." + name());
		}
	}

}
