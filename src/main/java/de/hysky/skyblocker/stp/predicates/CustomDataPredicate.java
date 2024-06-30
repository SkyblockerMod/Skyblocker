package de.hysky.skyblocker.stp.predicates;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.stp.matchers.DoubleMatcher;
import de.hysky.skyblocker.stp.matchers.FloatMatcher;
import de.hysky.skyblocker.stp.matchers.IntMatcher;
import de.hysky.skyblocker.stp.matchers.LongMatcher;
import de.hysky.skyblocker.stp.matchers.RegexMatcher;
import de.hysky.skyblocker.stp.matchers.StringMatcher;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

/**
 * Allows for matching a specific value on some data inside of an item's custom data by specifying
 * a {@code path} to it. Each compound name must be separated by a dot (.), if the field lives directly under the
 * custom data then just put the field name in as the path.<br><br>
 * 
 * Example Paths: {@code drill_data.fuel}, {@code dye_item}, {@code something.is.here}
 */
public record CustomDataPredicate(String[] path, Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher, Optional<IntMatcher> intMatcher, Optional<LongMatcher> longMatcher, Optional<FloatMatcher> floatMatcher, Optional<DoubleMatcher> doubleMatcher) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "custom_data");
	public static final Codec<CustomDataPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.validate(CustomDataPredicate::validatePath).fieldOf("path").forGetter(CustomDataPredicate::pathString),
			StringMatcher.CODEC.optionalFieldOf("stringMatcher").forGetter(CustomDataPredicate::stringMatcher),
			RegexMatcher.CODEC.optionalFieldOf("regexMatcher").forGetter(CustomDataPredicate::regexMatcher),
			IntMatcher.CODEC.optionalFieldOf("intMatcher").forGetter(CustomDataPredicate::intMatcher),
			LongMatcher.CODEC.optionalFieldOf("longMatcher").forGetter(CustomDataPredicate::longMatcher),
			FloatMatcher.CODEC.optionalFieldOf("floatMatcher").forGetter(CustomDataPredicate::floatMatcher),
			DoubleMatcher.CODEC.optionalFieldOf("doubleMatcher").forGetter(CustomDataPredicate::doubleMatcher))
			.apply(instance, CustomDataPredicate::new));

	private CustomDataPredicate(String path, Optional<StringMatcher> stringMatcher, Optional<RegexMatcher> regexMatcher, Optional<IntMatcher> intMatcher, Optional<LongMatcher> longMatcher, Optional<FloatMatcher> floatMatcher, Optional<DoubleMatcher> doubleMatcher) {
		this(path.split("\\."), stringMatcher, regexMatcher, intMatcher, longMatcher, floatMatcher, doubleMatcher);
	}

	public String pathString() {
		return String.join(".", path);
	}

	@Override
	public boolean test(ItemStack stack) {
		NbtElement element = findElement(ItemUtils.getCustomData(stack));

		//The path was valid - the instanceof also acts here as an implicit not null check
		if (regexMatcher.isPresent() && (element instanceof NbtString || element instanceof AbstractNbtNumber)) {
			String stringified = element.asString();

			return regexMatcher.get().test(stringified);
		}

		return switch (element) {
			case NbtString nbtString when stringMatcher.isPresent() -> stringMatcher.get().test(nbtString.asString());

			//Int-like types
			case NbtByte nbtByte when intMatcher.isPresent() -> intMatcher.get().test(nbtByte.intValue());
			case NbtShort nbtShort when intMatcher.isPresent() -> intMatcher.get().test(nbtShort.intValue());
			case NbtInt nbtInt when intMatcher.isPresent() -> intMatcher.get().test(nbtInt.intValue());

			//Longs
			case NbtLong nbtLong when longMatcher.isPresent() -> longMatcher.get().test(nbtLong.longValue());

			//Floating Point Types
			case NbtFloat nbtFloat when floatMatcher.isPresent() -> floatMatcher.get().test(nbtFloat.floatValue());
			case NbtDouble nbtDouble when doubleMatcher.isPresent() -> doubleMatcher.get().test(nbtDouble.doubleValue());

			case null, default -> false;
		};
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.CUSTOM_DATA;
	}

	private NbtElement findElement(NbtCompound customData) {
		String[] split = path;
		String finalElementName = split[split.length - 1];

		//Fast path for direct field lookup - avoids the other allocations/memory copies
		if (split.length == 1) {
			return customData.get(finalElementName);
		}

		String[] compounds2Traverse = new String[split.length - 1];

		//Copy the traversal path into that array
		System.arraycopy(split, 0, compounds2Traverse, 0, split.length - 1);

		NbtCompound compound = customData;

		//Hopefully arrive at the end
		for (String compoundName : compounds2Traverse) {
			compound = compound.getCompound(compoundName);
		}

		return compound.get(finalElementName);
	}

	/**
	 * Validates that the path is not empty.
	 */
	private static DataResult<String> validatePath(String path) {
		return switch (path) {
			case String s when s.isEmpty() -> DataResult.error(() -> "Path must not be empty!");
			case String s when s.startsWith(".") || s.endsWith(".") -> DataResult.error(() -> "Path must not start or end with a \".\"!");

			default -> DataResult.success(path);
		};
	}
}
