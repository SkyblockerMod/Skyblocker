package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.text.Style;

/**
 * This visitor is used to get the style of a text at a specific selection range.
 * Pretty much does a or operation on the styles of the text in the selection range.
 */
public class GetStyleVisitor extends BaseVisitor {
	private Style style = Style.EMPTY;
	private final boolean noSize = selSize == 0;
	private boolean done = false;

	public GetStyleVisitor(int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
	}

	@Override
	protected void visit(Style st, String asString) {
		if (done) return;
		if (asString.length() < selStart) {
			selStart -= asString.length();
			return;
		}
		if (selStart != 0) asString = asString.substring(selStart);
		selStart = 0;
		if (noSize) {
			this.style = st;
			this.done = true;
			return;
		}
		selSize -= asString.length();
		done = selSize <= 0;
		if (asString.isEmpty()) return;
		this.style = this.style
				.withBold(this.style.isBold() || st.isBold())
				.withItalic(this.style.isItalic() || st.isItalic())
				.withUnderline(this.style.isUnderlined() || st.isUnderlined())
				.withStrikethrough(this.style.isStrikethrough() || st.isStrikethrough())
				.withObfuscated(this.style.isObfuscated() || st.isObfuscated());
	}

	public Style getStyle() {
		return style;
	}
}
