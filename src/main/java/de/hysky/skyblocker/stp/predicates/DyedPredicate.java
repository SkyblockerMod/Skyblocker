package de.hysky.skyblocker.stp.predicates;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record DyedPredicate(boolean item, OptionalInt color) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "dyed");
	public static final Codec<DyedPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("item").forGetter(DyedPredicate::item),
			Color.CODEC.optionalFieldOf("color").forGetter(dyed -> Optional.of(new Color(dyed.color(), OptionalInt.empty(), OptionalInt.empty()))))
			.apply(instance, DyedPredicate::new));

	private DyedPredicate(boolean item, Optional<Color> color) {
		this(item, color.map(Color::get).orElseGet(OptionalInt::empty));
	}

	@Override
	public boolean test(ItemStack stack) {
		GeneralConfig config = SkyblockerConfigManager.get().general;
		String itemUuid = ItemUtils.getItemUuid(stack);

		DyedColorComponent colorComponent = stack.get(DataComponentTypes.DYED_COLOR);

		boolean skyblockDye = ItemUtils.getCustomData(stack).contains("dye_item") || config.customDyeColors.containsKey(itemUuid) || config.customAnimatedDyes.containsKey(itemUuid);
		boolean isColorCorrect = color().isEmpty() || (colorComponent != null && color().getAsInt() == (item ? DyedColorComponent.getColor(stack, -1) : colorComponent.rgb()));
		return (item ? skyblockDye : colorComponent != null) && isColorCorrect;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.DYED;
	}

	private record Color(OptionalInt decimal, OptionalInt hex, OptionalInt rgb) {
		public static final Codec<Color> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("decimal")).forGetter(Color::decimal),
				Codec.STRING.optionalFieldOf("hex").xmap(s ->
					s.map(string -> OptionalInt.of(Integer.parseInt(string.replace("#", ""), 16))).orElseGet(OptionalInt::empty),
						i -> i.isPresent() ? Optional.of(Integer.toHexString(i.getAsInt())) : Optional.empty()
				).forGetter(Color::hex),
				CodecUtils.optionalInt(Codec.INT.listOf(3, 3).xmap(
						l -> ((l.getFirst() & 255) << 16) + ((l.get(1) & 255) << 8) + (l.getLast() & 255),
						i -> List.of(i >> 16 & 255, i >> 8 & 255, i & 255)).optionalFieldOf("rgb")).forGetter(Color::rgb))
				.apply(instance, Color::new));

		OptionalInt get() {
			return decimal.isPresent() ? decimal : hex.isPresent() ? hex : rgb.isPresent() ? rgb : OptionalInt.empty();
		}
	}
}
