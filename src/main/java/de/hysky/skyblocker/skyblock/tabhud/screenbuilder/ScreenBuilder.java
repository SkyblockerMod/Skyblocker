package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ScreenBuilder {

	public static final Logger LOGGER = LogUtils.getLogger();


	private @NotNull JsonObject config;
	private final @Nullable ScreenBuilder parent;
	private int positionsHash = 0;

	private final List<HudWidget> widgets = new ArrayList<>();


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

	/**
	 * Updates the widget list and their configs
	 */
	public void updateWidgets() {
		widgets.clear();
		for (Map.Entry<String, JsonElement> entry : getFullConfig(false).entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				if (entry.getValue().isJsonPrimitive() && !entry.getValue().getAsJsonPrimitive().getAsBoolean()) continue;
				throw new IllegalStateException("Invalid widget config: " + entry.getKey());
			}
			HudWidget widget = WidgetManager.getWidget(entry.getKey());
			if (widget == null) continue;
			JsonObject object = entry.getValue().getAsJsonObject();
			for (WidgetOption<?> option : widget.getOptions()) {
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
	}

	public void render(DrawContext context, int screenWidth, int screenHeight, boolean renderConfig) {
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (HudWidget widget : widgets) {
			hash = hash * 31 + Boolean.hashCode(widget.shouldRender());
			hash = hash * 31 + Integer.hashCode(widget.getWidth());
			hash = hash * 31 + Integer.hashCode(widget.getHeight());
		}
		if (positionsHash != hash) {
			positionsHash = hash;
			updatePositions(widgets, screenWidth, screenHeight);
		}
		for (HudWidget widget : widgets) {
			if (renderConfig) widget.renderConfig(context);
			else widget.render(context);
		}
	}

	public static void updatePositions(List<HudWidget> widgets, int screenWidth, int screenHeight) {
		widgets.forEach(w -> w.setPositioned(false));
		for (HudWidget widget : widgets) {
			if (!widget.isPositioned()) WidgetPositioner.applyRuleToWidget(widget, screenWidth, screenHeight);
		}
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
	}

}
