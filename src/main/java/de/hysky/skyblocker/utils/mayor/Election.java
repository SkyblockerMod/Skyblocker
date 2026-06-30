package de.hysky.skyblocker.utils.mayor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Comparator;
import java.util.List;

public record Election(int year, List<ElectionCandidate> candidates) {
	public static final Codec<Election> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("year").forGetter(Election::year),
			ElectionCandidate.CODEC.listOf().fieldOf("candidates").forGetter(Election::candidates)
	).apply(instance, Election::new));

	public int totalVotes() {
		return candidates.stream().mapToInt(ElectionCandidate::votes).sum();
	}

	public ElectionCandidate mostVotes() {
		return candidates.stream().max(Comparator.comparingInt(ElectionCandidate::votes)).orElseThrow();
	}

	public ElectionCandidate secondMostVotes() {
		return candidates.stream().sorted(Comparator.comparingInt(ElectionCandidate::votes)).skip(1).findFirst().orElseThrow();
	}
}
