package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;

/**
 * This visitor is used to set a new style for a selection range in a text.
 * It replaces the style of the selected text with the new style.
 */
public class SetStyleVisitor extends BaseVisitor {
	private final MutableComponent newText = MutableComponent.create(new PlainTextContents.LiteralContents(""));
	private final Style newStyle;

	public SetStyleVisitor(Style style, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.newStyle = style;
	}

	private void addNewText(MutableComponent newText) {
		this.newText.append(newText);
	}

	@Override
	protected void visit(Style style, String asString) {
		if (asString.length() < selStart) { // not yet where we want to insert
			if (asString.isEmpty()) return;
			addNewText(Component.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		if (selStart >= 0) {
			String substring = asString.substring(0, selStart);
			addNewText(Component.literal(substring).setStyle(style));

			asString = asString.substring(selStart);
			selStart = -1;
		}
		if (selSize <= 0) {
			if (!asString.isEmpty()) addNewText(Component.literal(asString).setStyle(style));
			return;
		}
		if (asString.length() <= selSize) {
			addNewText(Component.literal(asString).setStyle(newStyle.applyTo(style)));
		} else {
			addNewText(Component.literal(asString.substring(0, selSize)).setStyle(newStyle.applyTo(style)));
			addNewText(Component.literal(asString.substring(selSize)).setStyle(style));
		}
		selSize -= asString.length();
	}

	public MutableComponent getNewText() {
		return newText;
	}
}
