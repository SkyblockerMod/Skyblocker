package me.xmrvizzy.skyblocker.skyblock.itemlist;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ResultButtonWidget extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/recipe_book.png");

    protected ItemStack itemStack = null;

    public ResultButtonWidget(int x, int y) {
        super(x, y, 25, 25, Text.of(""));
    }

    protected void setItemStack(ItemStack itemStack) {
        this.active = !itemStack.getItem().equals(Items.AIR);
        this.visible = true;
        this.itemStack = itemStack;
    }

    protected void clearItemStack() {
        this.visible = false;
        this.itemStack = null;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        // this.drawTexture(matrices, this.x, this.y, 29, 206, this.width, this.height);
        context.drawTexture(BACKGROUND_TEXTURE, this.getX(), this.getY(), 29, 206, this.getWidth(), this.getHeight());
        // client.getItemRenderer().renderInGui(this.itemStack, this.x + 4, this.y + 4);
        context.drawItem(this.itemStack, this.getX() + 4, this.getY() + 4);
        // client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, itemStack, this.x + 4, this.y + 4);
        context.drawItemInSlot(client.textRenderer, itemStack, this.getX() + 4, this.getY() + 4);
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Text> tooltip = Screen.getTooltipFromItem(client, this.itemStack);
        List<OrderedText> orderedTooltip = new ArrayList<>();

        for(int i = 0; i < tooltip.size(); i++) {
        	orderedTooltip.add(tooltip.get(i).asOrderedText());
        }

        client.currentScreen.setTooltip(orderedTooltip);
    }

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub

	}
}
