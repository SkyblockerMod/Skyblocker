package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * Calculates the x coordinates of the selection start and end in a text.
 */
public class GetRenderWidthVisitor extends BaseVisitor {
	private final IntIntMutablePair widths = new IntIntMutablePair(0, 0);
	private final MutableText text = Text.empty();
	private final TextRenderer textRenderer;

	public GetRenderWidthVisitor(TextRenderer textRenderer, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.textRenderer = textRenderer;
	}

	public GetRenderWidthVisitor(int selectionStart, int selectionEnd) {
		this(MinecraftClient.getInstance().textRenderer, selectionStart, selectionEnd);
	}

	@Override
	protected void visit(Style style, String asString) {
		if (asString.length() < selStart) {
			text.append(Text.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		if (selStart >= 0) {
			text.append(Text.literal(asString.substring(0, selStart)).setStyle(style));
			widths.first(textRenderer.getWidth(text));
			asString = asString.substring(selStart);
			selStart = -1;
			if (selSize == 0) widths.second(textRenderer.getWidth(text));
		}
		if (selSize == 0) return;
		if (asString.length() <= selSize) {
			text.append(Text.literal(asString).setStyle(style));
			selSize -= asString.length();
			if (selSize == 0) widths.second(textRenderer.getWidth(text));
		} else {
			text.append(Text.literal(asString.substring(0, selSize)).setStyle(style));
			widths.second(textRenderer.getWidth(text));
			selSize = 0;
		}
	}

	public IntIntPair getWidths() {
		return widths;
	}
}
