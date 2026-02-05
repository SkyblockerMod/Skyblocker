package de.hysky.skyblocker.utils.mayor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a mayor perk.
 * @param name The name of the perk.
 * @param description The description of the perk. This will include the formatting codes that are used in the game.
 */
public record Perk(String name, String description) {
	/**
	 * An empty perk.
	 * Represents a perk that does not exist for ministers.
	 * Allows for better null safety.
	 */
	public static final Perk EMPTY = new Perk("", "");
	public static final Codec<Perk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(Perk::name),
			Codec.STRING.fieldOf("description").forGetter(Perk::description)
			).apply(instance, Perk::new));
}
