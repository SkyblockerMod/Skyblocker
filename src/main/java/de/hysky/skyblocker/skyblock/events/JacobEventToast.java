package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class JacobEventToast extends EventToast {
    private final String[] crops;

    private static final ItemStack DEFAULT_ITEM = new ItemStack(Items.IRON_HOE);
	private static final Text CROPS = Text.translatable("skyblocker.events.crops");
	private final int cropsWidth;

	public JacobEventToast(long eventStartTime, String name, String[] crops) {
        super(eventStartTime, name, new ItemStack(Items.IRON_HOE));
        this.crops = crops;
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		cropsWidth = renderer.getWidth(CROPS);

		int i = cropsWidth + 4 + crops.length * 24;
		messageWidth = Math.max(messageWidth, i);
		messageNowWidth = Math.max(messageNowWidth, i);
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, getWidth(), getHeight());

        int y = (getHeight() - getInnerContentsHeight()) / 2;
        if (startTime < 3_000) {
            int k = MathHelper.floor(Math.clamp((3_000 - startTime) / 200.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
            y = 2 + drawMessage(context, 30, y, 0xFFFFFF | k);
        } else {
            int k = (~MathHelper.floor(Math.clamp((startTime - 3_000) / 200.0f, 0.0f, 1.0f) * 255.0f)) << 24 | 0x4000000;


            int x = 30 + cropsWidth + 4;
            context.drawText(textRenderer, CROPS, 30, 7 + (16 - textRenderer.fontHeight) / 2, Colors.WHITE, false);
            for (int i = 0; i < crops.length; i++) {
                context.drawItem(JacobsContestWidget.FARM_DATA.getOrDefault(crops[i], DEFAULT_ITEM), x + i * (16 + 8), 7);
            }
            // IDK how to make the items transparent, so I just redraw the texture on top
            HudHelper.renderNineSliceColored(context, TEXTURE, 0, 0, getWidth(), getHeight(), ColorHelper.fromFloats((k >> 24) / 255f, 1f, 1f, 1f));
            y += textRenderer.fontHeight * message.size();
        }
        drawTimer(context, 30, y);

        context.drawItemWithoutEntity(icon, 8, getHeight() / 2 - 8);
    }
}
