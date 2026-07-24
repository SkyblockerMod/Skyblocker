package de.hysky.skyblocker.utils.render.text;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class GridTooltipComponent implements ClientTooltipComponent {
	private final GridFormattedCharSequence sequence;
	private @Nullable GridComponentManager manager;

	public GridTooltipComponent(final GridFormattedCharSequence sequence) {
		this.sequence = sequence;
	}

	public void setManager(@Nullable GridComponentManager manager) {
		this.manager = manager;
	}

	@Override
	public int getHeight(Font font) {
		return font.lineHeight + 1;
	}

	public GridFormattedCharSequence sequence() {
		return sequence;
	}

	@Override
	public int getWidth(Font font) {
		if (manager != null) {
			manager.updateWidths(this, font);
			return manager.getTotalWidth(sequence.gridContents().group());
		}
		return font.width(sequence);
	}

	@Override
	public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		ActiveTextCollector textRenderer = graphics.textRenderer();
		for (int i = 0; i < sequence.gridContents().components().size(); i++) {
			Component column = sequence.gridContents().components().get(i);
			textRenderer.accept(x, y, column);
			if (manager == null) {
				x += font.width(column);
			}
			else {
				int width = manager.getWidth(sequence.gridContents().group(), i);
				x += width == 0 ? font.width(column) : width;
			}
		}

	}
}
