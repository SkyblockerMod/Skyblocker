package de.hysky.skyblocker.utils.mayor;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;

public record ElectionCandidate(String key, String name, List<CandidatePerk> perks, int votes) {
	public static final Codec<ElectionCandidate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("key").forGetter(ElectionCandidate::key),
		Codec.STRING.fieldOf("name").forGetter(ElectionCandidate::name),
		CandidatePerk.CODEC.listOf().fieldOf("perks").forGetter(ElectionCandidate::perks),
		Codec.INT.fieldOf("votes").forGetter(ElectionCandidate::votes)
	).apply(instance, ElectionCandidate::new));

	public boolean hasPerk(String perkName, @Nullable String descriptionContains, boolean ministerOnly) {
		return perks.stream().anyMatch(
				perk -> (!ministerOnly || perk.minister())
								&& perk.name().equals(perkName)
								&& (descriptionContains == null || ChatFormatting.stripFormatting(perk.description()).contains(descriptionContains)));
	}
}
