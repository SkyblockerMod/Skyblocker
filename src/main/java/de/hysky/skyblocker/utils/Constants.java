package de.hysky.skyblocker.utils;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * Holds generic static constants
 */
public interface Constants {
	String LEVEL_EMBLEMS = "\u2E15\u273F\u2741\u2E19\u03B1\u270E\u2615\u2616\u2663\u213B\u2694\u27B6\u26A1\u2604\u269A\u2693\u2620\u269B\u2666\u2660\u2764\u2727\u238A\u1360\u262C\u269D\u29C9\uA214\u32D6\u2E0E\u26A0\uA541\u3020\u30C4\u2948\u2622\u2623\u273E\u269C\u0BD0\u0A6D\u2742\u16C3\u3023\u10F6\u0444\u266A\u266B\u04C3\u26C1\u26C3\u16DD\uA03E\u1C6A\u03A3\u09EB\u2603\u2654\u26C2\u12DE";
	IntFunction<UnaryOperator<Style>> WITH_COLOR = color -> style -> style.withColor(color);
	Supplier<MutableText> PREFIX = () -> Text.empty()
			.append(Text.literal("[").styled(WITH_COLOR.apply(0x8c8c8c)))
			.append(Text.literal("S").styled(WITH_COLOR.apply(0x00ff4c)))
			.append(Text.literal("k").styled(WITH_COLOR.apply(0x02fa60)))
			.append(Text.literal("y").styled(WITH_COLOR.apply(0x04f574)))
			.append(Text.literal("b").styled(WITH_COLOR.apply(0x07ef88)))
			.append(Text.literal("l").styled(WITH_COLOR.apply(0x09ea9c)))
			.append(Text.literal("o").styled(WITH_COLOR.apply(0x0be5af)))
			.append(Text.literal("c").styled(WITH_COLOR.apply(0x0de0c3)))
			.append(Text.literal("k").styled(WITH_COLOR.apply(0x10dad7)))
			.append(Text.literal("e").styled(WITH_COLOR.apply(0x12d5eb)))
			.append(Text.literal("r").styled(WITH_COLOR.apply(0x14d0ff)))
			.append(Text.literal("] ").styled(WITH_COLOR.apply(0x8c8c8c)));
}
