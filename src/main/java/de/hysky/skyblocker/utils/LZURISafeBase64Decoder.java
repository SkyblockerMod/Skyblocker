package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.List;

public class LZURISafeBase64Decoder {
	private static final String KEY_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";

	private static int readBit(String input, int[] state) {
		int resb = state[0] & state[1];
		state[1] >>= 1;

		if (state[1] == 0) {
			state[1] = 32;
			if (state[2] < input.length()) {
				state[0] = KEY_STR.indexOf(input.charAt(state[2]++));
			}
		}

		return resb > 0 ? 1 : 0;
	}

	private static int readBits(int numBits, String input, int[] state) {
		int bits = 0;
		int maxpower = 1 << numBits;
		int power = 1;

		while (power != maxpower) {
			bits |= readBit(input, state) * power;
			power <<= 1;
		}

		return bits;
	}

	public static String decodeLZString(String input) {
		if (input == null || input.isEmpty()) return "";

		// state[0]: val, state[1]: position, state[2]: index
		int[] state = { KEY_STR.indexOf(input.charAt(0)), 32, 1 };

		List<String> dictionary = new ArrayList<>();
		for (int i = 0; i < 3; i++) dictionary.add("");

		int enlargeIn = 4;
		int dictSize = 4;
		int numBits = 3;

		int next = readBits(2, input, state);
		String c;

		switch (next) {
			case 0 -> c = String.valueOf((char) readBits(8, input, state));
			case 1 -> c = String.valueOf((char) readBits(16, input, state));
			case 2 -> { return ""; }
			default -> throw new IllegalStateException("Invalid LZ string");
		}

		dictionary.add(c);
		String w = c;
		StringBuilder result = new StringBuilder(c);

		while (true) {
			if (state[2] > input.length()) return result.toString();

			int cc = readBits(numBits, input, state);
			String entry;

			switch (cc) {
				case 0 -> {
					dictionary.add(String.valueOf((char) readBits(8, input, state)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 1 -> {
					dictionary.add(String.valueOf((char) readBits(16, input, state)));
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
