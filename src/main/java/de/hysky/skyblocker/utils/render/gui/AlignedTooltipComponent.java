package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

/**
 * Start a text with @align(x) as the first sibling to render the next line in the current line, off-set by x pixels.
 * Example:
 * {@snippet :
 * List<Text> lines; //Assuming you have a list of lines for the tooltip
 * lines.add(Text.empty().append("@align(50)").append("Left"));
 * lines.add(Text.of("Right"));
 * }
 * This will result in {@code Left<50px space>Right} (in the same line) being rendered.
 *
 */
public class AlignedTooltipComponent implements TooltipComponent {
	private final OrderedText left;
	private final int xOffset;
	private final OrderedText right;

	public AlignedTooltipComponent(OrderedText left, int xOffset, OrderedText right) {
		this.left = left;
		this.xOffset = xOffset;
		this.right = right;
	}

	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return Math.max(textRenderer.getWidth(left), xOffset) + textRenderer.getWidth(right);
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
		textRenderer.draw(left, x, y, -1, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		textRenderer.draw(right, x + xOffset, y, -1, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
	}
}
