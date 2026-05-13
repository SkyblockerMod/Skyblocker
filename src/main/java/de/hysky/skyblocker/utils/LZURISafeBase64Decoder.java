package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.List;

public class LZURISafeBase64Decoder {
	private static final String KEY_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";

	private int val;
	private int position = 32;
	private int index = 1;
	private final String input;

	private LZURISafeBase64Decoder(String input) {
		this.input = input;
		this.val = KEY_STR.indexOf(input.charAt(0));
	}

	private int readBit() {
		int resb = val & position;
		position >>= 1;

		if (position == 0) {
			position = 32;
			if (index < input.length()) {
				val = KEY_STR.indexOf(input.charAt(index++));
			}
		}

		return resb > 0 ? 1 : 0;
	}

	private int readBits(int numBits) {
		int bits = 0;
		int maxpower = 1 << numBits;
		int power = 1;

		while (power != maxpower) {
			bits |= readBit() * power;
			power <<= 1;
		}

		return bits;
	}

	public static String decodeLZString(String input) {
		if (input == null || input.isEmpty()) return "";
		return new LZURISafeBase64Decoder(input).decode();
	}

	private String decode() {
		List<String> dictionary = new ArrayList<>();
		for (int i = 0; i < 3; i++) dictionary.add("");

		int enlargeIn = 4;
		int dictSize = 4;
		int numBits = 3;

		int next = readBits(2);
		String c;

		switch (next) {
			case 0 -> c = String.valueOf((char) readBits(8));
			case 1 -> c = String.valueOf((char) readBits(16));
			case 2 -> { return ""; }
			default -> throw new IllegalStateException("Invalid LZ string");
		}

		dictionary.add(c);
		String w = c;
		StringBuilder result = new StringBuilder(c);

		while (true) {
			if (index > input.length()) return result.toString();

			int cc = readBits(numBits);
			String entry;

			switch (cc) {
				case 0 -> {
					dictionary.add(String.valueOf((char) readBits(8)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 1 -> {
					dictionary.add(String.valueOf((char) readBits(16)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 2 -> {
					return result.toString();
				}
			}

			if (enlargeIn == 0) {
				enlargeIn = 1 << numBits;
				numBits++;
			}

			if (cc < dictionary.size() && dictionary.get(cc) != null) {
				entry = dictionary.get(cc);
			} else if (cc == dictSize) {
				entry = w + w.charAt(0);
			} else {
				throw new IllegalStateException("Bad compressed code: " + cc);
			}

			result.append(entry);

			dictionary.add(w + entry.charAt(0));
			dictSize++;
			enlargeIn--;

			w = entry;

			if (enlargeIn == 0) {
				enlargeIn = 1 << numBits;
				numBits++;
			}
		}
	}
}
