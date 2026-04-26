package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class LayerConfig {
	public static final LayerConfig DUMMY = new LayerConfig();

	public @Nullable FancyTab fancyTab;
	public Map<String, WidgetConfig> widgets;
	private LayerConfig.@Nullable Identified parent;

	public LayerConfig(@Nullable FancyTab fancyTab, Map<String, WidgetConfig> widgets) {
		this.fancyTab = fancyTab;
		this.widgets = widgets;
	}

	public LayerConfig() {
		this(null, new Object2ObjectOpenHashMap<>());
	}

	public void setParent(LayerConfig.@Nullable Identified parent) {
		this.parent = parent;
	}

	public void visit(LayerBuilder.Visitor visitor) {
		if (parent != null) {
			parent.config().visit((s, w, screenId) -> visitor.visit(s, w, screenId != null ? screenId : parent.id()));
		}
		for (Map.Entry<String, WidgetConfig> entry : widgets.entrySet()) {
			visitor.visit(entry.getKey(), entry.getValue(), null);
		}
	}

	public LayerConfig getFullConfig() {
		LayerConfig parentConfig = new LayerConfig();
		visit((id, widgetConfig, _) -> parentConfig.widgets.put(id, widgetConfig));
		parentConfig.fancyTab = fancyTab;
		return parentConfig;
	}

	public Map<String, WidgetConfig.Meta> getWidgetMetaMap() {
		Map<String, WidgetConfig.Meta> widgets = new HashMap<>();
		visit(((id, widgetConfig, screenId) -> widgets.compute(id, (_, m) -> {
			if (m == null && widgetConfig.config().isEmpty()) return null;
			return new WidgetConfig.Meta(
					Optional.ofNullable(m).flatMap(WidgetConfig.Meta::inheritedFrom),
					Optional.ofNullable(screenId),
					widgetConfig
			);
		})));
		return widgets;
	}

	public @Nullable FancyTab fancyTab() {
		return fancyTab == null && parent != null ? parent.config().fancyTab() : fancyTab;
	}

	public static class FancyTab {
		public boolean enabled;
		public Positioner positioner;
		public ObjectArraySet<String> hiddenWidgets;

		public FancyTab(boolean enabled, Positioner positioner, Set<String> hiddenWidgets) {
			this.enabled = enabled;
			this.positioner = positioner;
			this.hiddenWidgets = new ObjectArraySet<>(hiddenWidgets);
		}

		public FancyTab() {
			this(true, Positioner.CENTERED, Set.of());
		}
	}

	public enum Positioner implements StringRepresentable {
		TOP(TopAlignedWidgetPositioner::new),
		CENTERED(CenteredWidgetPositioner::new);

		public static final Codec<Positioner> CODEC = StringRepresentable.fromEnum(Positioner::values);

		private final BiFunction<Float, Integer, WidgetPositioner> function;

		Positioner(BiFunction<Float, Integer, WidgetPositioner> widgetPositionerSupplier) {
			function = widgetPositionerSupplier;
		}

		public WidgetPositioner getNewPositioner(float maxHeight, int screenHeight) {
			return function.apply(maxHeight, screenHeight);
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.uiAndVisuals.tabHud.defaultPosition." + name());
		}
	}

	public record Identified(ScreenId id, LayerConfig config) {}
}
