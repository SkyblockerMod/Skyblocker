package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Calculates the x coordinates of the selection start and end in a text.
 */
public class GetRenderWidthVisitor extends BaseVisitor {
	private final IntIntMutablePair widths = new IntIntMutablePair(0, 0);
	private final MutableComponent text = Component.empty();
	private final Font textRenderer;

	public GetRenderWidthVisitor(Font textRenderer, int selectionStart, int selectionEnd) {
		super(selectionStart, selectionEnd);
		this.textRenderer = textRenderer;
	}

	public GetRenderWidthVisitor(int selectionStart, int selectionEnd) {
		this(Minecraft.getInstance().font, selectionStart, selectionEnd);
	}

	@Override
	protected void visit(Style style, String asString) {
		if (asString.length() < selStart) {
			text.append(Component.literal(asString).setStyle(style));
			selStart -= asString.length();
			return;
		}

		if (selStart >= 0) {
			text.append(Component.literal(asString.substring(0, selStart)).setStyle(style));
			widths.first(textRenderer.width(text));
			asString = asString.substring(selStart);
			selStart = -1;
			if (selSize == 0) widths.second(textRenderer.width(text));
		}
		if (selSize == 0) return;
		if (asString.length() <= selSize) {
			text.append(Component.literal(asString).setStyle(style));
			selSize -= asString.length();
			if (selSize == 0) widths.second(textRenderer.width(text));
		} else {
			text.append(Component.literal(asString.substring(0, selSize)).setStyle(style));
			widths.second(textRenderer.width(text));
			selSize = 0;
		}
	}

	public IntIntPair getWidths() {
		return widths;
	}
}
