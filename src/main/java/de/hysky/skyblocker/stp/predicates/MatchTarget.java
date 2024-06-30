package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

enum MatchTarget implements StringIdentifiable {
	NAME,
	LORE;

	static final Codec<MatchTarget> CODEC = StringIdentifiable.createCodec(MatchTarget::values);

	@Override
	public String asString() {
		return name();
	}
}
