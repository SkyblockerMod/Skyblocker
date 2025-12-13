package de.hysky.skyblocker.utils.render.gui.state;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.mixins.accessors.TextRendererAccessor;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.GlyphDrawable;
import net.minecraft.client.font.TextRenderer.GlyphDrawer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.text.OrderedText;

public class OutlinedTextGuiElementRenderState extends TextGuiElementRenderState {
	private final int outlineColor;
	private @Nullable OutlineGlyphDrawable preparation;
	private @Nullable ScreenRect bounds;

	public OutlinedTextGuiElementRenderState(
			TextRenderer textRenderer,
			OrderedText orderedText,
			Matrix3x2f matrix,
			int x,
			int y,
			int color,
			int outlineColor,
			boolean shadow,
			boolean trackEmpty,
			ScreenRect clipBounds) {
		super(textRenderer, orderedText, matrix, x, y, color, 0, shadow, trackEmpty, clipBounds);
		this.outlineColor = outlineColor;
	}

	/**
	 * The text outline drawing code from the {@code TextRenderer}.
	 */
	private GlyphDrawable prepareOutline() {
		TextRendererAccessor accessor = (TextRendererAccessor) this.textRenderer;
		TextRenderer.Drawer drawer = this.textRenderer.new Drawer(0.0f, 0.0f, this.outlineColor, false, this.trackEmpty);

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i != 0 || j != 0) {
					float[] fs = new float[]{this.x};
					int k = i;
					int l = j;
					this.orderedText.accept((index, style, codePoint) -> {
						boolean bl = style.isBold();
						BakedGlyph bakedGlyph = accessor.invokeGetGlyph(codePoint, style);
						drawer.x = fs[0] + k * bakedGlyph.getMetrics().getShadowOffset();
						drawer.y = y + l * bakedGlyph.getMetrics().getShadowOffset();
						fs[0] += bakedGlyph.getMetrics().getAdvance(bl);
						return drawer.accept(index, style.withColor(this.outlineColor), bakedGlyph);
					});
				}
			}
		}

		return drawer;
	}

	private GlyphDrawable prepareText() {
		GlyphDrawable textPreparation = this.textRenderer.prepare(this.orderedText, (float) this.x, (float) this.y, this.color, this.shadow, this.trackEmpty, this.backgroundColor);

		ScreenRect screenRect = textPreparation.getScreenRect();
		if (screenRect != null) {
			screenRect = screenRect.transformEachVertex(this.matrix);
			this.bounds = this.clipBounds != null ? this.clipBounds.intersection(screenRect) : screenRect;
		}

		return textPreparation;
	}

	@Override
	public GlyphDrawable prepare() {
		if (this.preparation == null) {
			this.preparation = new OutlineGlyphDrawable(this.prepareOutline(), this.prepareText());
		}

		return this.preparation;
	}

	@Override
	public @Nullable ScreenRect bounds() {
		this.prepare();

		return this.bounds;
	}

	private record OutlineGlyphDrawable(GlyphDrawable outline, GlyphDrawable text) implements GlyphDrawable {
		@Override
		public void draw(GlyphDrawer glyphDrawer) {
			this.outline.draw(glyphDrawer);
			this.text.draw(glyphDrawer);
		}

		@Override
		public ScreenRect getScreenRect() {
			//Shouldn't need an implementation but if it eventually does its probably best to use the outline rect
			//since it should be the bigger one
			throw new UnsupportedOperationException("For drawing only");
		}
	}
}
