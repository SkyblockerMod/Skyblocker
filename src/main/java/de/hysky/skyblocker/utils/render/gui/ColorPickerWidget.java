package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.gl.RenderPipelines;
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
	private static final Identifier SV_THUMB_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "color_picker/sv_thumb");

	private final int[] rainbowColors;

	private double hThumbX = 0;
	private double svThumbX = 0;
	private double svThumbY = 0;

	private int svColor = 0xFF_FF_00_00;

	private boolean draggingSV = false;
	private boolean draggingH = false;

	private ScreenRect svRect;
	private ScreenRect hRect;

	private int rgbColor = -1;
	private @Nullable Callback onColorChange = null;

	private static int[] createRainbowColors(int samples) {
		int[] rainbowColors = new int[samples];
		for (int i = 0; i < samples; i++) {
			rainbowColors[i] = Color.HSBtoRGB((float) i / samples, 1, 1);
		}
		return rainbowColors;
	}

	public ColorPickerWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.literal("ColorPicker"));
		rainbowColors = createRainbowColors(Math.min(width / 20, 8));
		updateRects();
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		super.onRelease(mouseX, mouseY);
		if ((draggingH || draggingSV) && onColorChange != null) {
			onColorChange.onColorChange(rgbColor, true);
		}
		draggingH = false;
		draggingSV = false;
	}

	private void updateRects() {
		hRect = new ScreenRect(getX() + 1, getBottom() - 9, getWidth() - 2, 8);
		int i = 15;
		svRect = new ScreenRect(getX() + 1 + i, getY() + 1, getWidth() - 2 - i, height - hRect.height() - 6);
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
	public void onClick(double mouseX, double mouseY) {
		super.onClick(mouseX, mouseY);
		int i = (int) mouseX;
		int j = (int) mouseY;
		if (hRect.contains(i, j)) {
			draggingH = true;
			onDrag(mouseX, mouseY, 0, 0);
		}
		if (svRect.contains(i, j)) {
			draggingSV = true;
			onDrag(mouseX, mouseY, 0, 0);
		}
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		super.onDrag(mouseX, mouseY, deltaX, deltaY);
		if (draggingH) {
			hThumbX = Math.clamp(mouseX - hRect.getLeft(), 0, hRect.width() - 1);
			svColor = Color.HSBtoRGB((float) (hThumbX / (hRect.width() - 1)), 1, 1);
		}
		if (draggingSV) {
			svThumbX = Math.clamp(mouseX - svRect.getLeft(), 0, svRect.width() - 1);
			svThumbY = Math.clamp(mouseY - svRect.getTop(), 0, svRect.height() - 1);
		}
		if (draggingH || draggingSV) {
			rgbColor = Color.HSBtoRGB(
					(float) (hThumbX / (hRect.width() - 1)),
					(float) (svThumbX / (svRect.width() - 1)),
					(float) (1 - (svThumbY / (svRect.height() - 1))));
			if (onColorChange != null) onColorChange.onColorChange(rgbColor, false);
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
		context.fill(hRect.getLeft() + (int) hThumbX - 1, hRect.getTop(), hRect.getLeft() + (int) hThumbX + 2, hRect.getBottom(), Colors.BLACK);
		context.fill(hRect.getLeft() + (int) hThumbX, hRect.getTop() - 1, hRect.getLeft() + (int) hThumbX + 1, hRect.getBottom() + 1, Colors.BLACK);
		context.fill(hRect.getLeft() + (int) hThumbX, hRect.getTop(), hRect.getLeft() + (int) hThumbX + 1, hRect.getBottom(), Colors.WHITE);

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

		// Preview
		context.fill(getX(), getY(), svRect.getLeft() - 2, svRect.getBottom() + 1, color);
		context.fill(getX() + 1, getY() + 1, svRect.getLeft() - 3, svRect.getBottom(), rgbColor);
	}

	public int getRGBColor() {
		return rgbColor;
	}

	public void setRGBColor(int rgb) {
		this.rgbColor = ColorHelper.fullAlpha(rgb);
		float[] floats = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
		setHSV(floats[0], floats[1], floats[2]);
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
	 * Sets a callback that will be called whenever the color is changed by the user (not when {@link ColorPickerWidget#setRGBColor(int)} is called).
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
