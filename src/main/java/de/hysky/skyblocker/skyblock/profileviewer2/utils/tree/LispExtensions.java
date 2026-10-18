package de.hysky.skyblocker.skyblock.profileviewer2.utils.tree;

import com.mojang.serialization.Codec;
import moe.nea.lisp.LispAst;
import moe.nea.lisp.LispData;
import moe.nea.lisp.LispParser;
import moe.nea.lisp.bind.LispBinding;

import de.hysky.skyblocker.utils.Formatters;

public class LispExtensions {
	public static final Codec<LispAst.Program> PROGRAM_CODEC = Codec.STRING.xmap(source -> LispParser.Companion.parse("<codec>", source), LispAst.Program::toSource);

	protected LispExtensions() {}

	@LispBinding(name = "pow")
	public LispData.LispNumber pow(double base, double exponent) {
		return new LispData.LispNumber(Math.pow(base, exponent));
	}

	@LispBinding(name = "round")
	public LispData.LispNumber round(double number) {
		return new LispData.LispNumber(Math.round(number));
	}

	@LispBinding(name = "ceil")
	public LispData.LispNumber ceil(double number) {
		return new LispData.LispNumber(Math.ceil(number));
	}

	@LispBinding(name = "floor")
	public LispData.LispNumber floor(double number) {
		return new LispData.LispNumber(Math.floor(number));
	}

	@LispBinding(name = "format-int")
	public LispData.LispString formatInt(int number) {
		return new LispData.LispString(Formatters.INTEGER_NUMBERS.format(number));
	}
}
