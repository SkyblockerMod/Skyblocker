package de.hysky.skyblocker.utils;

import it.unimi.dsi.fastutil.chars.CharList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains utilities for transforming text. These methods are from Aaron's Mod.
 *
 * @author AzureAaron
 */
public class TextTransformer {
	private static final Pattern REDUNDANT_COLOR_REGEX = Pattern.compile("&(?:#[\\da-f]{6}|[\\da-f])(\\s*)&(#[\\da-f]{6}|[\\da-f])");
	private static final CharList FORMAT_CODES = CharList.of('4', 'c', '6', 'e', '2', 'a', 'b', '3', '1', '9', 'd', '5', 'f', '7', '8', '0', 'r', 'k', 'l', 'm', 'n', 'o');
	private static final Map<Integer, Character> HEX_TO_CODES = Arrays.stream(ChatFormatting.values()).filter(c -> c.getColor() != null).collect(Collectors.toUnmodifiableMap(ChatFormatting::getColor, ChatFormatting::getChar));
	private static final List<Map.Entry<Predicate<Style>, Character>> PREDICATE_FORMAT_LIST = List.of(
			Map.entry(Style::isBold, 'l'),
			Map.entry(Style::isItalic, 'o'),
			Map.entry(Style::isObfuscated, 'k'),
			Map.entry(Style::isUnderlined, 'n'),
			Map.entry(Style::isStrikethrough, 'm')
	);

	private static String getCodes(Style style, Style previous) {
		final var codes = new StringJoiner("&", "&", "").setEmptyValue("");
		boolean hasReset = false;
		final boolean needsReset = previous.getColor() != null && style.getColor() == null ||
				PREDICATE_FORMAT_LIST.stream().anyMatch(x -> x.getKey().test(previous) && !x.getKey().test(style));

		if (needsReset && style.getColor() == null) {
			codes.add("r");
			hasReset = true;
		} else if (style.getColor() != null && (needsReset || !Objects.equals(previous.getColor(), style.getColor()))) {
			final Character code = HEX_TO_CODES.get(style.getColor().getValue());

			if (code == null) {
				// Adds non-standard hex colors as &#RRGGBB
				codes.add(style.getColor().formatValue());
			} else {
				codes.add("" + code);
			}

			hasReset = true;
		}

		final boolean alreadyReset = hasReset;
		PREDICATE_FORMAT_LIST.stream()
				.filter(x -> x.getKey().test(style) && (alreadyReset || !x.getKey().test(previous)))
				.map(Map.Entry::getValue)
				.forEach(c -> codes.add("" + c));

		return codes.toString();
	}

	private static String removeRedundantCodes(String message) {
		final var result = new StringBuilder();
		Matcher redundant = REDUNDANT_COLOR_REGEX.matcher(message);

		while (redundant.find()) {
			redundant.appendReplacement(result, "$1&$2");
		}

		return redundant.appendTail(result).toString();
	}

	/**
	 * Converts message components to strings containing legacy formatting codes.
	 *
	 * @author LJNeon
	 *
	 * @param component The chat component to be transformed
	 * @return A string matching the exact formatting of the input component
	 */
	public static String toLegacy(Component component) {
		final var result = new StringBuilder();
		final var previous = new AtomicReference<>(Style.EMPTY);

		component.visit((style, string) -> {
			result.append(getCodes(style, previous.get()));
			result.append(string.replace('§', '&'));
			previous.set(style);

			return Optional.empty();
		}, Style.EMPTY);

		return removeRedundantCodes(result.toString());
	}

	/**
	 * Converts strings with section symbol/legacy formatting to MutableText objects.
	 *
	 * @author AzureAaron
	 *
	 * @param legacy The string with legacy formatting to be transformed
	 * @return A {@link MutableComponent} object matching the exact formatting of the input
	 */
	public static MutableComponent fromLegacy(String legacy) {
		return fromLegacy(legacy, '§', true);
	}

	/**
	 * Converts strings with section symbol/legacy formatting to MutableText objects.
	 *
	 * @author AzureAaron
	 *
	 * @param legacy The string with legacy formatting to be transformed
	 * @param legacyPrefix The character that prefixes the legacy formatting codes (e.g., '§' or '&')
	 * @param override Whether to override the parent style by defaulting to false instead of null for bold, italic, underline, strikethrough, and obfuscated properties.
	 *                 This is required to be true for item name and lore texts, or else the parent style will make the name and lore texts italic.
	 * @return A {@link MutableComponent} object matching the exact formatting of the input
	 */
	public static MutableComponent fromLegacy(String legacy, char legacyPrefix, boolean override) {
		MutableComponent newText = Component.empty();
		StringBuilder builder = new StringBuilder();
		ChatFormatting formatting = null;
		Boolean bold = override ? false : null;
		Boolean italic = override ? false : null;
		Boolean underline = override ? false : null;
		Boolean strikethrough = override ? false : null;
		Boolean obfuscated = override ? false : null;

		for (int i = 0; i < legacy.length(); i++) {
			//If we've encountered a new formatting code then append the text from the previous "sequence" and reset state
			if (i != 0 && legacy.charAt(i - 1) == legacyPrefix && FORMAT_CODES.contains(Character.toLowerCase(legacy.charAt(i))) && !builder.isEmpty()) {
				newText.append(Component.literal(builder.toString()).setStyle(Style.EMPTY
						.withColor(formatting)
						.withBold(bold)
						.withItalic(italic)
						.withUnderlined(underline)
						.withStrikethrough(strikethrough)
						.withObfuscated(obfuscated)));

				//Erase all characters in the builder so we can reuse it, also clear formatting
				//Note that this resets all formatting when encountering any new formatting code, not just when encountering a new color code,
				//due to some weird formatting from hypixel such as the soulbound text
				builder.delete(0, builder.length());
				formatting = null;
				bold = override ? false : null;
				italic = override ? false : null;
				underline = override ? false : null;
				strikethrough = override ? false : null;
				obfuscated = override ? false : null;
			}

			if (i != 0 && legacy.charAt(i - 1) == legacyPrefix) {
				ChatFormatting fmt = ChatFormatting.getByCode(legacy.charAt(i));

				switch (fmt) {
					case BOLD -> bold = true;
					case ITALIC -> italic = true;
					case UNDERLINE -> underline = true;
					case STRIKETHROUGH -> strikethrough = true;
					case OBFUSCATED -> obfuscated = true;

					case null, default -> formatting = fmt;
				}

				continue;
			}

			//This character isn't the start of a formatting sequence or this character isn't part of a formatting sequence
			if (legacy.charAt(i) != legacyPrefix && (i == 0 || legacy.charAt(i - 1) != legacyPrefix)) {
				builder.append(legacy.charAt(i));
			}

			// We've read the last character so append the last text with all the formatting
			if (i == legacy.length() - 1) {
				newText.append(Component.literal(builder.toString()).setStyle(Style.EMPTY
						.withColor(formatting)
						.withBold(bold)
						.withItalic(italic)
						.withUnderlined(underline)
						.withStrikethrough(strikethrough)
						.withObfuscated(obfuscated)));
			}
		}
		return newText;
	}
}
