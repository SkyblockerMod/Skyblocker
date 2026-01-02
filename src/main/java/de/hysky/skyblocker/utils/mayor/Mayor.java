package de.hysky.skyblocker.utils.mayor;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a mayor as retrieved from the API.
 * @param key The key of the mayor.
 * @param name The name of the mayor.
 * @param perks The perks of the mayor.
 */
public record Mayor(String key, String name, List<Perk> perks) {
	/**
	 * An empty mayor. Allows for better null safety.
	 */
	public static final Mayor EMPTY = new Mayor("", "", List.of());
	public static final Codec<Mayor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("key").forGetter(Mayor::key),
			Codec.STRING.fieldOf("name").forGetter(Mayor::name),
			Perk.CODEC.listOf().fieldOf("perks").forGetter(Mayor::perks)
			).apply(instance, Mayor::new));

	/**
	 * For formatting purposes when printing out the result of the API call.
	 * @see MayorUtils#tickMayorCache()
	 */
	@Override
	public String toString() {
		if (isEmpty()) return "Mayor.EMPTY";
		return name;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
