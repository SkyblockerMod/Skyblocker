package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import java.util.Optional;

abstract class BaseVisitor implements StringVisitable.StyledVisitor<Void> {
	protected int selStart;
	protected int selSize;

	BaseVisitor(int selectionStart, int selectionEnd) {
		this.selStart = Math.min(selectionStart, selectionEnd);
		this.selSize = Math.abs(selectionStart - selectionEnd);
	}

	@Override
	public final Optional<Void> accept(Style style, String asString) {
		visit(style, asString);
		return Optional.empty();
	}

	protected abstract void visit(Style style, String asString);
}
