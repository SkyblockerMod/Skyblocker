package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.MutableText;
import org.joml.Matrix4f;

/**
 * This class is used to display text at a certain x offset after the current text in tooltips.
 * Example:
 * <pre><code>
 * List<Text> lines; //Assuming you have a list of lines for the tooltip
 * lines.add(Text.literal("Left")
 *               .align(Text.literal("Right"), 50);</code></pre>
 * This will result in {@code Left<50px - the width of the "Left" text>Right} (in the same line) being rendered.
 */
public class AlignedTooltipComponent implements TooltipComponent {
	private final MutableText text;

	public AlignedTooltipComponent(MutableText text) {
		this.text = text;
	}

	//Same as the original OrderedTextTooltipComponent
	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		MutableText tmpText = this.text;
		int width = 0;
		while (tmpText != null) {
			int offset = tmpText.getXOffset();
			//If the offset would cause the following text to overlap with the previous text, the width of the previous text is used instead to append the following text to the previous text
			width += offset != Integer.MIN_VALUE ? Math.max(textRenderer.getWidth(tmpText), tmpText.getXOffset()) : textRenderer.getWidth(tmpText);
			tmpText = tmpText.getAlignedText();
		}
		return width;
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
		MutableText tmpText = this.text;
		int tmpX = x;
		while (tmpText != null) {
			textRenderer.draw(tmpText, tmpX, y, -1, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
			tmpX += Math.max(textRenderer.getWidth(tmpText), tmpText.getXOffset());
			tmpText = tmpText.getAlignedText();
		}
	}
}
