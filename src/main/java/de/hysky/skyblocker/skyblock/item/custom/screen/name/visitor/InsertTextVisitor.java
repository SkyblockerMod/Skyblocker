package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class InsertTextVisitor extends BaseVisitor {
	private final MutableText newText = MutableText.of(new PlainTextContent.Literal(""));
	private final String text;
	private final Style insertAs;

	public InsertTextVisitor(String text, @Nullable Style insertAs, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.text = text;
		this.insertAs = insertAs;
	}

	private void addNewText(MutableText newText) {
		this.newText.append(newText);
	}

	@Override
	public void visit(Style style, String asString) {
		if (asString.length() < selStart) { // not yet where we want to insert
			if (asString.isEmpty()) return;
			addNewText(Text.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		// we want to insert somewhere in this string
		if (selStart >= 0) {
			String substring = asString.substring(0, selStart);
			if (insertAs != null && selSize == 0) {
				addNewText(Text.literal(substring).setStyle(style));
				if (!text.isEmpty()) addNewText(Text.literal(text).setStyle(insertAs.withParent(style)));
			} else {
				addNewText(Text.literal(substring + text).setStyle(style));
			}
			asString = asString.substring(selStart);
			selStart = -1;
		}
		// if we have no size, we just append the rest of the string
		if (selSize <= 0) {
			if (!asString.isEmpty()) addNewText(Text.literal(asString).setStyle(style));
			return;
		}
		// the string is larger than the selection size, we need to split it
		if (asString.length() > selSize) {
			addNewText(Text.literal(asString.substring(selSize)).setStyle(style));
		}
		selSize -= asString.length();
	}

	public MutableText getNewText() {
		return newText;
	}
}
