package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.mojang.blaze3d.platform.NativeImage;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.render.HudHelper;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class AnimatedDyeTimelineWidget extends AbstractContainerWidget implements Closeable {
	private static final Identifier GRADIENT_TEXTURE = SkyblockerMod.id("generated/dye_gradient");
	private static final int HORIZONTAL_MARGIN = 3;
	private static final int VERTICAL_MARGIN = 1;

	private DynamicTexture gradientTexture;
	private int textureWidth;
	private int textureHeight;
	private final FrameCallback frameCallback;

	private String uuid = "";

	private final ArrayList<KeyframeWidget> keyframes = new ArrayList<>();
	private @Nullable AnimatedDyeTimelineWidget.KeyframeWidget focusedFrame = null;

	public AnimatedDyeTimelineWidget(int x, int y, int width, int height, FrameCallback frameCallback) {
		super(x, y, width, height, Component.literal("Animated Dye Timeline"));
		createImage(width, height);
		this.frameCallback = frameCallback;
	}

	private void createImage(int width, int height) {
		gradientTexture = new DynamicTexture("TimelineGradient", width - HORIZONTAL_MARGIN * 2, height - VERTICAL_MARGIN * 2, true);
		assert gradientTexture.getPixels() != null;
		textureWidth = gradientTexture.getPixels().getWidth();
		textureHeight = gradientTexture.getPixels().getHeight();
		Minecraft.getInstance().getTextureManager().register(GRADIENT_TEXTURE, gradientTexture);
	}

	/**
	 * Called when the screen has been displayed again after a popup
	 */
	public void recreateImage() {
		createImage(width, height);
		createGradientTexture();
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		createImage(width, height);
		createGradientTexture();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return keyframes;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blit(RenderPipelines.GUI_TEXTURED,
				GRADIENT_TEXTURE,
				getX() + HORIZONTAL_MARGIN,
				getY() + VERTICAL_MARGIN,
				0, 0,
				getWidth() - HORIZONTAL_MARGIN * 2,
				getHeight() - VERTICAL_MARGIN * 2,
				textureWidth, textureHeight,
				textureWidth, textureHeight
		);
		for (KeyframeWidget frame : keyframes) {
			frame.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		super.setFocused(focused);
		if (focused instanceof KeyframeWidget keyframe) {
			frameCallback.onFrameSelected(keyframe.color, keyframe.time);
			focusedFrame = keyframe;
		}
	}

	public void setAnimatedDye(String uuid) {
		this.uuid = uuid;
		CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(uuid);
		keyframes.clear();
		keyframes.ensureCapacity(dye.keyframes().size());
		for (int i = 0; i < dye.keyframes().size(); i++) {
			CustomArmorAnimatedDyes.Keyframe keyframe = dye.keyframes().get(i);
			keyframes.add(new KeyframeWidget(keyframe.color(), keyframe.time(), i != 0 && i != dye.keyframes().size() - 1));
		}
		setFocused(keyframes.getFirst());
		createGradientTexture();
	}

	private void createGradientTexture() {
		NativeImage image = gradientTexture.getPixels();
		assert image != null;
		long l = System.currentTimeMillis();
		for (int i = 0; i < keyframes.size() - 1; i++) {
			KeyframeWidget frame = keyframes.get(i);
			KeyframeWidget nextFrame = keyframes.get(i + 1);
			int startX = (int) ((image.getWidth() - 1) * frame.time);
			int endX = (int) ((image.getWidth() - 1) * nextFrame.time);
			int size = endX - startX;
			for (int x = 0; x <= size; x++) {
				int color = OkLabColor.interpolate(frame.color, nextFrame.color, (float) x / size);
				for (int y = 0; y < image.getHeight(); y++) {
					image.setPixel(x + startX, y, color | 0xFF_00_00_00);
				}
			}
		}
		double v = (System.currentTimeMillis() - l) / 1000.d;
		CustomizeScreen.LOGGER.debug("Time taken to generate gradient texture: {}s", v);
		gradientTexture.upload();
	}

	private int deletedIndex = -1;
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean b = super.mouseClicked(click, doubled);
		if (b) {
			if (deletedIndex != -1) {
				setFocused(keyframes.get(deletedIndex));
				deletedIndex = -1;
			}
			return true;
		}
		if (isMouseOver(click.x(), click.y())) {
			double mouseX = click.x() - getX() + HORIZONTAL_MARGIN;
			KeyframeWidget e = new KeyframeWidget(0xFFFF0000, (float) (mouseX / (getWidth() - HORIZONTAL_MARGIN * 2 - 1)), true);
			keyframes.add(e);
			setFocused(e);
			dataChanged();
			return true;
		}
		return false;
	}

	public void setColor(int argb) {
		if (focusedFrame == null) {
			CustomizeScreen.LOGGER.warn("Tried to set color when no frame was focused");
			return;
		}
		focusedFrame.color = argb;
		dataChanged();
	}

	private void dataChanged() {
		keyframes.sort(Comparator.comparingDouble(f -> f.time));
		createGradientTexture();
		List<CustomArmorAnimatedDyes.Keyframe> configFrames = List.copyOf(keyframes.stream().map(keyframe -> new CustomArmorAnimatedDyes.Keyframe(keyframe.color, keyframe.time)).toList());
		CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(uuid);
		CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
				configFrames,
				dye.cycleBack(),
				dye.delay(),
				dye.duration()
		);
		SkyblockerConfigManager.update(config -> config.general.customAnimatedDyes.put(uuid, newDye));
	}

	private class KeyframeWidget extends AbstractWidget {

		int color;
		float time;

		private final boolean draggable;

		private KeyframeWidget(int color, float time, boolean draggable) {
			super(0, 0, 7, AnimatedDyeTimelineWidget.this.getHeight(), Component.literal("Keyframe"));
			this.draggable = draggable;
			this.color = color;
			this.time = time;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
			HudHelper.drawBorder(context, getX(), getY(), getWidth(), getHeight(), isFocused() ? -1 : CommonColors.GRAY);
		}

		@Override
		public int getX() {
			AnimatedDyeTimelineWidget parent = AnimatedDyeTimelineWidget.this;
			return (int) (parent.getX() + HORIZONTAL_MARGIN + time * (parent.getWidth() - HORIZONTAL_MARGIN * 2 - 1)) - 3;
		}

		@Override
		public int getY() {
			return AnimatedDyeTimelineWidget.this.getY();
		}

		private boolean dragging = false;
		@Override
		protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
			super.onDrag(click, offsetX, offsetY);
			if (!draggable) {
				return;
			}
			AnimatedDyeTimelineWidget parent = AnimatedDyeTimelineWidget.this;
			double mouseX = click.x() - parent.getX() + HORIZONTAL_MARGIN;
			float v = (float) (mouseX / (parent.getWidth() - HORIZONTAL_MARGIN * 2 - 1));
			time = Math.clamp(v, 0, 1);
			dragging = true;
		}

		@Override
		public void onRelease(MouseButtonEvent click) {
			super.onRelease(click);
			if (dragging) dataChanged();
		}

		@Override
		public boolean keyPressed(KeyEvent input) {
			if (input.key() == GLFW.GLFW_KEY_DELETE) {
				deleteThis(false);
			}
			return super.keyPressed(input);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isMouseOver(click.x(), click.y())) {
				deleteThis(true);
				return true;
			}
			return super.mouseClicked(click, doubled);
		}

		private void deleteThis(boolean mouse) {
			if (!draggable) return;
			int i = keyframes.indexOf(this);
			AnimatedDyeTimelineWidget.this.setFocused(keyframes.get(i + 1));
			if (mouse) deletedIndex = i;
			keyframes.remove(this);
			dataChanged();
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected int contentHeight() {
		return getHeight();
	}

	@Override
	protected double scrollRate() {
		return 0;
	}

	@Override
	public void close() {
		Minecraft.getInstance().getTextureManager().release(GRADIENT_TEXTURE);
	}

	public interface FrameCallback {
		void onFrameSelected(int color, float time);
	}
}
