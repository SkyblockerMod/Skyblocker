package de.hysky.skyblocker.skyblock.dwarven;

import java.util.Locale;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public enum CorpseType implements StringIdentifiable {
	LAPIS("LAPIS_ARMOR_HELMET", null, Formatting.BLUE), // dark blue looks bad and these two never exist in same shaft
	UMBER("ARMOR_OF_YOG_HELMET", "UMBER_KEY", Formatting.GOLD),
	TUNGSTEN("MINERAL_HELMET", "TUNGSTEN_KEY", Formatting.GRAY),
	VANGUARD("VANGUARD_HELMET", "SKELETON_KEY", Formatting.AQUA),
	UNKNOWN("UNKNOWN", null, Formatting.RED);

	public static final Codec<CorpseType> CODEC = StringIdentifiable.createCodec(CorpseType::values);
	public final String helmetItemId;
	public final String keyItemId;
	public final Formatting color;

	CorpseType(String helmetItemId, String keyItemId, Formatting color) {
		this.helmetItemId = helmetItemId;
		this.keyItemId = keyItemId;
		this.color = color;
	}

	static CorpseType fromHelmetItemId(String helmetItemId) {
		for (CorpseType value : values()) {
			if (value.helmetItemId.equals(helmetItemId)) {
				return value;
			}
		}
		return UNKNOWN;
	}

	@Override
	public String asString() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	/**
	 * @return the price of the key item for this corpse type
	 * @throws IllegalStateException when there's no price found for the key item, or when the corpse type is UNKNOWN
	 */
	public double getKeyPrice() throws IllegalStateException {
		return switch (this) {
			case UNKNOWN -> throw new IllegalStateException("There's no key or key price for the UNKNOWN corpse type!");
			case LAPIS -> 0; // Lapis corpses don't need a key
			default -> {
				var result = ItemUtils.getItemPrice(keyItemId);
				if (!result.rightBoolean()) throw new IllegalStateException("No price found for key item `" + keyItemId + "`!");
				yield result.leftDouble();
			}
		};
	}

	public static class CorpseTypeArgumentType extends EnumArgumentType<CorpseType> {
		protected CorpseTypeArgumentType() {
			super(CODEC, CorpseType::values);
		}

		static CorpseTypeArgumentType corpseType() {
			return new CorpseTypeArgumentType();
		}

		static <S> CorpseType getCorpseType(CommandContext<S> context, String name) {
			return context.getArgument(name, CorpseType.class);
		}
	}
}
