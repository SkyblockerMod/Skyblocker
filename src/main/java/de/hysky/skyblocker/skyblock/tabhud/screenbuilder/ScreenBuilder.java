package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ScreenBuilder {
	public static final Logger LOGGER = LogUtils.getLogger();
	private static boolean positionsDirty = false;

	public static void markPositionsDirty() {
		positionsDirty = true;
	}


	private @NotNull JsonObject config;
	private final @Nullable ScreenBuilder parent;
	private int positionsHash = 0;

	private final Set<HudWidget> widgets = new HashSet<>();


	public ScreenBuilder(@NotNull JsonObject config, @Nullable ScreenBuilder parent) {
		this.config = config;
		this.parent = parent;
	}

	public @NotNull JsonObject getFullConfig(boolean addInheritedIndicator) {
		JsonObject parentConfig = new JsonObject();
		if (parent != null) {
			parentConfig = parent.getFullConfig(addInheritedIndicator).deepCopy();
		}
		if (addInheritedIndicator) {
			for (String s : parentConfig.keySet()) {
				JsonElement element = parentConfig.get(s);
				if (element.isJsonObject()) {
					element.getAsJsonObject().addProperty("inherited", true);
				}
			}
		}
		for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
			parentConfig.add(entry.getKey(), entry.getValue().deepCopy());
		}
		return parentConfig;
	}

	public @NotNull JsonObject getConfig() {
		return config;
	}

	public void setConfig(@NotNull JsonObject config) {
		this.config = config;
	}

	public void addWidget(@NotNull HudWidget widget) {
		Objects.requireNonNull(widget);
		widgets.add(widget);
	}

	public void removeWidget(@NotNull HudWidget widget) {
		widgets.remove(widget);
	}

	/**
	 * Updates the config JSON with the widgets
	 */
	public void updateConfig() {
		JsonObject newConfig = new JsonObject();
		Set<String> widgetIds = widgets.stream().map(w -> w.getInformation().id()).collect(Collectors.toCollection(ObjectOpenHashSet::new));
		for (HudWidget widget : widgets) {
			if (widget.isInherited()) continue; // don't want to save inherited stuff
			JsonObject widgetConfig = new JsonObject();
			List<WidgetOption<?>> options = new ArrayList<>();
			widget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				widgetConfig.add(option.getId(), option.toJson());
			}
			newConfig.add(widget.getInformation().id(), widgetConfig);
		}
		if (parent != null) {
			for (String s : parent.getFullConfig(false).keySet()) {
				if (!widgetIds.contains(s)) {
					newConfig.addProperty(s, false); // explicitly mark it has disabled if parent(s) has it but not this.
				}
			}
		}
		setConfig(newConfig);
	}

	/**
	 * Updates the widget list and their configs
	 */
	public void updateWidgetsList() {
		Profilers.get().push("skyblocker:updateWidgetsList");
		widgets.clear();
		for (Map.Entry<String, JsonElement> entry : getFullConfig(true).entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				if (entry.getValue().isJsonPrimitive() && !entry.getValue().getAsJsonPrimitive().getAsBoolean()) continue;
				throw new IllegalStateException("Invalid widget config: " + entry.getKey());
			}
			HudWidget widget = WidgetManager.getWidgetOrPlaceholder(entry.getKey());
			JsonObject object = entry.getValue().getAsJsonObject();
			widget.setInherited(object.has("inherited") && object.get("inherited").getAsBoolean());
			List<WidgetOption<?>> options = new ArrayList<>();
			widget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				JsonElement element = object.get(option.getId());
				if (element == null) {
					LOGGER.warn("Widget {} has no value for option {}", entry.getKey(), option.getId());
					continue;
				}
				try {
					option.fromJson(element);
				} catch (Exception e) {
					LOGGER.warn("WARNING: Widget {} has invalid value for option {}", entry.getKey(), option.getId(), e);
				}
			}
			widgets.add(widget);
		}
		for (HudWidget widget : widgets) {
			if (widget instanceof ComponentBasedWidget componentBasedWidget && !componentBasedWidget.shouldUpdateBeforeRendering()) componentBasedWidget.update();
		}
		Profilers.get().pop();
	}

	public void render(DrawContext context, int screenWidth, int screenHeight, boolean renderConfig) {
		Profilers.get().push("skyblocker:renderHud");
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (HudWidget widget : widgets) {
			boolean shouldRender = widget.shouldRender();
			widget.setVisible(shouldRender);
			hash = hash * 31 + Boolean.hashCode(shouldRender);
			hash = hash * 31 + Integer.hashCode(widget.getScaledWidth());
			hash = hash * 31 + Integer.hashCode(widget.getScaledHeight());
		}
		if (positionsDirty || positionsHash != hash) {
			positionsHash = hash;
			positionsDirty = false;
			updatePositions(widgets, screenWidth, screenHeight);
		}
		for (HudWidget widget : widgets) {
			if (!widget.shouldRender()) continue;
			if (renderConfig) widget.renderConfig(context);
			else widget.render(context);
		}
		Profilers.get().pop();
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(widgets, screenWidth, screenHeight);
	}

	public static void updatePositions(Collection<HudWidget> widgets, int screenWidth, int screenHeight) {
		Profilers.get().push("skyblocker:updatePositions");
		widgets.forEach(w -> w.setPositioned(false));
		for (HudWidget widget : widgets) {
			if (!widget.isPositioned()) WidgetPositioner.applyRuleToWidget(widget, screenWidth, screenHeight);
		}
		Profilers.get().pop();
	}

	public Collection<HudWidget> getWidgets() {
		return widgets;
	}
}
