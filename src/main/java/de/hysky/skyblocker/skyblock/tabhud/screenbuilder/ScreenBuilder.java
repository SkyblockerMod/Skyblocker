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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScreenBuilder {
	public static final Logger LOGGER = LogUtils.getLogger();


	private final @NotNull ScreenConfig config;
	private final @Nullable ScreenBuilder parent;
	private int positionsHash = 0;

	private final Set<HudWidget> widgets = new ObjectOpenHashSet<>();
	private final Set<HudWidget> tabWidgets = new ObjectLinkedOpenHashSet<>(0); // Linked to keep the order
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
			parentConfig.widgetConfigs.replaceAll((s, widgetConfig) -> widgetConfig.getInherited());
		}
		parentConfig.widgetConfigs.putAll(config.widgetConfigs);
		if (parentConfig.fancyTab().isEmpty()) parentConfig.fancyTab = config.fancyTab;
		return parentConfig;
	}

	public @NotNull ScreenConfig getConfig() {
		return config;
	}

	public void addWidget(@NotNull HudWidget widget) {
		Objects.requireNonNull(widget);
		widgets.add(widget);
		updateRenderedWidgets();
	}

	public void removeWidget(@NotNull HudWidget widget) {
		widgets.remove(widget);
		updateRenderedWidgets();
	}

	public boolean isInScreenBuilder(HudWidget widget) {
		return renderedWidgets.contains(widget);
	}

	/**
	 * Updates the config JSON with the widgets
	 */
	public void updateConfig() {
		Map<String, WidgetConfig> newConfig = new Object2ObjectOpenHashMap<>(widgets.size());
		Set<String> widgetIds = widgets.stream().map(HudWidget::getId).collect(Collectors.toCollection(ObjectOpenHashSet::new));
		for (HudWidget widget : widgets) {
			if (widget.renderingInformation.inherited) continue; // don't want to save inherited stuff
			JsonObject widgetConfig = new JsonObject();
			List<WidgetOption<?>> options = new ArrayList<>();
			widget.getPerScreenOptions(options);
			for (WidgetOption<?> option : options) {
				widgetConfig.add(option.getId(), option.toJson());
			}
			newConfig.put(widget.getId(), new WidgetConfig(Optional.empty(), widgetConfig));
		}
		if (parent != null) {
			for (String s : parent.getFullConfig(false).widgetConfigs.keySet()) {
				if (!widgetIds.contains(s)) {
					newConfig.put(s, new WidgetConfig(Optional.of(Boolean.FALSE), new JsonObject())); // explicitly mark it has disabled if parent(s) has it but not this.
				}
			}
		}
		config.widgetConfigs = newConfig;
	}

	/**
	 * Updates the widget list and their configs
	 */
	public void updateWidgetsList() {
		Profilers.get().push("skyblocker:updateWidgetsList");
		widgets.clear();
		for (Map.Entry<String, WidgetConfig> entry : getFullConfig(true).widgetConfigs.entrySet()) {
			Optional<Boolean> inherited = entry.getValue().inherited();
			if (inherited.isPresent() && !inherited.get()) continue;
			HudWidget widget = WidgetManager.getWidgetOrPlaceholder(entry.getKey());
			JsonObject object = entry.getValue().config();
			widget.renderingInformation.inherited = inherited.orElse(false);
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
		ScreenConfig fullConfig = getFullConfig(false);
		hasFancyTabWidget = fullConfig.hasFancyTab();
		if (!hasFancyTabWidget) return;
		Profilers.get().push("skyblocker:updateTabWidgetsList");
		Set<String> currentWidgets = PlayerListManager.getCurrentWidgets();
		Set<HudWidget> newTabWidgets = new ObjectLinkedOpenHashSet<>(currentWidgets.size());
		FancyTabConfig tabConfig = fullConfig.fancyTab().get();
		for (String s : currentWidgets) {
			HudWidget widget = PlayerListManager.getTabWidget(s);
			if (widget == null) continue;
			if (tabConfig.hiddenWidgets.contains(widget.getId())) continue;
			newTabWidgets.add(widget);
		}
		if (newTabWidgets.equals(tabWidgets)) return;
		tabWidgets.clear();
		tabWidgets.addAll(newTabWidgets);
		for (HudWidget widget : tabWidgets) {
			if (widget instanceof ComponentBasedWidget componentBasedWidget && !componentBasedWidget.shouldUpdateBeforeRendering()) componentBasedWidget.update();
		}
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
			widget.renderingInformation.visible = shouldRender;
			hash = hash * 31 + Boolean.hashCode(shouldRender);
			hash = hash * 31 + Integer.hashCode(widget.getScaledWidth());
			hash = hash * 31 + Integer.hashCode(widget.getScaledHeight());
		}
		if (positionsHash != hash) {
			positionsHash = hash;
			updatePositions(screenWidth, screenHeight);
		}
		for (HudWidget widget : renderedWidgets) {
			if (!widget.renderingInformation.visible && !renderConfig) continue;
			if (renderConfig) widget.renderConfig(context);
			else widget.render(context);
		}
		Profilers.get().pop();
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(widgets, screenWidth, screenHeight);
		ScreenConfig fullConfig = getFullConfig(false);
		if (fullConfig.hasFancyTab()) {
			WidgetPositioner positioner = fullConfig.fancyTab().get().positioner.getNewPositioner(0.9f, screenHeight);
			tabWidgets.forEach(positioner::positionWidget);
			Vector2i dimensions = positioner.finalizePositioning();
			int x = (screenWidth - dimensions.x) / 2;
			int y = (screenHeight - dimensions.y) / 2;
			for (HudWidget widget : tabWidgets) {
				widget.setPosition(widget.getX() + x, widget.getY() + y);
			}
		}
	}

	public static void updatePositions(Collection<HudWidget> widgets, int screenWidth, int screenHeight) {
		Profilers.get().push("skyblocker:updatePositions");
		widgets.forEach(w -> w.renderingInformation.positioned = false);
		for (HudWidget widget : widgets) {
			if (!widget.renderingInformation.positioned) WidgetPositioner.applyRuleToWidget(widget, screenWidth, screenHeight);
		}
		Profilers.get().pop();
	}

	public Collection<HudWidget> getWidgets() {
		return widgets;
	}

	public static class ScreenConfig {
		public static final Codec<ScreenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				FancyTabConfig.CODEC.optionalFieldOf("fancy_tab").forGetter(ScreenConfig::fancyTab),
				Codec.unboundedMap(Codec.STRING, WidgetConfig.CODEC).fieldOf("widgets").forGetter(c -> c.widgetConfigs)
		).apply(instance, ScreenConfig::new));

		public @Nullable FancyTabConfig fancyTab;
		public Map<String, WidgetConfig> widgetConfigs;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public ScreenConfig(Optional<FancyTabConfig> fancyTab, Map<String, WidgetConfig> widgetConfigs) {
			this.fancyTab = fancyTab.orElse(null);
			this.widgetConfigs = widgetConfigs;
		}

		public ScreenConfig() {
			this(Optional.empty(), new Object2ObjectOpenHashMap<>());
		}

		public boolean hasFancyTab() {
			return fancyTab != null && fancyTab.enabled;
		}

		public Optional<FancyTabConfig> fancyTab() {
			return Optional.ofNullable(fancyTab);
		}

		public FancyTabConfig getOrCreateFancyTab() {
			if (fancyTab == null) {
				fancyTab = new FancyTabConfig();
			}
			return fancyTab;
		}
	}

	public record WidgetConfig(Optional<Boolean> inherited, JsonObject config) {
		public static final Codec<WidgetConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("inherited").forGetter(WidgetConfig::inherited),
				CodecUtils.JSON_OBJECT_CODEC.optionalFieldOf("config", new JsonObject()).forGetter(WidgetConfig::config)
		).apply(instance, WidgetConfig::new));

		public WidgetConfig getInherited() {
			return new WidgetConfig(Optional.of(Boolean.TRUE), config.deepCopy());
		}
	}

	public static class FancyTabConfig {
		public static final Codec<FancyTabConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("enabled").forGetter(c -> c.enabled),
				Positioner.CODEC.optionalFieldOf("positioner", Positioner.CENTERED).forGetter(c -> c.positioner),
				Codec.STRING.listOf().optionalFieldOf("hidden_widgets", List.of())
						.xmap(ObjectOpenHashSet::new, List::copyOf)
						.forGetter(c -> c.hiddenWidgets)
		).apply(instance, FancyTabConfig::new));

		public boolean enabled;
		public Positioner positioner;
		public ObjectOpenHashSet<String> hiddenWidgets;

		public FancyTabConfig() {
			this(true, Positioner.CENTERED, Set.of());
		}

		public FancyTabConfig(boolean enabled, Positioner positioner, Set<String> hiddenWidgets) {
			this.enabled = enabled;
			this.positioner = positioner;
			this.hiddenWidgets = new ObjectOpenHashSet<>(hiddenWidgets);
		}
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

		@Override
		public String toString() {
			return I18n.translate("skyblocker.config.uiAndVisuals.tabHud.defaultPosition." + name());
		}
	}
}
