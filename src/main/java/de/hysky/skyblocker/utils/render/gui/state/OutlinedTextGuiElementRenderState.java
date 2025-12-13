package de.hysky.skyblocker.utils.render.gui.state;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.mixins.accessors.FontAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.GlyphVisitor;
import net.minecraft.client.gui.Font.PreparedText;
import net.minecraft.client.gui.Font.PreparedTextBuilder;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;

public class OutlinedTextGuiElementRenderState extends GuiTextRenderState {
	private final int outlineColor;
	private @Nullable OutlineGlyphDrawable preparation;
	private @Nullable ScreenRectangle bounds;

	public OutlinedTextGuiElementRenderState(
			Font textRenderer,
			FormattedCharSequence orderedText,
			Matrix3x2f matrix,
			int x,
			int y,
			int color,
			int outlineColor,
			boolean shadow,
			boolean trackEmpty,
			ScreenRectangle clipBounds) {
		super(textRenderer, orderedText, matrix, x, y, color, 0, shadow, trackEmpty, clipBounds);
		this.outlineColor = outlineColor;
	}

	/**
	 * The text outline drawing code from the {@code TextRenderer}.
	 */
	private PreparedText prepareOutline() {
		FontAccessor accessor = (FontAccessor) this.font;
		Font.PreparedTextBuilder drawer = this.font.new PreparedTextBuilder(0.0f, 0.0f, this.outlineColor, false, this.includeEmpty);

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i != 0 || j != 0) {
					float[] fs = new float[]{this.x};
					int k = i;
					int l = j;
					this.text.accept((index, style, codePoint) -> {
						boolean bl = style.isBold();
						BakedGlyph bakedGlyph = accessor.invokeGetGlyph(codePoint, style);
						drawer.x = fs[0] + k * bakedGlyph.info().getShadowOffset();
						drawer.y = y + l * bakedGlyph.info().getShadowOffset();
						fs[0] += bakedGlyph.info().getAdvance(bl);
						return drawer.accept(index, style.withColor(this.outlineColor), bakedGlyph);
					});
				}
			}
		}

		return drawer;
	}

	private PreparedText prepareText() {
		PreparedText textPreparation = this.font.prepareText(this.text, (float) this.x, (float) this.y, this.color, this.dropShadow, this.includeEmpty, this.backgroundColor);

		ScreenRectangle screenRect = textPreparation.bounds();
		if (screenRect != null) {
			screenRect = screenRect.transformMaxBounds(this.pose);
			this.bounds = this.scissor != null ? this.scissor.intersection(screenRect) : screenRect;
		}

		return textPreparation;
	}

	@Override
	public PreparedText ensurePrepared() {
		if (this.preparation == null) {
			this.preparation = new OutlineGlyphDrawable(this.prepareOutline(), this.prepareText());
		}

		return this.preparation;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		this.ensurePrepared();

		return this.bounds;
	}

	private record OutlineGlyphDrawable(PreparedText outline, PreparedText text) implements PreparedText {
		@Override
		public void visit(GlyphVisitor glyphDrawer) {
			this.outline.visit(glyphDrawer);
			this.text.visit(glyphDrawer);
		}

		@Override
		public ScreenRectangle bounds() {
			//Shouldn't need an implementation but if it eventually does its probably best to use the outline rect
			//since it should be the bigger one
			throw new UnsupportedOperationException("For drawing only");
		}
	}
}
