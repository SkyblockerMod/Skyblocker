package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class LayerConfig {
	public static final LayerConfig DUMMY = new LayerConfig();
	public static final Codec<LayerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FancyTab.CODEC.optionalFieldOf("fancyTab").forGetter(c -> Optional.ofNullable(c.fancyTab)),
			Codec.unboundedMap(Codec.STRING, WidgetConfig.CODEC).fieldOf("widgets").forGetter(c -> c.widgets)
	).apply(instance, LayerConfig::new));

	public @Nullable FancyTab fancyTab;
	public Map<String, WidgetConfig> widgets;
	private LayerConfig.@Nullable Identified parent;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public LayerConfig(Optional<FancyTab> fancyTab, Map<String, WidgetConfig> widgetConfigs) {
		this(fancyTab.orElse(null), widgetConfigs);
	}

	public LayerConfig(@Nullable FancyTab fancyTab, Map<String, WidgetConfig> widgets) {
		this.fancyTab = fancyTab;
		this.widgets = new Object2ObjectOpenHashMap<>(widgets);
	}

	public LayerConfig() {
		this(Optional.empty(), new Object2ObjectOpenHashMap<>());
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

	public WidgetConfig.@Nullable Meta getMeta(String id) {
		WidgetConfig.Meta parentMeta = null;
		if (parent != null) {
			parentMeta = parent.config.getMeta(id);
		}
		if (parentMeta != null) {
			// mark it as inherited from the parent
			parentMeta = new WidgetConfig.Meta(parentMeta.overrides(), parentMeta.inheritedFrom().or(() -> Optional.of(parent.id)), parentMeta.widgetConfig());
		}
		if (widgets.containsKey(id)) {
			if (parentMeta != null) {
				// this config is overriding it.
				return new WidgetConfig.Meta(parentMeta.inheritedFrom(), Optional.empty(), widgets.get(id));
			} else {
				return new WidgetConfig.Meta(Optional.empty(), Optional.empty(), widgets.get(id));
			}
		}
		return parentMeta;
	}

	public @Nullable FancyTab fancyTab() {
		return fancyTab == null && parent != null ? parent.config().fancyTab() : fancyTab;
	}

	public static class FancyTab {
		public static final Codec<FancyTab> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("enabled").forGetter(c -> c.enabled),
				Positioner.CODEC.optionalFieldOf("positioner", Positioner.CENTERED).forGetter(c -> c.positioner),
				Codec.STRING.listOf().optionalFieldOf("hidden_widgets", List.of())
						.xmap(ObjectArraySet::new, List::copyOf)
						.forGetter(c -> c.hiddenWidgets)
		).apply(instance, FancyTab::new));
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
