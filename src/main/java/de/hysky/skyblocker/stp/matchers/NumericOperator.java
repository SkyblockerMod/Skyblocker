package de.hysky.skyblocker.stp.matchers;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

enum NumericOperator implements StringIdentifiable {
	EQUAL("=="),
	NOT_EQUAL("!="),
	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL_TO("<="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUAL_TO(">=");

	static final Codec<NumericOperator> CODEC = StringIdentifiable.createBasicCodec(NumericOperator::values);

	private final String javaOperator;

	NumericOperator(String javaOperator) {
		this.javaOperator = javaOperator;
	}

	@Override
	public String asString() {
		return javaOperator;
	}
}