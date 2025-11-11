package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @implNote Does not render a background.
 */
public class ColorPickerWidget extends ClickableWidget {
	private static final Identifier SV_THUMB_TEXTURE = SkyblockerMod.id("color_picker/sv_thumb");

	private final int[] rainbowColors;
	/**
	 * Whether the alpha channel can be changed
	 */
	private final boolean hasAlpha;
	/**
	 * Mask to have full alpha if {@link ColorPickerWidget#hasAlpha} is {@code false}
	 */
	private final int alphaMask;

	/**
	 * Alpha thumb X
	 */
	private double aThumbX = 0;
	/**
	 * Hue thumb X
	 */
	private double hThumbX = 0;
	/**
	 * Saturation/Value thumb X
	 */
	private double svThumbX = 0;
	/**
	 * Saturation/Value thumb Y
	 */
	private double svThumbY = 0;

	private int svColor = 0xFF_FF_00_00;

	private boolean draggingSV = false;
	private boolean draggingH = false;
	private boolean draggingA = false;

	private ScreenRect svRect;
	private ScreenRect hRect;
	private ScreenRect aRect = ScreenRect.empty();

	private int argbColor = -1;
	private @Nullable Callback onColorChange = null;

	private static int[] createRainbowColors(int samples) {
		int[] rainbowColors = new int[samples];
		for (int i = 0; i < samples; i++) {
			rainbowColors[i] = Color.HSBtoRGB((float) i / samples, 1, 1);
		}
		return rainbowColors;
	}
	public ColorPickerWidget(int x, int y, int width, int height) {
		this(x, y, width, height, false);
	}

	public ColorPickerWidget(int x, int y, int width, int height, boolean hasAlpha) {
		super(x, y, width, height, Text.literal("ColorPicker"));
		rainbowColors = createRainbowColors(Math.min(width / 20, 8));
		this.hasAlpha = hasAlpha;
		this.alphaMask = hasAlpha ? 0 : 0xFF000000;
		updateRects();
	}

	@Override
	public void onRelease(Click click) {
		super.onRelease(click);
		if ((draggingH || draggingSV || draggingA) && onColorChange != null) {
			onColorChange.onColorChange(argbColor | alphaMask, true);
		}
		draggingH = false;
		draggingSV = false;
		draggingA = false;
	}

	private void updateRects() {
		int y = getBottom();
		if (hasAlpha) {
			aRect = new ScreenRect(getX() + 1, getBottom() - 9, getWidth() - 2, 8);
			y = aRect.getTop();
		}
		hRect = new ScreenRect(getX() + 1, y - 9 - 4, getWidth() - 2, 8);
		int previewOffset = 15;
		int svY = getY() + 1;
		svRect = new ScreenRect(getX() + 1 + previewOffset, svY, getWidth() - 2 - previewOffset, hRect.getTop() - svY - 4);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		updateRects();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		updateRects();
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		updateRects();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		updateRects();
	}

	@Override
	public void setDimensions(int width, int height) { // this doesn't call setWidth or setHeight
		super.setDimensions(width, height);
		updateRects();
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		super.onClick(click, doubled);
		int i = (int) click.x();
		int j = (int) click.y();
		if (hRect.contains(i, j)) {
			draggingH = true;
			onDrag(click, 0, 0);
		}
		if (svRect.contains(i, j)) {
			draggingSV = true;
			onDrag(click, 0, 0);
		}
		if (hasAlpha && aRect.contains(i, j)) {
			draggingA = true;
			onDrag(click, 0, 0);
		}
	}

	@Override
	protected void onDrag(Click click, double deltaX, double deltaY) {
		super.onDrag(click, deltaX, deltaY);
		if (draggingH) {
			hThumbX = Math.clamp(click.x() - hRect.getLeft(), 0, hRect.width() - 1);
			svColor = Color.HSBtoRGB((float) (hThumbX / (hRect.width() - 1)), 1, 1);
		}
		if (draggingSV) {
			svThumbX = Math.clamp(click.x() - svRect.getLeft(), 0, svRect.width() - 1);
			svThumbY = Math.clamp(click.y() - svRect.getTop(), 0, svRect.height() - 1);
		}
		if (draggingA) {
			aThumbX = Math.clamp(click.x() - aRect.getLeft(), 0, aRect.width() - 1);
		}
		if (draggingH || draggingSV || draggingA) {
			float alpha = hasAlpha ? (float) aThumbX / (aRect.width() - 1) : 1f;
			argbColor = ColorHelper.withAlpha(alpha, Color.HSBtoRGB(
					(float) (hThumbX / (hRect.width() - 1)),
					(float) (svThumbX / (svRect.width() - 1)),
					(float) (1 - (svThumbY / (svRect.height() - 1)))));
			if (onColorChange != null) onColorChange.onColorChange(argbColor, false);
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		int color = 0x80_60_60_60;
		// Hue
		context.fill(hRect.getLeft() - 1, hRect.getTop() - 1, hRect.getRight() + 1, hRect.getBottom() + 1, color);
		for (int i = 0; i < rainbowColors.length; i++) {
			int startColor = rainbowColors[i];
			int endColor = rainbowColors[(i + 1) % rainbowColors.length];
			float segmentLength = (float) hRect.width() / rainbowColors.length;
			float startX = hRect.getLeft() + segmentLength * i;
			float endX = hRect.getLeft() + segmentLength * (i + 1);
			HudHelper.drawHorizontalGradient(context, startX, hRect.getTop(), endX, hRect.getBottom(), startColor, endColor);
		}
		drawThumb(context, hRect, (int) hThumbX);

		// Light and saturation or whatever
		context.fill(svRect.getLeft() - 1, svRect.getTop() - 1, svRect.getRight() + 1, svRect.getBottom() + 1, color);
		int pickerX = svRect.getLeft();
		int pickerY = svRect.getTop();
		int pickerEndX = svRect.getRight();
		int pickerEndY = svRect.getBottom();
		HudHelper.drawHorizontalGradient(context, pickerX, pickerY, pickerEndX, pickerEndY, -1, svColor);
		context.fillGradient(pickerX, pickerY, pickerEndX, pickerEndY, 1, 0xFF_00_00_00);

		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SV_THUMB_TEXTURE,
				svRect.getLeft() + (int) svThumbX - 2,
				svRect.getTop() + (int) svThumbY - 2,
				5, 5
		);

		// Alpha
		if (hasAlpha) {
			context.fill(aRect.getLeft() - 1, aRect.getTop() - 1, aRect.getRight() + 1, aRect.getBottom() + 1, color);
			HudHelper.drawHorizontalGradient(context, aRect.getLeft(), aRect.getTop(), aRect.getRight(), aRect.getBottom(), Colors.BLACK, Colors.WHITE);

			drawThumb(context, aRect, (int) aThumbX);

		}

		// Preview
		context.fill(getX(), getY(), svRect.getLeft() - 2, svRect.getBottom() + 1, color);
		context.fill(getX() + 1, getY() + 1, svRect.getLeft() - 3, svRect.getBottom(), argbColor);
	}

	private void drawThumb(DrawContext context, ScreenRect rect, int thumbX) {
		context.fill(rect.getLeft() + thumbX - 1, rect.getTop(), rect.getLeft() + thumbX + 2, rect.getBottom(), Colors.BLACK);
		context.fill(rect.getLeft() + thumbX, rect.getTop() - 1, rect.getLeft() + thumbX + 1, rect.getBottom() + 1, Colors.BLACK);
		context.fill(rect.getLeft() + thumbX, rect.getTop(), rect.getLeft() + thumbX + 1, rect.getBottom(), Colors.WHITE);
	}

	public int getARGBColor() {
		return argbColor;
	}

	public void setARGBColor(int argb) {
		this.argbColor = argb | alphaMask;
		float[] floats = Color.RGBtoHSB((argbColor >> 16) & 0xFF, (argbColor >> 8) & 0xFF, argbColor & 0xFF, null);
		setHSV(floats[0], floats[1], floats[2]);
		setAlpha(ColorHelper.getAlphaFloat(argbColor));
	}

	/**
	 * values between 0 and 1
	 */
	public void setHSV(float h, float s, float v) {
		hThumbX = h * (hRect.width() - 1);
		svThumbX = s * (svRect.width() - 1);
		svThumbY = (1 - v) * (svRect.height() - 1);
		svColor = Color.HSBtoRGB((float) (hThumbX / (hRect.width() - 1)), 1, 1);
	}

	/**
	 * @param alpha between 0 and 1
	 */
	public void setAlpha(float alpha) {
		if (!hasAlpha) return;
		aThumbX = alpha * (aRect.width() - 1);
	}

	/**
	 * Sets a callback that will be called whenever the color is changed by the user (not when {@link ColorPickerWidget#setARGBColor(int)} is called).
	 * @param onColorChange The consumer
	 */
	public void setOnColorChange(@Nullable Callback onColorChange) {
		this.onColorChange = onColorChange;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public interface Callback {

		/**
		 * @param color the new color
		 * @param mouseRelease true if the change is "final" after the user has released the mouse or false when it's from the user dragging on of the thumbs around.
		 */
		void onColorChange(int color, boolean mouseRelease);
	}
}
