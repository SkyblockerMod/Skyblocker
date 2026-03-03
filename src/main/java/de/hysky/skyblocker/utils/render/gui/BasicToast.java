package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BasicToast implements Toast {
	private static final Identifier TEXTURE = SkyblockerMod.id("notification");

	private final long displayDuration;
	private final @Nullable ItemStack icon;
	private final List<FormattedCharSequence> lines;
	private final int width;
	private final Object token;

	private Visibility visibility = Visibility.SHOW;

	public BasicToast(Component message, long displayDuration, @Nullable ItemStack icon, Object token) {
		Font textRenderer = Minecraft.getInstance().font;
		this.lines = textRenderer.split(message, 200);
		this.displayDuration = displayDuration;
		this.icon = icon;
		this.token = token;
		this.width = lines.stream().mapToInt(textRenderer::width).max().orElse(200) + (icon == null ? 10 : 30);
	}

	public BasicToast(Component message, long displayDuration, @Nullable ItemStack icon) {
		this(message, displayDuration, icon, NO_TOKEN);
	}

	@Override
	public Visibility getWantedVisibility() {
		return visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		if (time > displayDuration) visibility = Visibility.HIDE;
	}

	@Override
	public void render(GuiGraphics context, Font textRenderer, long startTime) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());
		int offset;
		if (icon != null) {
			context.renderFakeItem(icon, 4, 4);
			offset = 20;
		} else {
			offset = 0;
		}
		for (int i = 0; i < lines.size(); i++) {
			context.drawString(textRenderer, lines.get(i), 4 + offset, 8 + i * 12, -1, false);
		}
	}

	@Override
	public int height() {
		return 8 + 4 + Math.max(lines.size(), 1) * 12;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public Object getToken() {
		return token;
	}
}
