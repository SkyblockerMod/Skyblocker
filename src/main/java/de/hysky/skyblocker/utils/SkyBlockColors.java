package de.hysky.skyblocker.utils;

import net.minecraft.network.chat.TextColor;

public class SkyBlockColors {
	public static final TextColor DARK_RED = TextColor.fromRgb(0xD13228);
	public static final TextColor GOLD = TextColor.fromRgb(0xFF9000);
	public static final TextColor DARK_BLUE = TextColor.fromRgb(0x353FCE);
	public static final TextColor BLUE = TextColor.fromRgb(0x459BFF);
	public static final TextColor DARK_PURPLE = TextColor.fromRgb(0xA335EE);
	public static final TextColor LIGHT_GRAY = TextColor.fromRgb(0xA8BFD2);
	public static final TextColor GRAY = TextColor.fromRgb(0x707592);
	public static final TextColor YELLOW = TextColor.fromRgb(0xFFDE2F);

	public static int fromVanilla(int colour) {
		return switch (colour) {
			case 0xAA0000 -> DARK_RED.getValue();
			case 0xFFAA00 -> GOLD.getValue();
			case 0x0000AA -> DARK_BLUE.getValue();
			case 0x5555FF -> BLUE.getValue();
			case 0xAA00AA -> DARK_PURPLE.getValue();
			case 0xAAAAAA -> LIGHT_GRAY.getValue();
			case 0x555555 -> GRAY.getValue();
			case 0xFFFF55 -> YELLOW.getValue();
			default -> colour;
		};
	}
}
