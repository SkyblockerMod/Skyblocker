package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class JacobEventToast extends EventToast {
	private final String[] crops;

	private static final ItemStack DEFAULT_ITEM = new ItemStack(Items.IRON_HOE);
	private static final Component CROPS = Component.translatable("skyblocker.events.crops");
	private final int cropsWidth;

	public JacobEventToast(long eventStartTime, String name, String[] crops) {
		super(eventStartTime, name, new ItemStack(Items.IRON_HOE));
		this.crops = crops;
		Font renderer = Minecraft.getInstance().font;
		cropsWidth = renderer.width(CROPS);

		int i = cropsWidth + 4 + crops.length * 24;
		messageWidth = Math.max(messageWidth, i);
		messageNowWidth = Math.max(messageNowWidth, i);
	}

	@Override
	public void render(GuiGraphics context, Font textRenderer, long startTime) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());

		int y = (height() - getInnerContentsHeight()) / 2;
		if (startTime < 3_000) {
			int k = Mth.floor(Math.clamp((3_000 - startTime) / 200.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
			y = 2 + drawMessage(context, 30, y, 0xFFFFFF | k);
		} else {
			int k = (~Mth.floor(Math.clamp((startTime - 3_000) / 200.0f, 0.0f, 1.0f) * 255.0f)) << 24 | 0x4000000;


			int x = 30 + cropsWidth + 4;
			context.drawString(textRenderer, CROPS, 30, 7 + (16 - textRenderer.lineHeight) / 2, CommonColors.WHITE, false);
			for (int i = 0; i < crops.length; i++) {
				context.renderItem(JacobsContestWidget.FARM_DATA.getOrDefault(crops[i], DEFAULT_ITEM), x + i * (16 + 8), 7);
			}
			// IDK how to make the items transparent, so I just redraw the texture on top
			HudHelper.renderNineSliceColored(context, TEXTURE, 0, 0, width(), height(), ARGB.colorFromFloat((k >> 24) / 255f, 1f, 1f, 1f));
			y += textRenderer.lineHeight * message.size();
		}
		drawTimer(context, 30, y);

		context.renderFakeItem(icon, 8, height() / 2 - 8);
	}
}
