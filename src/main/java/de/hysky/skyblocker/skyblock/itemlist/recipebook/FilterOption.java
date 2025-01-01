package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.utils.Identifiable;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public enum FilterOption implements Identifiable {

	ALL(query -> true, Identifier.of("skyblocker", "textures/gui/filter/all.png")),
	ENTITIES(query -> query.endsWith("(monster)") || query.endsWith("(miniboss)") || query.endsWith("(boss)")
			|| query.endsWith("(animal)") || query.endsWith("(pest)") || query.endsWith("(sea creature)"),
			Identifier.of("skyblocker", "textures/gui/filter/entities.png")),
	NPCS(query -> query.endsWith("(npc)") || query.endsWith("(rift npc)"), Identifier.of("skyblocker", "textures/gui/filter/npcs.png")),
	MAYORS(query -> query.endsWith("(mayor)") || query.endsWith("(retired mayor)"), Identifier.of("skyblocker", "textures/gui/filter/mayors.png")),

	// Basically a negation on everything else.
	ITEMS(query -> !ENTITIES.matches(query) && !NPCS.matches(query) && !MAYORS.matches(query),
			Identifier.of("skyblocker", "textures/gui/filter/items.png"));

	final Predicate<String> matchingPredicate;
	final Identifier texture;

	FilterOption(Predicate<String> matchingPredicate, Identifier texture) {
		this.matchingPredicate = matchingPredicate;
		this.texture = texture;
	}

	public boolean matches(String query) {
		return matchingPredicate.test(query);
	}

	@Override
	public Identifier identify() {
		return texture;
	}
}
