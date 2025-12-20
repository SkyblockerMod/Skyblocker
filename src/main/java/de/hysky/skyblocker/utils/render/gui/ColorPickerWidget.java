package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.HudHelper;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

/**
 * @implNote Does not render a background.
 */
public class ColorPickerWidget extends AbstractWidget {
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

	private ScreenRectangle svRect;
	private ScreenRectangle hRect;
	private ScreenRectangle aRect = ScreenRectangle.empty();

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
		super(x, y, width, height, Component.literal("ColorPicker"));
		rainbowColors = createRainbowColors(Math.min(width / 20, 8));
		this.hasAlpha = hasAlpha;
		this.alphaMask = hasAlpha ? 0 : 0xFF000000;
		updateRects();
	}

	@Override
	public void onRelease(MouseButtonEvent click) {
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
			aRect = new ScreenRectangle(getX() + 1, getBottom() - 9, getWidth() - 2, 8);
			y = aRect.top();
		}
		hRect = new ScreenRectangle(getX() + 1, y - 9 - 4, getWidth() - 2, 8);
		int previewOffset = 15;
		int svY = getY() + 1;
		svRect = new ScreenRectangle(getX() + 1 + previewOffset, svY, getWidth() - 2 - previewOffset, hRect.top() - svY - 4);
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
	public void setSize(int width, int height) { // this doesn't call setWidth or setHeight
		super.setSize(width, height);
		updateRects();
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		int i = (int) click.x();
		int j = (int) click.y();
		if (hRect.containsPoint(i, j)) {
			draggingH = true;
			onDrag(click, 0, 0);
		}
		if (svRect.containsPoint(i, j)) {
			draggingSV = true;
			onDrag(click, 0, 0);
		}
		if (hasAlpha && aRect.containsPoint(i, j)) {
			draggingA = true;
			onDrag(click, 0, 0);
		}
	}

	@Override
	protected void onDrag(MouseButtonEvent click, double deltaX, double deltaY) {
		super.onDrag(click, deltaX, deltaY);
		if (draggingH) {
			hThumbX = Math.clamp(click.x() - hRect.left(), 0, hRect.width() - 1);
			svColor = Color.HSBtoRGB((float) (hThumbX / (hRect.width() - 1)), 1, 1);
		}
		if (draggingSV) {
			svThumbX = Math.clamp(click.x() - svRect.left(), 0, svRect.width() - 1);
			svThumbY = Math.clamp(click.y() - svRect.top(), 0, svRect.height() - 1);
		}
		if (draggingA) {
			aThumbX = Math.clamp(click.x() - aRect.left(), 0, aRect.width() - 1);
		}
		if (draggingH || draggingSV || draggingA) {
			float alpha = hasAlpha ? (float) aThumbX / (aRect.width() - 1) : 1f;
			argbColor = ARGB.color(alpha, Color.HSBtoRGB(
					(float) (hThumbX / (hRect.width() - 1)),
					(float) (svThumbX / (svRect.width() - 1)),
					(float) (1 - (svThumbY / (svRect.height() - 1)))));
			if (onColorChange != null) onColorChange.onColorChange(argbColor, false);
		}
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		int color = 0x80_60_60_60;
		// Hue
		context.fill(hRect.left() - 1, hRect.top() - 1, hRect.right() + 1, hRect.bottom() + 1, color);
		for (int i = 0; i < rainbowColors.length; i++) {
			int startColor = rainbowColors[i];
			int endColor = rainbowColors[(i + 1) % rainbowColors.length];
			float segmentLength = (float) hRect.width() / rainbowColors.length;
			float startX = hRect.left() + segmentLength * i;
			float endX = hRect.left() + segmentLength * (i + 1);
			HudHelper.drawHorizontalGradient(context, startX, hRect.top(), endX, hRect.bottom(), startColor, endColor);
		}
		drawThumb(context, hRect, (int) hThumbX);

		// Light and saturation or whatever
		context.fill(svRect.left() - 1, svRect.top() - 1, svRect.right() + 1, svRect.bottom() + 1, color);
		int pickerX = svRect.left();
		int pickerY = svRect.top();
		int pickerEndX = svRect.right();
		int pickerEndY = svRect.bottom();
		HudHelper.drawHorizontalGradient(context, pickerX, pickerY, pickerEndX, pickerEndY, -1, svColor);
		context.fillGradient(pickerX, pickerY, pickerEndX, pickerEndY, 1, 0xFF_00_00_00);

		context.blitSprite(RenderPipelines.GUI_TEXTURED, SV_THUMB_TEXTURE,
				svRect.left() + (int) svThumbX - 2,
				svRect.top() + (int) svThumbY - 2,
				5, 5
		);

		// Alpha
		if (hasAlpha) {
			context.fill(aRect.left() - 1, aRect.top() - 1, aRect.right() + 1, aRect.bottom() + 1, color);
			HudHelper.drawHorizontalGradient(context, aRect.left(), aRect.top(), aRect.right(), aRect.bottom(), CommonColors.BLACK, CommonColors.WHITE);

			drawThumb(context, aRect, (int) aThumbX);

		}

		// Preview
		context.fill(getX(), getY(), svRect.left() - 2, svRect.bottom() + 1, color);
		context.fill(getX() + 1, getY() + 1, svRect.left() - 3, svRect.bottom(), argbColor);

		// Cursor changes (functions similar to Vanilla's slider widgets)
		if (this.isHovered()) {
			// Apply hand cursor to indicate that the element can be interacted with
			if (this.svRect.containsPoint(mouseX, mouseY) || this.hRect.containsPoint(mouseX, mouseY) || this.aRect.containsPoint(mouseX, mouseY)) {
				context.requestCursor(CursorTypes.POINTING_HAND);
			}

			// Apply crosshair or resize east/west to indicate the element is being interacted with
			if (this.draggingSV) {
				context.requestCursor(CursorTypes.CROSSHAIR);
			} else if (this.draggingH || this.draggingA) {
				context.requestCursor(CursorTypes.RESIZE_EW);
			}
		}
	}

	private void drawThumb(GuiGraphics context, ScreenRectangle rect, int thumbX) {
		context.fill(rect.left() + thumbX - 1, rect.top(), rect.left() + thumbX + 2, rect.bottom(), CommonColors.BLACK);
		context.fill(rect.left() + thumbX, rect.top() - 1, rect.left() + thumbX + 1, rect.bottom() + 1, CommonColors.BLACK);
		context.fill(rect.left() + thumbX, rect.top(), rect.left() + thumbX + 1, rect.bottom(), CommonColors.WHITE);
	}

	public int getARGBColor() {
		return argbColor;
	}

	public void setARGBColor(int argb) {
		this.argbColor = argb | alphaMask;
		float[] floats = Color.RGBtoHSB((argbColor >> 16) & 0xFF, (argbColor >> 8) & 0xFF, argbColor & 0xFF, null);
		setHSV(floats[0], floats[1], floats[2]);
		setAlpha(ARGB.alphaFloat(argbColor));
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
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}

	public interface Callback {

		/**
		 * @param color the new color
		 * @param mouseRelease true if the change is "final" after the user has released the mouse or false when it's from the user dragging on of the thumbs around.
		 */
		void onColorChange(int color, boolean mouseRelease);
	}
}
