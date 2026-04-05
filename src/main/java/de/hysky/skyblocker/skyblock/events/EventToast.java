package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.SkyblockTime;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

public class EventToast implements Toast {
	protected static final Identifier TEXTURE = SkyblockerMod.id("notification");

	private long toastTime = 0;
	private final long eventStartTime;
	private final long eventEndTime;

	protected final List<FormattedCharSequence> message;
	protected final List<FormattedCharSequence> messageNow;
	protected int messageWidth;
	protected int messageNowWidth;
	protected final FlexibleItemStack icon;

	protected boolean started;

	public EventToast(long eventStartTime, long eventEndTime, String name, FlexibleItemStack icon) {
		this.eventStartTime = eventStartTime;
		this.eventEndTime = eventEndTime;

		MutableComponent formatted = Component.translatable("skyblocker.events.startsSoon", Component.literal(name).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE);
		Font renderer = Minecraft.getInstance().font;
		message = renderer.split(formatted, 150);
		messageWidth = message.stream().mapToInt(renderer::width).max().orElse(150);

		MutableComponent formattedNow = Component.translatable("skyblocker.events.startsNow", Component.literal(name).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE);
		messageNow = renderer.split(formattedNow, 150);
		messageNowWidth = messageNow.stream().mapToInt(renderer::width).max().orElse(150);
		this.icon = icon;
		this.started = eventStartTime - System.currentTimeMillis() / 1000 < 0;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font textRenderer, long startTime) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());

		int y = (height() - getInnerContentsHeight())/2;
		y = 2 + extractMessage(graphics, 30, y, CommonColors.WHITE);
		extractTimer(graphics, 30, y);

		graphics.fakeItem(icon.getStackOrThrow(), 8, height()/2 - 8);
	}

	protected int extractMessage(GuiGraphicsExtractor graphics, int x, int y, int color) {
		Font textRenderer = Minecraft.getInstance().font;
		for (FormattedCharSequence orderedText : started ? messageNow : message) {
			graphics.text(textRenderer, orderedText, x, y, color, false);
			y += textRenderer.lineHeight;
		}
		return y;
	}

	protected void extractTimer(GuiGraphicsExtractor graphics, int x, int y) {
		long currentTime = System.currentTimeMillis() / 1000;
		int timeTillEvent = (int) (eventStartTime - currentTime);
		started = timeTillEvent < 0;
		if (started) return;

		Component time = SkyblockTime.formatTime(timeTillEvent);

		Font textRenderer = Minecraft.getInstance().font;
		graphics.text(textRenderer, time, x, y, CommonColors.SOFT_YELLOW, false);
	}

	@Override
	public int width() {
		return (started ? messageNowWidth : messageWidth) + 30 + 6;
	}

	protected int getInnerContentsHeight() {
		return message.size() * 9 + (started ? 0 : 9);
	}

	@Override
	public int height() {
		return Math.max(getInnerContentsHeight() + 12 + 2, 32);
	}

	@Override
	public Visibility getWantedVisibility() {
		long currentTime = System.currentTimeMillis() / 1000;
		if (toastTime > 5_000 || currentTime > eventEndTime) return Visibility.HIDE;
		return Visibility.SHOW;
	}

	@Override
	public void update(ToastManager manager, long time) {
		this.toastTime = time;
	}
}
