package de.hysky.skyblocker.utils.container;

import com.demonwav.mcdev.annotations.Translatable;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface SlotTextAdder extends ContainerMatcher {

	/**
	 * This method will be called for each rendered slot. Consider using a switch statement on {@code slotId} if you wish to limit the text to specific slots.
	 *
	 * @return A list of positioned text to be rendered. Return {@link List#of()} if no text should be rendered.
	 * @implNote By minecraft's design, scaled text inexplicably moves around.
	 * It's also not anti-aliased, so it looks horribly jagged and unreadable when scaled down too much.
	 * So, limit your text to 3 characters (or roughly less than 20 width) if you want it to not look horrible.
	 */
	@NotNull
	List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId);

	@Override
	default boolean isEnabled() {
		if (getConfigInformation() == null) return SlotTextManager.isEnabled();
		return SlotTextManager.isEnabled(getConfigInformation().id());
	}

	@Nullable
	default ConfigInformation getConfigInformation() {
		return null;
	}

	record ConfigInformation(String id, Text name, @Nullable OptionDescription description) {
		public ConfigInformation(String id, Text name) {
			this(id, name, null);
		}

		public ConfigInformation(String id, @Translatable String name) {
			this(id, Text.translatable(name));
		}

		public ConfigInformation(String id, @Translatable String name, @Translatable String... descriptionLines) {
			this(id, name, Stream.of(descriptionLines).map(Text::translatable).toArray(Text[]::new));
		}

		// Additional constructor in case the description lines have any formatting
		public ConfigInformation(String id, @Translatable String name, Text... descriptionLines) {
			this(id, Text.translatable(name), OptionDescription.of(descriptionLines));
		}

		public Option<Boolean> getOption(SkyblockerConfig config) {
			return Option.<Boolean>createBuilder()
					.name(name)
					.description(description != null ? description : OptionDescription.EMPTY)
					.binding(true,
							() -> config.uiAndVisuals.slotText.textEnabled.getOrDefault(id, true),
							newValue -> config.uiAndVisuals.slotText.textEnabled.put(id, newValue.booleanValue()))
					.controller(ConfigUtils::createBooleanController)
					.build();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ConfigInformation other && Objects.equals(id, other.id);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
	}
}
