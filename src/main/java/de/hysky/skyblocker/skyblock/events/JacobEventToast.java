package de.hysky.skyblocker.skyblock.events;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class JacobEventToast extends EventToast{

    private final String[] crops;

    public static final Map<String, ItemStack> cropItems = new HashMap<>();

    static {
        cropItems.put("Wheat", new ItemStack(Items.WHEAT));
        cropItems.put("Mushroom", new ItemStack(Items.RED_MUSHROOM));
        cropItems.put("Pumpkin", new ItemStack(Items.CARVED_PUMPKIN));
        cropItems.put("Melon", new ItemStack(Items.MELON));
        cropItems.put("Sugar Cane", new ItemStack(Items.SUGAR_CANE));
        cropItems.put("Cactus", new ItemStack(Items.CACTUS));
        cropItems.put("Carrot", new ItemStack(Items.CARROT));
        cropItems.put("Cocoa Beans", new ItemStack(Items.COCOA_BEANS));
        cropItems.put("Potato", new ItemStack(Items.POTATO));
        cropItems.put("Nether Wart", new ItemStack(Items.NETHER_WART));
    }

    public JacobEventToast(long eventStartTime, String name, String[] crops) {
        super(eventStartTime, name, new ItemStack(Items.IRON_HOE));
        this.crops = crops;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, 0, getWidth(), getHeight());

        int y = (getHeight() - getInnerContentsHeight())/2;
        TextRenderer textRenderer = manager.getClient().textRenderer;
        if (startTime < 3_000){
            int k = MathHelper.floor(MathHelper.clamp((3_000 - startTime) / 200.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
            y = 2 + drawMessage(context, 30, y, 0xFFFFFF | k);
        } else  {
            int k = (~MathHelper.floor(MathHelper.clamp((startTime - 3_000) / 200.0f, 0.0f, 1.0f) * 255.0f)) << 24 | 0x4000000;


            String s = "Crops:";
            int x = 30 + textRenderer.getWidth(s) + 4;
            context.drawText(textRenderer, s, 30, 7 + (16 - textRenderer.fontHeight)/2, Colors.WHITE, false);
            for (int i = 0; i < crops.length; i++) {
                context.drawItem(cropItems.get(crops[i]), x + i * (16 + 8), 7);
            }
            // IDK how to make the items transparent, so I just redraw the texture on top
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 400f);
            RenderHelper.renderNineSliceColored(context, TEXTURE, 0, 0, getWidth(), getHeight(), 1f, 1f, 1f, (k >> 24)/ 255f);
            context.getMatrices().pop();
            y += textRenderer.fontHeight * message.size();
        }
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 400f);
        drawTimer(context, 30, y);

        context.drawItemWithoutEntity(icon, 8, getHeight()/2 - 8);
        context.getMatrices().pop();
        return startTime > 5_000 ? Visibility.HIDE: Visibility.SHOW;
    }
}
