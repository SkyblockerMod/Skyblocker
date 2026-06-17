package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.Positioner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LayerConfig {
	public static final LayerConfig DUMMY = new LayerConfig();
	public static final Codec<LayerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FancyTab.CODEC.optionalFieldOf("fancyTab").forGetter(c -> Optional.ofNullable(c.fancyTab)),
			Codec.unboundedMap(Codec.STRING, WidgetConfig.CODEC).fieldOf("widgets").forGetter(c -> c.widgets)
	).apply(instance, LayerConfig::new));

	public @Nullable FancyTab fancyTab;
	public final Map<String, WidgetConfig> widgets;

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

	public @Nullable FancyTab fancyTab() {
		return fancyTab;
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
}
