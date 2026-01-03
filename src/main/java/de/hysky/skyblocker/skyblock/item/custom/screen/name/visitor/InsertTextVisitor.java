package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jspecify.annotations.Nullable;

public class InsertTextVisitor extends BaseVisitor {
	private final MutableComponent newText = MutableComponent.create(new PlainTextContents.LiteralContents(""));
	private final String text;
	private final Style insertAs;

	public InsertTextVisitor(String text, @Nullable Style insertAs, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.text = text;
		this.insertAs = insertAs;
	}

	private void addNewText(MutableComponent newText) {
		this.newText.append(newText);
	}

	@Override
	public void visit(Style style, String asString) {
		if (asString.length() < selStart) { // not yet where we want to insert
			if (asString.isEmpty()) return;
			addNewText(Component.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		// we want to insert somewhere in this string
		if (selStart >= 0) {
			String substring = asString.substring(0, selStart);
			if (insertAs != null && selSize == 0) {
				addNewText(Component.literal(substring).setStyle(style));
				if (!text.isEmpty()) addNewText(Component.literal(text).setStyle(insertAs.applyTo(style)));
			} else {
				addNewText(Component.literal(substring + text).setStyle(style));
			}
			asString = asString.substring(selStart);
			selStart = -1;
		}
		// if we have no size, we just append the rest of the string
		if (selSize <= 0) {
			if (!asString.isEmpty()) addNewText(Component.literal(asString).setStyle(style));
			return;
		}
		// the string is larger than the selection size, we need to split it
		if (asString.length() > selSize) {
			addNewText(Component.literal(asString.substring(selSize)).setStyle(style));
		}
		selSize -= asString.length();
	}

	public MutableComponent getNewText() {
		return newText;
	}
}
