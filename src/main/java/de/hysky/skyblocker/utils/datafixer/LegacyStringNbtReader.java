package de.hysky.skyblocker.utils.datafixer;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

/**
 * Implementation of {@link TagParser} that is capable of reading the legacy SNBT format of 1.8.
 */
public class LegacyStringNbtReader {
	public static final SimpleCommandExceptionType TRAILING = new SimpleCommandExceptionType(Component.translatable("argument.nbt.trailing"));
	public static final SimpleCommandExceptionType EXPECTED_KEY = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.key"));
	public static final SimpleCommandExceptionType EXPECTED_VALUE = new SimpleCommandExceptionType(Component.translatable("argument.nbt.expected.value"));
	public static final Dynamic2CommandExceptionType LIST_MIXED = new Dynamic2CommandExceptionType(
		(receivedType, expectedType) -> Component.translatableEscape("argument.nbt.list.mixed", receivedType, expectedType)
	);
	public static final Dynamic2CommandExceptionType ARRAY_MIXED = new Dynamic2CommandExceptionType(
		(receivedType, expectedType) -> Component.translatableEscape("argument.nbt.array.mixed", receivedType, expectedType)
	);
	public static final DynamicCommandExceptionType ARRAY_INVALID = new DynamicCommandExceptionType(
		type -> Component.translatableEscape("argument.nbt.array.invalid", type)
	);
	private static final Pattern DOUBLE_PATTERN_IMPLICIT = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", Pattern.CASE_INSENSITIVE);
	private static final Pattern DOUBLE_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
	private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", Pattern.CASE_INSENSITIVE);
	private static final Pattern BYTE_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", Pattern.CASE_INSENSITIVE);
	private static final Pattern LONG_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", Pattern.CASE_INSENSITIVE);
	private static final Pattern SHORT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", Pattern.CASE_INSENSITIVE);
	private static final Pattern INT_PATTERN = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
	public final StringReader reader;

	public LegacyStringNbtReader(StringReader reader) {
		this.reader = reader;
	}

	/**
	 * {@return the NBT compound parsed from the {@code string}}
	 *
	 * @throws CommandSyntaxException if the reader detects a syntax error (including
	 * {@linkplain #TRAILING trailing strings})
	 */
	public static CompoundTag parse(String string) throws CommandSyntaxException {
		return new LegacyStringNbtReader(new StringReader(string)).readCompound();
	}

	@VisibleForTesting
	protected CompoundTag readCompound() throws CommandSyntaxException {
		CompoundTag nbtCompound = this.parseCompound();
		this.reader.skipWhitespace();
		if (this.reader.canRead()) {
			throw TRAILING.createWithContext(this.reader);
		} else {
			return nbtCompound;
		}
	}

	protected String readString() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw EXPECTED_KEY.createWithContext(this.reader);
		} else {
			return this.reader.readString();
		}
	}

	protected Tag parseElementPrimitive() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		int i = this.reader.getCursor();
		if (StringReader.isQuotedStringStart(this.reader.peek())) {
			return StringTag.valueOf(this.reader.readQuotedString());
		} else {
			String string = this.reader.readUnquotedString();
			if (string.isEmpty()) {
				this.reader.setCursor(i);
				throw EXPECTED_VALUE.createWithContext(this.reader);
			} else {
				return this.parsePrimitive(string);
			}
		}
	}

	private Tag parsePrimitive(String input) {
		try {
			if (FLOAT_PATTERN.matcher(input).matches()) {
				return FloatTag.valueOf(Float.parseFloat(input.substring(0, input.length() - 1)));
			}

			if (BYTE_PATTERN.matcher(input).matches()) {
				return ByteTag.valueOf(Byte.parseByte(input.substring(0, input.length() - 1)));
			}

			if (LONG_PATTERN.matcher(input).matches()) {
				return LongTag.valueOf(Long.parseLong(input.substring(0, input.length() - 1)));
			}

			if (SHORT_PATTERN.matcher(input).matches()) {
				return ShortTag.valueOf(Short.parseShort(input.substring(0, input.length() - 1)));
			}

			if (INT_PATTERN.matcher(input).matches()) {
				return IntTag.valueOf(Integer.parseInt(input));
			}

			if (DOUBLE_PATTERN.matcher(input).matches()) {
				return DoubleTag.valueOf(Double.parseDouble(input.substring(0, input.length() - 1)));
			}

			if (DOUBLE_PATTERN_IMPLICIT.matcher(input).matches()) {
				return DoubleTag.valueOf(Double.parseDouble(input));
			}

			if ("true".equalsIgnoreCase(input)) {
				return ByteTag.ONE;
			}

			if ("false".equalsIgnoreCase(input)) {
				return ByteTag.ZERO;
			}
		} catch (NumberFormatException var3) {
		}

		return StringTag.valueOf(input);
	}

	/**
	 * {@return the parsed NBT element}
	 *
	 * @throws CommandSyntaxException if the reader detects a syntax error
	 */
	public Tag parseElement() throws CommandSyntaxException {
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		} else {
			char c = this.reader.peek();
			if (c == '{') {
				return this.parseCompound();
			} else {
				return c == '[' ? this.parseArray() : this.parseElementPrimitive();
			}
		}
	}

	protected Tag parseArray() throws CommandSyntaxException {
		return this.reader.canRead(3) && !StringReader.isQuotedStringStart(this.reader.peek(1)) && this.reader.peek(2) == ';'
			? this.parseElementPrimitiveArray()
			: this.parseList();
	}

	/**
	 * {@return the parsed NBT compound}
	 *
	 * @throws CommandSyntaxException if the reader detects a syntax error
	 */
	public CompoundTag parseCompound() throws CommandSyntaxException {
		this.expect('{');
		CompoundTag nbtCompound = new CompoundTag();
		this.reader.skipWhitespace();

		while (this.reader.canRead() && this.reader.peek() != '}') {
			int i = this.reader.getCursor();
			String string = this.readString();
			if (string.isEmpty()) {
				this.reader.setCursor(i);
				throw EXPECTED_KEY.createWithContext(this.reader);
			}

			this.expect(':');
			nbtCompound.put(string, this.parseElement());
			if (!this.readComma()) {
				break;
			}

			if (!this.reader.canRead()) {
				throw EXPECTED_KEY.createWithContext(this.reader);
			}
		}

		this.expect('}');
		return nbtCompound;
	}

	/**
	 * @implNote The implementation is the exact same as the old vanilla StringNbtReader except where noted.
	 */
	protected Tag parseList() throws CommandSyntaxException {
		this.expect('[');
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		} else {
			ListTag nbtList = new ListTag();
			TagType<?> listType = null;

			while (this.reader.peek() != ']') {
				//Legacy lists might have the element indices before the actual element so we need to skip them (why Mojang? :()
				//Ex: [0:"Hello",1:"World!"]
				int originalPos = this.reader.getCursor();

				//Skip the index numbers
				while (Character.isDigit(this.reader.peek())) {
					this.reader.skip();
				}

				//Skip the : or restore the cursor position if its not present and this is an int list
				if (this.reader.peek() == ':') {
					this.reader.skip();
					this.reader.skipWhitespace();
				} else {
					this.reader.setCursor(originalPos);
				}

				int cursorPosition = this.reader.getCursor();
				Tag element = this.parseElement();
				TagType<?> elementType = element.getType();

				if (listType == null) {
					listType = elementType;
				} else if (elementType != listType) {
					this.reader.setCursor(cursorPosition);
					throw LIST_MIXED.createWithContext(this.reader, elementType.getPrettyName(), listType.getPrettyName());
				}

				nbtList.add(element);
				if (!this.readComma()) {
					break;
				}

				if (!this.reader.canRead()) {
					throw EXPECTED_VALUE.createWithContext(this.reader);
				}
			}

			this.expect(']');
			return nbtList;
		}
	}

	private Tag parseElementPrimitiveArray() throws CommandSyntaxException {
		this.expect('[');
		int i = this.reader.getCursor();
		char c = this.reader.read();
		this.reader.read();
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		} else if (c == 'B') {
			return new ByteArrayTag(mapToByteArray(this.readArray(ByteArrayTag.TYPE, ByteTag.TYPE)));
		} else if (c == 'L') {
			return new LongArrayTag(this.readArray(LongArrayTag.TYPE, LongTag.TYPE).stream().mapToLong(Number::longValue).toArray());
		} else if (c == 'I') {
			return new IntArrayTag(this.readArray(IntArrayTag.TYPE, IntTag.TYPE).stream().mapToInt(Number::intValue).toArray());
		} else {
			this.reader.setCursor(i);
			throw ARRAY_INVALID.createWithContext(this.reader, String.valueOf(c));
		}
	}

	private static byte[] mapToByteArray(List<Number> numbers) {
		byte[] bytes = new byte[numbers.size()];

		for (int i = 0; i < numbers.size(); i++) {
			bytes[i] = numbers.get(i).byteValue();
		}

		return bytes;
	}

	@SuppressWarnings("unchecked")
	private <T extends Number> List<T> readArray(TagType<?> arrayTypeReader, TagType<?> typeReader) throws CommandSyntaxException {
		List<T> list = Lists.newArrayList();

		while (this.reader.peek() != ']') {
			int i = this.reader.getCursor();
			Tag nbtElement = this.parseElement();
			TagType<?> nbtType = nbtElement.getType();
			if (nbtType != typeReader) {
				this.reader.setCursor(i);
				throw ARRAY_MIXED.createWithContext(this.reader, nbtType.getPrettyName(), arrayTypeReader.getPrettyName());
			}

			if (typeReader == ByteTag.TYPE) {
				list.add((T) Byte.valueOf(((NumericTag) nbtElement).byteValue()));
			} else if (typeReader == LongTag.TYPE) {
				list.add((T) Long.valueOf(((NumericTag) nbtElement).longValue()));
			} else {
				list.add((T) Integer.valueOf(((NumericTag) nbtElement).intValue()));
			}

			if (!this.readComma()) {
				break;
			}

			if (!this.reader.canRead()) {
				throw EXPECTED_VALUE.createWithContext(this.reader);
			}
		}

		this.expect(']');
		return list;
	}

	protected boolean readComma() {
		this.reader.skipWhitespace();
		if (this.reader.canRead() && this.reader.peek() == ',') {
			this.reader.skip();
			this.reader.skipWhitespace();
			return true;
		} else {
			return false;
		}
	}

	protected void expect(char c) throws CommandSyntaxException {
		this.reader.skipWhitespace();
		this.reader.expect(c);
	}
}
