package de.hysky.skyblocker.utils.render.gui;

import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.mixins.accessors.EditBoxAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

public class FilteredEditBox extends EditBox {
	private Predicate<String> filter = Objects::nonNull;

	public FilteredEditBox(Font font, int width, int height, Component narration) {
		super(font, width, height, narration);
	}

	public FilteredEditBox(Font font, int x, int y, int width, int height, Component narration) {
		super(font, x, y, width, height, null, narration);
	}

	public FilteredEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox oldBox, Component narration) {
		super(font, x, y, width, height, oldBox, narration);
	}

	public void setFilter(Predicate<String> filter) {
		this.filter = filter;
	}

	@Override
	public void setValue(String value) {
		if (this.filter.test(value)) {
			super.setValue(value);
		}
	}

	@Override
	public void insertText(final String input) {
		EditBoxAccessor accessor = (EditBoxAccessor) this;
		int start = Math.min(this.getCursorPosition(), accessor.getHighlightPos());
		int end = Math.max(this.getCursorPosition(), accessor.getHighlightPos());
		int maxInsertionLength = accessor.getMaxLength() - this.getValue().length() - (start - end);
		if (maxInsertionLength > 0) {
			String text = StringUtil.filterText(input);
			int insertionLength = text.length();
			if (maxInsertionLength < insertionLength) {
				if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1))) {
					maxInsertionLength--;
				}

				text = text.substring(0, maxInsertionLength);
				insertionLength = maxInsertionLength;
			}

			String newValue = new StringBuilder(this.getValue()).replace(start, end, text).toString();

			if (this.filter.test(input)) {
				this.setValue(newValue);
				this.setCursorPosition(start + insertionLength);
				this.setHighlightPos(this.getCursorPosition());
				accessor.invokeOnValueChange(this.getValue());
			}
		}
	}

	@Override
	public void deleteCharsToPos(final int pos) {
		EditBoxAccessor accessor = (EditBoxAccessor) this;

		if (!this.getValue().isEmpty()) {
			if (accessor.getHighlightPos() != this.getCursorPosition()) {
				this.insertText("");
			} else {
				int start = Math.min(pos, this.getCursorPosition());
				int end = Math.max(pos, this.getCursorPosition());
				if (start != end) {
					String newValue = new StringBuilder(this.getValue()).delete(start, end).toString();

					if (this.filter.test(newValue)) {
						this.setCursorPosition(start);
						accessor.invokeOnValueChange(this.getValue());
						this.moveCursorTo(start, false);
					}
				}
			}
		}
	}
}
