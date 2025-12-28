package de.hysky.skyblocker.utils.mayor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a minister as retrieved from the API.
 * @param key The key of the minister.
 * @param name The name of the minister.
 * @param perk The perk of the minister.
 */
public record Minister(String key, String name, Perk perk) {
	/**
	 * An empty minister. Allows for better null safety.
	 */
	public static final Minister EMPTY = new Minister("", "", Perk.EMPTY);
	public static final Codec<Minister> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("key").forGetter(Minister::key),
			Codec.STRING.fieldOf("name").forGetter(Minister::name),
			Perk.CODEC.fieldOf("perk").forGetter(Minister::perk)
			).apply(instance, Minister::new));

	/**
	 * For formatting purposes when printing out the result of the API call.
	 * @see MayorUtils#tickMayorCache()
	 */
	@Override
	public String toString() {
		if (isEmpty()) return "Minister.EMPTY";
		return name;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
