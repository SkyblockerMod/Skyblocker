package me.xmrvizzy.skyblocker.skyblock.itemlist;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        // this.drawTexture(matrices, this.x, this.y, 29, 206, this.width, this.height);
        DrawableHelper.drawTexture(matrices, this.getX(), this.getY(), 29, 206, this.getWidth(), this.getHeight());
        // client.getItemRenderer().renderInGui(this.itemStack, this.x + 4, this.y + 4);
        client.getItemRenderer().renderInGui(matrices, this.itemStack, this.getX() + 4, this.getY() + 4);
        // client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, itemStack, this.x + 4, this.y + 4);
        client.getItemRenderer().renderGuiItemOverlay(matrices, client.textRenderer, itemStack, this.getX() + 4, this.getY() + 4);
    }

    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Text> tooltip = client.currentScreen.getTooltipFromItem(this.itemStack);
        // TODO : add null check with log error
        client.currentScreen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub
		
	}
}
