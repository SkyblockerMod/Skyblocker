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
}
