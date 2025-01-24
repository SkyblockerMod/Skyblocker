package de.hysky.skyblocker.utils.datafixer;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.StringNbtReader;

/**
 * Implementation of {@link StringNbtReader} that is capable of reading the legacy SNBT format of 1.8.
 */
public class LegacyStringNbtReader extends StringNbtReader {

	public static NbtCompound parse(String string) throws CommandSyntaxException {
		return new LegacyStringNbtReader(new StringReader(string)).readCompound();
	}

	private LegacyStringNbtReader(StringReader reader) {
		super(reader);
	}

	/**
	 * @implNote The implementation is the exact same as the vanilla StringNbtReader except where noted.
	 */
	@Override
	protected NbtElement parseList() throws CommandSyntaxException {
		this.expect('[');
		this.reader.skipWhitespace();
		if (!this.reader.canRead()) {
			throw EXPECTED_VALUE.createWithContext(this.reader);
		} else {
			NbtList nbtList = new NbtList();
			NbtType<?> listType = null;

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
				NbtElement element = this.parseElement();
				NbtType<?> elementType = element.getNbtType();

				if (listType == null) {
					listType = elementType;
				} else if (elementType != listType) {
					this.reader.setCursor(cursorPosition);
					throw LIST_MIXED.createWithContext(this.reader, elementType.getCommandFeedbackName(), listType.getCommandFeedbackName());
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
}
