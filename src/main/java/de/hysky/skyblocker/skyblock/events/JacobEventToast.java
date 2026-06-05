package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.render.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

import java.util.List;

public class JacobEventToast extends EventToast {
	private final List<String> crops;

	private static final Component CROPS = Component.translatable("skyblocker.events.crops");
	private final int cropsWidth;

	public JacobEventToast(long eventStartTime, long eventEndTime, String name, List<String> crops) {
		super(eventStartTime, eventEndTime, name, Ico.IRON_HOE);
		this.crops = crops;
		Font renderer = Minecraft.getInstance().font;
		cropsWidth = renderer.width(CROPS);

		int i = cropsWidth + 4 + crops.size() * 24;
		messageWidth = Math.max(messageWidth, i);
		messageNowWidth = Math.max(messageNowWidth, i);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font textRenderer, long startTime) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());

		int y = (height() - getInnerContentsHeight()) / 2;
		if (startTime < 3_000) {
			int k = Mth.floor(Math.clamp((3_000 - startTime) / 200.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
			y = 2 + extractMessage(graphics, 30, y, 0xFFFFFF | k);
		} else {
			int k = (~Mth.floor(Math.clamp((startTime - 3_000) / 200.0f, 0.0f, 1.0f) * 255.0f)) << 24 | 0x4000000;


			int x = 30 + cropsWidth + 4;
			graphics.text(textRenderer, CROPS, 30, 7 + (16 - textRenderer.lineHeight) / 2, CommonColors.WHITE, false);
			for (int i = 0; i < crops.size(); i++) {
				graphics.item(JacobsContestWidget.FARM_DATA.getOrDefault(crops.get(i), Ico.IRON_HOE).getStackOrThrow(), x + i * (16 + 8), 7);
			}
			// IDK how to make the items transparent, so I just redraw the texture on top
			GuiHelper.nineSliceColored(graphics, TEXTURE, 0, 0, width(), height(), ARGB.colorFromFloat((k >> 24) / 255f, 1f, 1f, 1f));
			y += textRenderer.lineHeight * message.size();
		}
		extractTimer(graphics, 30, y);

		graphics.fakeItem(icon.getStackOrThrow(), 8, height() / 2 - 8);
	}
}
