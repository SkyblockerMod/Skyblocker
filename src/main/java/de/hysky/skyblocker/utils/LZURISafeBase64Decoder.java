package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.List;

public class LZURISafeBase64Decoder {
	public static String decodeLZString(String input) {
		if (input == null || input.isEmpty()) return "";

		final String keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";

		class Data {
			int val;
			int position = 32;
			int index = 1;

			Data(String input) {
				this.val = keyStr.indexOf(input.charAt(0));
			}

			int readBit(String input) {
				int resb = val & position;
				position >>= 1;

				if (position == 0) {
					position = 32;
					if (index < input.length()) {
						val = keyStr.indexOf(input.charAt(index++));
					}
				}

				return resb > 0 ? 1 : 0;
			}

			int readBits(String input, int numBits) {
				int bits = 0;
				int maxpower = 1 << numBits;
				int power = 1;

				while (power != maxpower) {
					bits |= readBit(input) * power;
					power <<= 1;
				}

				return bits;
			}
		}

		Data data = new Data(input);

		List<String> dictionary = new ArrayList<>();
		for (int i = 0; i < 3; i++) dictionary.add("");

		int enlargeIn = 4;
		int dictSize = 4;
		int numBits = 3;

		int next = data.readBits(input, 2);
		String c;

		switch (next) {
			case 0 -> c = String.valueOf((char) data.readBits(input, 8));
			case 1 -> c = String.valueOf((char) data.readBits(input, 16));
			case 2 -> { return ""; }
			default -> throw new IllegalStateException("Invalid LZ string");
		}

		dictionary.add(c);
		String w = c;
		StringBuilder result = new StringBuilder(c);

		while (true) {
			if (data.index > input.length()) return result.toString();

			int cc = data.readBits(input, numBits);
			String entry;

			switch (cc) {
				case 0 -> {
					dictionary.add(String.valueOf((char) data.readBits(input, 8)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 1 -> {
					dictionary.add(String.valueOf((char) data.readBits(input, 16)));
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
