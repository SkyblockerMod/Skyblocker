package de.hysky.skyblocker.utils.data.constants;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record EmblemConstants(List<Emblem> skills, List<Emblem> catacombs, List<Emblem> levelling, List<Emblem> slayer, List<Emblem> achievement, List<Emblem> superstar, List<Emblem> special) {
	public static final Codec<EmblemConstants> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Emblem.LIST_CODEC.fieldOf("skills").forGetter(EmblemConstants::skills),
			Emblem.LIST_CODEC.fieldOf("catacombs").forGetter(EmblemConstants::catacombs),
			Emblem.LIST_CODEC.fieldOf("levelling").forGetter(EmblemConstants::levelling),
			Emblem.LIST_CODEC.fieldOf("slayer").forGetter(EmblemConstants::slayer),
			Emblem.LIST_CODEC.fieldOf("achievement").forGetter(EmblemConstants::achievement),
			Emblem.LIST_CODEC.fieldOf("superstar").forGetter(EmblemConstants::superstar),
			Emblem.LIST_CODEC.fieldOf("special").forGetter(EmblemConstants::special)
			).apply(instance, EmblemConstants::new));
	public static final EmblemConstants EMPTY = new EmblemConstants(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

	public List<Emblem> all() {
		return List.of(this.skills(), this.catacombs(), this.levelling(), this.slayer(), this.achievement(), this.superstar(), this.special()).stream()
				.flatMap(List::stream)
				.toList();
	}

	public Optional<Emblem> fromId(String id) {
		return all().stream()
				.filter(emblem -> emblem.id().equals(id))
				.findFirst();
	}

	public record Emblem(String id, String name, Component display) {
		public static final Codec<Emblem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Emblem::id),
				Codec.STRING.fieldOf("name").forGetter(Emblem::name),
				ComponentSerialization.CODEC.fieldOf("display").forGetter(Emblem::display)
				).apply(instance, Emblem::new));
		public static final Codec<List<Emblem>> LIST_CODEC = CODEC.listOf();
	}
}
