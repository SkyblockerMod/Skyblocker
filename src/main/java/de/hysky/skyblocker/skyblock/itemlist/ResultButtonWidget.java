package de.hysky.skyblocker.skyblock.itemlist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ResultButtonWidget extends ClickableWidget {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("recipe_book/slot_craftable");

    protected ItemStack itemStack = null;

    public ResultButtonWidget(int x, int y) {
        super(x, y, 25, 25, Text.literal(""));
    }

    protected void setItemStack(ItemStack itemStack) {
        this.active = !itemStack.isEmpty();
        this.visible = true;
        this.itemStack = itemStack;
    }

    protected void clearItemStack() {
        this.visible = false;
        this.itemStack = null;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawGuiTexture(BACKGROUND_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        context.drawItem(this.itemStack, this.getX() + 4, this.getY() + 4);
        context.drawItemInSlot(client.textRenderer, itemStack, this.getX() + 4, this.getY() + 4);
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return;
        List<Text> tooltip = Screen.getTooltipFromItem(client, this.itemStack);
        client.currentScreen.setTooltip(tooltip.stream().map(Text::asOrderedText).toList());
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
