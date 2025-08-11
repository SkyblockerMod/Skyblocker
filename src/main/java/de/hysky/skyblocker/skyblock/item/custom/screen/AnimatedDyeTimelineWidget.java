package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.OkLabColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnimatedDyeTimelineWidget extends ContainerWidget implements Closeable {

	private static final Identifier GRADIENT_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "generated/dye_gradient");

	private static final int HORIZONTAL_MARGIN = 3;
	private static final int VERTICAL_MARGIN = 1;

	private final NativeImageBackedTexture gradientTexture;
	private final int textureWidth;
	private final int textureHeight;
	private final FrameCallback frameCallback;

	private String uuid = "";

	private final ArrayList<KeyframeWidget> keyframes = new ArrayList<>();
	private @Nullable AnimatedDyeTimelineWidget.KeyframeWidget focusedFrame = null;

	public AnimatedDyeTimelineWidget(int x, int y, int width, int height, FrameCallback frameCallback) {
		super(x, y, width, height, Text.literal("Animated Dye Timeline"));
		gradientTexture = new NativeImageBackedTexture("TimelineGradient", width - HORIZONTAL_MARGIN * 2, height - VERTICAL_MARGIN * 2, true);
		assert gradientTexture.getImage() != null;
		textureWidth = gradientTexture.getImage().getWidth();
		textureHeight = gradientTexture.getImage().getHeight();
		MinecraftClient.getInstance().getTextureManager().registerTexture(GRADIENT_TEXTURE, gradientTexture);
		this.frameCallback = frameCallback;
	}

	@Override
	public List<? extends Element> children() {
		return keyframes;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(RenderLayer::getGuiTextured,
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
	public void setFocused(@Nullable Element focused) {
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
		NativeImage image = gradientTexture.getImage();
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
					image.setColorArgb(x + startX, y, color | 0xFF_00_00_00);
				}
			}
		}
		double v = (System.currentTimeMillis() - l) / 1000.d;
		CustomizeArmorScreen.LOGGER.debug("Time taken to generate gradient texture: {}s", v);
		gradientTexture.upload();
	}

	private int deletedIndex = -1;
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean b = super.mouseClicked(mouseX, mouseY, button);
		if (b) {
			if (deletedIndex != -1) {
				setFocused(keyframes.get(deletedIndex));
				deletedIndex = -1;
			}
			return true;
		}
		if (isMouseOver(mouseX, mouseY)) {
			mouseX -= getX() + HORIZONTAL_MARGIN;
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
			CustomizeArmorScreen.LOGGER.warn("Tried to set color when no frame was focused");
			return;
		}
		focusedFrame.color = argb;
		dataChanged();
	}

	private void dataChanged() {
		keyframes.sort(Comparator.comparingDouble(f -> f.time));
		createGradientTexture();
		List<CustomArmorAnimatedDyes.Keyframe> configFrames = ImmutableList.copyOf(keyframes.stream().map(keyframe -> new CustomArmorAnimatedDyes.Keyframe(keyframe.color, keyframe.time)).toList());
		CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(uuid);
		CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
				configFrames,
				dye.cycleBack(),
				dye.delay(),
				dye.duration()
		);
		SkyblockerConfigManager.get().general.customAnimatedDyes.put(uuid, newDye);
	}

	private class KeyframeWidget extends ClickableWidget {

		int color;
		float time;

		private final boolean draggable;

		private KeyframeWidget(int color, float time, boolean draggable) {
			super(0, AnimatedDyeTimelineWidget.this.getY(), 7, AnimatedDyeTimelineWidget.this.getHeight(), Text.literal("Keyframe"));
			this.draggable = draggable;
			this.color = color;
			this.time = time;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
			context.drawBorder(getX(), getY(), getWidth(), getHeight(), isFocused() ? -1 : Colors.GRAY);
		}

		@Override
		public int getX() {
			AnimatedDyeTimelineWidget parent = AnimatedDyeTimelineWidget.this;
			return (int) (parent.getX() + HORIZONTAL_MARGIN + time * (parent.getWidth() - HORIZONTAL_MARGIN * 2 - 1)) - 3;
		}

		private boolean dragging = false;
		@Override
		protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
			super.onDrag(mouseX, mouseY, deltaX, deltaY);
			if (!draggable) {
				return;
			}
			AnimatedDyeTimelineWidget parent = AnimatedDyeTimelineWidget.this;
			mouseX -= parent.getX() + HORIZONTAL_MARGIN;
			float v = (float) (mouseX / (parent.getWidth() - HORIZONTAL_MARGIN * 2 - 1));
			time = Math.clamp(v, 0, 1);
			dragging = true;
		}

		@Override
		public void onRelease(double mouseX, double mouseY) {
			super.onRelease(mouseX, mouseY);
			if (dragging) dataChanged();
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (keyCode == GLFW.GLFW_KEY_DELETE) {
				deleteThis(false);
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isMouseOver(mouseX, mouseY)) {
				deleteThis(true);
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
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
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	@Override
	protected int getContentsHeightWithPadding() { return getHeight(); }
	@Override
	protected double getDeltaYPerScroll() { return 0; }
	@Override
	public void close() { gradientTexture.close(); }

	public interface FrameCallback {
		void onFrameSelected(int color, float time);
	}
}
