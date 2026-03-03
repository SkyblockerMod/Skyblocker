package de.hysky.skyblocker.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Holds generic static constants
 */
public interface Constants {
	String LEVEL_EMBLEMS = "\u2E15\u273F\u2741\u2E19\u03B1\u270E\u2615\u2616\u2663\u213B\u2694\u27B6\u26A1\u2604\u269A\u2693\u2620\u269B\u2666\u2660\u2764\u2727\u238A\u1360\u262C\u269D\u29C9\uA214\u32D6\u2E0E\u26A0\uA541\u3020\u30C4\u2948\u2622\u2623\u273E\u269C\u0BD0\u0A6D\u2742\u16C3\u3023\u10F6\u0444\u266A\u266B\u04C3\u26C1\u26C3\u16DD\u2618\uA598\uA03E\u1C6A\u03A3\u09EB\u2603\u2654\u26C2\u0FC7\uA925\uA56A\u2592\u2600\u2729\u272C\u272D\u272F\u2736\u2733\u2734\u2737\u2738\u2739\u273A\u12DE";
	/**
	 * Pattern for player names in the chat. For tab player names, use {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager#PLAYER_NAME_PATTERN PlayerListManager#PLAYER_NAME_PATTERN}.
	 */
	Pattern PLAYER_NAME = Pattern.compile("(?:\\[[0-9]+\\] )?(?:[" + Constants.LEVEL_EMBLEMS + "] )?(?:\\[[A-Z+]+\\] )?([A-Za-z0-9_]+)");

	Supplier<MutableComponent> PREFIX = () -> {
		if (FunUtils.shouldEnableFun()) {
			return Component.empty().append(Component.literal("[").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("S").withColor(0x00FF4C))
					.append(Component.literal("k").withColor(0x02FA60))
					.append(Component.literal("i").withColor(0x04F574))
					.append(Component.literal("b").withColor(0x07EF88))
					.append(Component.literal("i").withColor(0x09EA9C))
					.append(Component.literal("d").withColor(0x0BE5AF))
					.append(Component.literal("i").withColor(0x0DE0C3))
					.append(Component.literal("b").withColor(0x10DAD7))
					.append(Component.literal("l").withColor(0x12D5EB))
					.append(Component.literal("o").withColor(0x14D0FF))
					.append(Component.literal("c").withColor(0x16CBFF))
					.append(Component.literal("k").withColor(0x18C6FF))
					.append(Component.literal("e").withColor(0x1AC1FF))
					.append(Component.literal("r").withColor(0x1CBBFF))
					.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
		} else if (FunUtils.shouldEnableChristmasFun()) {
			return Component.empty().append(Component.literal("[").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("J").withColor(0x00FF4C))
					.append(Component.literal("o").withColor(0x02FA60))
					.append(Component.literal("l").withColor(0x04F574))
					.append(Component.literal("l").withColor(0x07EF88))
					.append(Component.literal("y").withColor(0x09EA9C))
					.append(Component.literal("b").withColor(0x0BE5AF))
					.append(Component.literal("l").withColor(0x0DE0C3))
					.append(Component.literal("o").withColor(0x10DAD7))
					.append(Component.literal("c").withColor(0x12D5EB))
					.append(Component.literal("k").withColor(0x14D0FF))
					.append(Component.literal("e").withColor(0x16CBFF))
					.append(Component.literal("r").withColor(0x18C6FF))
					.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
		}

		return Component.empty()
				.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
				.append(Component.literal("S").withColor(0x00FF4C))
				.append(Component.literal("k").withColor(0x02FA60))
				.append(Component.literal("y").withColor(0x04F574))
				.append(Component.literal("b").withColor(0x07EF88))
				.append(Component.literal("l").withColor(0x09EA9C))
				.append(Component.literal("o").withColor(0x0BE5AF))
				.append(Component.literal("c").withColor(0x0DE0C3))
				.append(Component.literal("k").withColor(0x10DAD7))
				.append(Component.literal("e").withColor(0x12D5EB))
				.append(Component.literal("r").withColor(0x14D0FF))
				.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
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
	List<String> SPOOK = List.of("000000", "070008", "0E000F", "150017", "1B001F", "220027", "29002E", "300036", "37003E", "3E0046",
			"45004D", "4C0055", "52005D", "590065", "60006C", "670074", "6E007C", "750084", "7C008B", "830093",
			"89009B", "9000A3", "9700AA", "993399", "9E00B2");
	// Exotic - Glitched Hexes
	Map<String, String> MISC_GLITCHED_HEXES = Map.of(
			"FFDC51", "SHARK_SCALE",
			"F7DA33", "FROZEN_BLAZE",
			"606060", "BAT_PERSON");
	Map<String, String> WITHER_CHESTPLATE_HEXES = Map.of(
			"E7413C", "POWER_WITHER_CHESTPLATE",
			"45413C", "TANK_WITHER_CHESTPLATE",
			"4A14B7", "SPEED_WITHER_CHESTPLATE",
			"1793C4", "WISE_WITHER_CHESTPLATE",
			"000000", "WITHER_CHESTPLATE");
	Map<String, String> WITHER_LEGGINGS_HEXES = Map.of(
			"E75C3C", "POWER_WITHER_LEGGINGS",
			"65605A", "TANK_WITHER_LEGGINGS",
			"5D2FB9", "SPEED_WITHER_LEGGINGS",
			"17A8C4", "WISE_WITHER_LEGGINGS",
			"000000", "WITHER_LEGGINGS");
	Map<String, String> WITHER_BOOTS_HEXES = Map.of(
			"E76E3C", "POWER_WITHER_BOOTS",
			"88837E", "TANK_WITHER_BOOTS",
			"8969C8", "SPEED_WITHER_BOOTS",
			"1CD4E4", "WISE_WITHER_BOOTS",
			"000000", "WITHER_BOOTS");
	//All glitched hexes
	List<String> GLITCHED = List.of("FFDC51", "F7DA33", "606060", "E7413C", "45413C", "4A14B7", "1793C4", "000000", "E75C3C", "65605A",
			"5D2FB9", "17A8C4", "E76E3C", "88837E", "8969C8", "1CD4E4"); // Glitched through other means such as Shark Scale upgrade color

	// List of exceptions
	List<String> RANCHERS = List.of("CC5500", "000000", "0");
	List<String> REAPER = List.of("1B1B1B", "FF0000");
	List<String> ADAPTIVE_CHEST = List.of("3ABE78", "82E3D8", "BFBCB2", "D579FF", "FF4242", "FFC234");
	List<String> ADAPTIVE = List.of("169F57", "2AB5A5", "6E00A0", "BB0000", "BFBCB2", "FFF7E6");
}
