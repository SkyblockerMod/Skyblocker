package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class WidgetsConfigToast implements Toast {
	private static final Identifier TEXTURE = SkyblockerMod.id("notification");

	public boolean isVisible = true;
	public final List<FormattedCharSequence> lines;
	public final long duration;

	public WidgetsConfigToast(Component message) {
		Font font = Minecraft.getInstance().font;
		this.lines = font.split(message, 150);
		this.duration = 1_000;
	}

	@Override
	public Visibility getWantedVisibility() {
		return isVisible ? Visibility.SHOW : Visibility.HIDE;
	}

	@Override
	public void update(ToastManager toastManager, long l) {
		if (isVisible && l > duration) isVisible = false;
	}

	@Override
	public void render(GuiGraphics context, Font font, long l) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());
		int y = 0;
		for (FormattedCharSequence line : lines) {
			y += font.lineHeight;
			context.drawString(font, line, 8, y, CommonColors.WHITE, true);
		}
	}
}
