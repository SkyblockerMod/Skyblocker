package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.SkyblockerMod;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public enum FilterOption implements Supplier<ResourceLocation>, Predicate<String> {

	ALL(query -> true, SkyblockerMod.id("textures/gui/filter/all.png")),
	ENTITIES(query -> query.endsWith("(monster)") || query.endsWith("(miniboss)") || query.endsWith("(boss)")
			|| query.endsWith("(animal)") || query.endsWith("(pest)") || query.endsWith("(sea creature)"),
			SkyblockerMod.id("textures/gui/filter/entities.png")),
	NPCS(query -> query.endsWith("(npc)") || query.endsWith("(rift npc)"), SkyblockerMod.id("textures/gui/filter/npcs.png")),
	MAYORS(query -> query.endsWith("(mayor)") || query.endsWith("(retired mayor)"), SkyblockerMod.id("textures/gui/filter/mayors.png")),

	// Basically a negation on everything else.
	ITEMS(query -> !ENTITIES.test(query) && !NPCS.test(query) && !MAYORS.test(query),
			SkyblockerMod.id("textures/gui/filter/items.png"));

	private	final Predicate<String> matchingPredicate;
	private final ResourceLocation texture;

	FilterOption(Predicate<String> matchingPredicate, ResourceLocation texture) {
		this.matchingPredicate = matchingPredicate;
		this.texture = texture;
	}

	@Override
	public boolean test(String query) {
		return matchingPredicate.test(query);
	}

	@Override
	public ResourceLocation get() {
		return texture;
	}
}
