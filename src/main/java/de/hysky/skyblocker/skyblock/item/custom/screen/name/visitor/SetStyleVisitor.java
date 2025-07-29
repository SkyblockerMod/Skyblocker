package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * This visitor is used to set a new style for a selection range in a text.
 * It replaces the style of the selected text with the new style.
 */
public class SetStyleVisitor extends BaseVisitor {
	private final MutableText newText = MutableText.of(new PlainTextContent.Literal(""));
	private final Style newStyle;

	public SetStyleVisitor(Style style, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.newStyle = style;
	}

	private void addNewText(MutableText newText) {
		this.newText.append(newText);
	}

	@Override
	protected void visit(Style style, String asString) {
		if (asString.length() < selStart) { // not yet where we want to insert
			if (asString.isEmpty()) return;
			addNewText(Text.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		if (selStart >= 0) {
			String substring = asString.substring(0, selStart);
			addNewText(Text.literal(substring).setStyle(style));

			asString = asString.substring(selStart);
			selStart = -1;
		}
		if (selSize <= 0) {
			if (!asString.isEmpty()) addNewText(Text.literal(asString).setStyle(style));
			return;
		}
		if (asString.length() <= selSize) {
			addNewText(Text.literal(asString).setStyle(newStyle.withParent(style)));
		} else {
			addNewText(Text.literal(asString.substring(0, selSize)).setStyle(newStyle.withParent(style)));
			addNewText(Text.literal(asString.substring(selSize)).setStyle(style));
		}
		selSize -= asString.length();
	}

	public MutableText getNewText() {
		return newText;
	}
}
