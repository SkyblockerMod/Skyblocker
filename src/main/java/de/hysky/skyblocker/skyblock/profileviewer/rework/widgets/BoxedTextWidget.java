package de.hysky.skyblocker.skyblock.profileviewer.rework.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerScreenRework;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.StreamSupport;

public class BoxedTextWidget implements ProfileViewerWidget {

	private static final Identifier BACKGROUND = Identifier.of(SkyblockerMod.NAMESPACE, "profile_viewer/generic_background");
	public static final int PADDING = 2;
	private static final int GAP = 1;

	public record TextWithHover(
			Text text,
			List<Text> hover
	) {}

	final int width;
	final int height;
	final List<TextWithHover> textLines;
	final TextRenderer textRenderer;

	public BoxedTextWidget(int width, int height, List<TextWithHover> textLines, TextRenderer textRenderer) {
		this.width = width;
		this.height = height;
		this.textLines = textLines;
		this.textRenderer = textRenderer;
	}

	public static BoxedTextWidget boxedTextWithHover(int width, List<TextWithHover> textLines) {
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		return new BoxedTextWidget(width + 2 * PADDING, (textRenderer.fontHeight + GAP) * textLines.size() - GAP + 2 * PADDING, textLines, textRenderer);
	}


	public static TextWithHover hover(Text text, List<Text> hover) {
		return new TextWithHover(text, hover);
	}

	public static TextWithHover nohover(Text text) {
		return new TextWithHover(text, List.of());
	}

	public static BoxedTextWidget boxedText(int width, Iterable<Text> textLines) {
		return boxedTextWithHover(width, StreamSupport.stream(textLines.spliterator(), false)
				.map(it -> new TextWithHover(it, List.of()))
				.toList()
		);
	}

	@Override
	public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks) {
		HudHelper.renderNineSliceColored(drawContext, BACKGROUND, x, y, width, height, Colors.WHITE);
		int lineSkip = GAP + textRenderer.fontHeight;
		var matrices = drawContext.getMatrices();
		var availableSpace = width - 2 * PADDING;
		for (int i = 0; i < textLines.size(); i++) {
			var line = textLines.get(i);
			var textWidth = textRenderer.getWidth(line.text());
			matrices.pushMatrix();
			matrices.translate(x + PADDING, y + PADDING + i * lineSkip + textRenderer.fontHeight / 2F);
			if (textWidth > availableSpace)
				matrices.scale((float) availableSpace / textWidth);
			drawContext.drawText(textRenderer, line.text(), 0, -textRenderer.fontHeight / 2, Colors.WHITE, ProfileViewerScreenRework.TEXT_SHADOW);
			matrices.popMatrix();
			if (!line.hover().isEmpty() && isHovered(x + PADDING, y + PADDING + i * lineSkip, Math.min(textWidth, availableSpace), textRenderer.fontHeight, mouseX, mouseY))
				drawContext.drawTooltip(textRenderer, line.hover(), mouseX, mouseY);
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

}
