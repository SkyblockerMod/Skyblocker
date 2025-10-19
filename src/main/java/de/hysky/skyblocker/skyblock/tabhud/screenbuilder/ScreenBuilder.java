package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.CodecUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScreenBuilder {
	public static final Logger LOGGER = LogUtils.getLogger();
	private static boolean positionsDirty = false;

	public static void markPositionsDirty() {
		positionsDirty = true;
	}


	private @NotNull ScreenConfig config;
	private final @Nullable ScreenBuilder parent;
	private int positionsHash = 0;

	private final Set<HudWidget> widgets = new ObjectOpenHashSet<>();
	private final Set<HudWidget> tabWidgets = new ObjectOpenHashSet<>(0);
	private final Set<HudWidget> renderedWidgets = new ObjectOpenHashSet<>();
	public boolean hasFancyTabWidget = false;


	public ScreenBuilder(@NotNull ScreenConfig config, @Nullable ScreenBuilder parent) {
		this.config = config;
		this.parent = parent;
	}

	public @NotNull ScreenConfig getFullConfig(boolean addInheritedIndicator) {
		ScreenConfig parentConfig;
		if (parent != null) {
			parentConfig = parent.getFullConfig(addInheritedIndicator);
		} else {
			parentConfig = new ScreenConfig();
		}
		if (addInheritedIndicator) {
			parentConfig.widgetConfigs().replaceAll((s, widgetConfig) -> widgetConfig.getInherited());
		}
		for (Map.Entry<String, WidgetConfig> entry : config.widgetConfigs().entrySet()) {
			parentConfig.widgetConfigs().put(entry.getKey(), entry.getValue());
		}
		return parentConfig;
	}

	public @NotNull ScreenConfig getConfig() {
		return config;
	}

	public void setConfig(@NotNull ScreenConfig config) {
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
		Map<String, WidgetConfig> newConfig = new Object2ObjectOpenHashMap<>(widgets.size());
		Set<String> widgetIds = widgets.stream().map(HudWidget::getId).collect(Collectors.toCollection(ObjectOpenHashSet::new));
		for (HudWidget widget : widgets) {
			if (widget.isInherited()) continue; // don't want to save inherited stuff
			JsonObject widgetConfig = new JsonObject();
			List<WidgetOption<?>> options = new ArrayList<>();
			widget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				widgetConfig.add(option.getId(), option.toJson());
			}
			newConfig.put(widget.getId(), new WidgetConfig(Optional.empty(),  widgetConfig));
		}
		if (parent != null) {
			for (String s : parent.getFullConfig(false).widgetConfigs().keySet()) {
				if (!widgetIds.contains(s)) {
					newConfig.put(s, new WidgetConfig(Optional.of(Boolean.FALSE), new JsonObject())); // explicitly mark it has disabled if parent(s) has it but not this.
				}
			}
		}
		setConfig(config.withWidgets(newConfig));
	}

	/**
	 * Updates the widget list and their configs
	 */
	public void updateWidgetsList() {
		Profilers.get().push("skyblocker:updateWidgetsList");
		widgets.clear();
		for (Map.Entry<String, WidgetConfig> entry : getFullConfig(true).widgetConfigs().entrySet()) {
			HudWidget widget = WidgetManager.getWidgetOrPlaceholder(entry.getKey());
			JsonObject object = entry.getValue().config();
			widget.setInherited(entry.getValue().inherited().orElse(false));
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
		updateRenderedWidgets();
		Profilers.get().pop();
	}

	public void updateTabWidgetsList() {
		if (!config.hasFancyTab()) return;
		Profilers.get().push("skyblocker:updateTabWidgetsList");
		Set<HudWidget> newTabWidgets = new ObjectOpenHashSet<>();
		FancyTabConfig tabConfig = config.fancyTab().get();
		for (String s : PlayerListManager.getCurrentWidgets()) {
			HudWidget widget = PlayerListManager.getTabWidget(s);
			if (widget == null) continue;
			if (tabConfig.hiddenWidgets().contains(widget.getId())) continue;
			newTabWidgets.add(widget);
		}
		if (newTabWidgets.equals(tabWidgets)) return;
		tabWidgets.clear();
		tabWidgets.addAll(newTabWidgets);
		updateRenderedWidgets();
		Profilers.get().pop();
	}

	private void updateRenderedWidgets() {
		renderedWidgets.clear();
		renderedWidgets.addAll(widgets);
		renderedWidgets.addAll(tabWidgets);
		positionsHash = 0;
	}

	public void render(DrawContext context, int screenWidth, int screenHeight, boolean renderConfig) {
		Profilers.get().push("skyblocker:renderHud");
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (HudWidget widget : renderedWidgets) {
			boolean shouldRender = widget.shouldRender() || renderConfig;
			widget.setVisible(shouldRender);
			hash = hash * 31 + Boolean.hashCode(shouldRender);
			hash = hash * 31 + Integer.hashCode(widget.getScaledWidth());
			hash = hash * 31 + Integer.hashCode(widget.getScaledHeight());
		}
		if (positionsDirty || positionsHash != hash) {
			positionsHash = hash;
			positionsDirty = false;
			updatePositions(screenWidth, screenHeight);
		}
		for (HudWidget widget : renderedWidgets) {
			if (!widget.shouldRender() && !renderConfig) continue;
			if (renderConfig) widget.renderConfig(context);
			else widget.render(context);
		}
		Profilers.get().pop();
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(widgets, screenWidth, screenHeight);
		if (config.hasFancyTab()) {
			WidgetPositioner positioner = config.fancyTab().get().positioner().getNewPositioner(0.9f, screenHeight);
			tabWidgets.forEach(positioner::positionWidget);
			positioner.finalizePositioning();
		}
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

	public record ScreenConfig(Optional<FancyTabConfig> fancyTab, Map<String, WidgetConfig> widgetConfigs) {
		public static final Codec<ScreenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				FancyTabConfig.CODEC.optionalFieldOf("fancy_tab").forGetter(ScreenConfig::fancyTab),
				Codec.unboundedMap(Codec.STRING, WidgetConfig.CODEC).fieldOf("widgets").forGetter(ScreenConfig::widgetConfigs)
		).apply(instance, ScreenConfig::new));

		public ScreenConfig() {
			this(Optional.empty(), new Object2ObjectOpenHashMap<>());
		}

		public boolean hasFancyTab() {
			return fancyTab.map(FancyTabConfig::enabled).orElse(false);
		}

		public ScreenConfig withWidgets(Map<String, WidgetConfig> widgetConfigs) {
			return new ScreenConfig(fancyTab, widgetConfigs);
		}
	}

	public record WidgetConfig(Optional<Boolean> inherited, JsonObject config) {
		public static final Codec<WidgetConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("inherited").forGetter(WidgetConfig::inherited),
				CodecUtils.JSON_OBJECT_CODEC.optionalFieldOf("config", new JsonObject()).forGetter(WidgetConfig::config)
		).apply(instance, WidgetConfig::new));

		public WidgetConfig getInherited() {
			return new WidgetConfig(Optional.of(Boolean.TRUE), config);
		}
	}

	public record FancyTabConfig(boolean enabled, Positioner positioner, List<String> hiddenWidgets) {
		public static final Codec<FancyTabConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("enabled").forGetter(FancyTabConfig::enabled),
				Positioner.CODEC.optionalFieldOf("positioner", Positioner.CENTERED).forGetter(FancyTabConfig::positioner),
				Codec.STRING.listOf().optionalFieldOf("hidden_widgets", List.of()).forGetter(FancyTabConfig::hiddenWidgets)
		).apply(instance, FancyTabConfig::new));
	}

	public enum Positioner implements StringIdentifiable {
		TOP(TopAlignedWidgetPositioner::new),
		CENTERED(CenteredWidgetPositioner::new);

		public static final Codec<Positioner> CODEC = StringIdentifiable.createCodec(Positioner::values);

		private final BiFunction<Float, Integer, WidgetPositioner> function;

		Positioner(BiFunction<Float, Integer, WidgetPositioner> widgetPositionerSupplier) {
			function = widgetPositionerSupplier;
		}

		public WidgetPositioner getNewPositioner(float maxHeight, int screenHeight) {
			return function.apply(maxHeight, screenHeight);
		}

		@Override
		public String asString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
