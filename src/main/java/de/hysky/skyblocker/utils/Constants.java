package de.hysky.skyblocker.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Holds generic static constants
 */
public interface Constants {
	String LEVEL_EMBLEMS = "\u2E15\u273F\u2741\u2E19\u03B1\u270E\u2615\u2616\u2663\u213B\u2694\u27B6\u26A1\u2604\u269A\u2693\u2620\u269B\u2666\u2660\u2764\u2727\u238A\u1360\u262C\u269D\u29C9\uA214\u32D6\u2E0E\u26A0\uA541\u3020\u30C4\u2948\u2622\u2623\u273E\u269C\u0BD0\u0A6D\u2742\u16C3\u3023\u10F6\u0444\u266A\u266B\u04C3\u26C1\u26C3\u16DD\uA03E\u1C6A\u03A3\u09EB\u2603\u2654\u26C2\u0FC7\uA925\uA56A\u2592\u12DE";
	Supplier<MutableText> PREFIX = () -> {
		LocalDate time = LocalDate.now();
		return Text.empty()
				.append(Text.literal("[").formatted(Formatting.GRAY))
				.append(Text.literal("S").withColor(0x00ff4c))
				.append(Text.literal("k").withColor(0x02fa60))
				.append(Text.literal(time.getMonthValue() == 4 && time.getDayOfMonth() == 1 ? "i" : "y").withColor(0x04f574))
				.append(Text.literal("b").withColor(0x07ef88))
				.append(Text.literal("l").withColor(0x09ea9c))
				.append(Text.literal("o").withColor(0x0be5af))
				.append(Text.literal("c").withColor(0x0de0c3))
				.append(Text.literal("k").withColor(0x10dad7))
				.append(Text.literal("e").withColor(0x12d5eb))
				.append(Text.literal("r").withColor(0x14d0ff))
				.append(Text.literal("] ").formatted(Formatting.GRAY));
	};


	List<String> SEYMOUR_IDS = List.of("VELVET_TOP_HAT", "CASHMERE_JACKET", "SATIN_TROUSERS", "OXFORD_SHOES");

	// Exotic Hexes
	List<String> CRYSTAL_HEXES = List.of("1F0030", "46085E", "54146E", "5D1C78", "63237D", "6A2C82", "7E4196", "8E51A6", "9C64B3", "A875BD",
			"B88BC9", "C6A3D4", "D9C1E3", "E5D1ED", "EFE1F5", "FCF3FF");
	List<String> FAIRY_HEXES = List.of("330066", "4C0099", "660033", "660066", "6600CC", "7F00FF", "99004C", "990099", "9933FF", "B266FF",
			"CC0066", "CC00CC", "CC99FF", "E5CCFF", "FF007F", "FF00FF", "FF3399", "FF33FF", "FF66B2", "FF66FF", "FF99CC", "FF99FF", "FFCCE5",
			"FFCCFF");
	List<String> OG_FAIRY_HEXES = List.of("FF99FF", "FFCCFF", "E5CCFF", "CC99FF", "CC00CC", "FF00FF", "FF33FF", "FF66FF",
			"B266FF", "9933FF", "7F00FF", "660066", "6600CC", "4C0099", "330066", "990099", "660033", "99004C", "CC0066",
			"660033", "99004C", "FFCCE5", "660033", "FFCCE5", "FF99CC", "FFCCE5", "FF99CC", "FF66B2");
	List<String> GLITCHED = List.of("FFDC51", "F7DA33", "606060", "E7413C", "45413C", "4A14B7", "1793C4", "000000", "E75C3C", "65605A",
			"5D2FB9", "17A8C4", "E76E3C", "88837E", "8969C8", "1CD4E4"); // Glitched through other means such as Shark Scale upgrade color
	List<String> SPOOK = List.of("000000", "070008", "0E000F", "150017", "1B001F", "220027", "29002E", "300036", "37003E", "3E0046",
			"45004D", "4C0055", "52005D", "590065", "60006C", "670074", "6E007C", "750084", "7C008B", "830093",
			"89009B", "9000A3", "9700AA", "993399", "9E00B2");

	// List of exceptions
	List<String> RANCHERS = List.of("CC5500", "000000", "0");
	List<String> REAPER = List.of("1B1B1B", "FF0000");
	List<String> ADAPTIVE_CHEST = List.of("3ABE78", "82E3D8", "BFBCB2", "D579FF", "FF4242", "FFC234");
	List<String> ADAPTIVE = List.of("169F57", "2AB5A5", "6E00A0", "BB0000", "BFBCB2", "FFF7E6");
}
