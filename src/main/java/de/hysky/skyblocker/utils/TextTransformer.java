package de.hysky.skyblocker.utils;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.chars.CharList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Contains utilities for transforming text. These methods are from Aaron's Mod.
 *
 * @author AzureAaron
 */
public class TextTransformer {
	private static final CharList FORMAT_CODES = CharList.of('4', 'c', '6', 'e', '2', 'a', 'b', '3', '1', '9', 'd', '5', 'f', '7', '8', '0', 'r', 'k', 'l', 'm', 'n', 'o');

	/**
	 * Converts strings with section symbol/legacy formatting to MutableText objects.
	 *
	 * @author AzureAaron
	 *
	 * @param legacy The string with legacy formatting to be transformed
	 * @return A {@link MutableText} object matching the exact formatting of the input
	 */
	public static MutableText fromLegacy(@NotNull String legacy) {
		MutableText newText = Text.empty();
		StringBuilder builder = new StringBuilder();
		Formatting formatting = null;
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		boolean strikethrough = false;
		boolean obfuscated = false;

		for (int i = 0; i < legacy.length(); i++) {
			//If we've encountered a new formatting code then append the text from the previous "sequence" and reset state
			if (i != 0 && legacy.charAt(i - 1) == '§' && FORMAT_CODES.contains(Character.toLowerCase(legacy.charAt(i))) && !builder.isEmpty()) {
				newText.append(Text.literal(builder.toString()).setStyle(Style.EMPTY
						.withColor(formatting)
						.withBold(bold)
						.withItalic(italic)
						.withUnderline(underline)
						.withStrikethrough(strikethrough)
						.withObfuscated(obfuscated)));

				//Erase all characters in the builder so we can reuse it, also clear formatting
				builder.delete(0, builder.length());
				formatting = null;
				bold = false;
				italic = false;
				underline = false;
				strikethrough = false;
				obfuscated = false;
			}

			if (i != 0 && legacy.charAt(i - 1) == '§') {
				Formatting fmt = Formatting.byCode(legacy.charAt(i));

				switch (fmt) {
					case BOLD -> bold = true;
					case ITALIC -> italic = true;
					case UNDERLINE -> underline = true;
					case STRIKETHROUGH -> strikethrough = true;
					case OBFUSCATED -> obfuscated = true;

					default -> formatting = fmt;
				}

				continue;
			}

			//This character isn't the start of a formatting sequence or this character isn't part of a formatting sequence
			if (legacy.charAt(i) != '§' && (i == 0 || (i != 0 && legacy.charAt(i - 1) != '§'))) {
				builder.append(legacy.charAt(i));
			}

			// We've read the last character so append the last text with all of the formatting
			if (i == legacy.length() - 1) {
				newText.append(Text.literal(builder.toString()).setStyle(Style.EMPTY
						.withColor(formatting)
						.withBold(bold)
						.withItalic(italic)
						.withUnderline(underline)
						.withStrikethrough(strikethrough)
						.withObfuscated(obfuscated)));
			}
		}
		return newText;
	}
}
