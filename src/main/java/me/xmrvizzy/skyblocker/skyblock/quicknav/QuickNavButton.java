package me.xmrvizzy.skyblocker.skyblock.quicknav;

import com.mojang.blaze3d.systems.RenderSystem;

import me.xmrvizzy.skyblocker.mixin.accessor.HandledScreenAccessor;
import me.xmrvizzy.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class QuickNavButton extends ClickableWidget {
    private static final Identifier BUTTON_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");

    private final int index;
    private boolean toggled;
    private int u;
    private int v;
    private final String command;
    private final ItemStack icon;

    public QuickNavButton(int index, boolean toggled, String command, ItemStack icon) {
        super(0, 0, 26, 32, Text.empty());
        this.index = index;
        this.toggled = toggled;
        this.command = command;
        this.icon = icon;
    }

    private void updateCoordinates() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof HandledScreen<?> handledScreen) {
            int x = ((HandledScreenAccessor) handledScreen).getX();
            int y = ((HandledScreenAccessor) handledScreen).getY();
            int h = ((HandledScreenAccessor) handledScreen).getBackgroundHeight();
            if (h > 166) --h; // why is this even a thing
            this.setX(x + this.index % 6 * 26 + 4);
            this.setY(this.index < 6 ? y - 26 : y + h - 4);
            this.u = 26;
            this.v = (index < 6 ? 0 : 64) + (toggled ? 32 : 0);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.toggled) {
            this.toggled = true;
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(command);
            // TODO : add null check with log error
        }
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        this.updateCoordinates();
        MatrixStack matrices = context.getMatrices();
        RenderSystem.disableDepthTest();
        // render button background
        if (!this.toggled) {
            if (this.index >= 6)
                // this.drawTexture(matrices, this.x, this.y + 4, this.u, this.v + 4, this.width, this.height - 4);
                context.drawTexture(BUTTON_TEXTURE, this.getX(), this.getY() + 4, this.u, this.v + 4, this.width, this.height - 4);
            else
                // this.drawTexture(matrices, this.x, this.y, this.u, this.v, this.width, this.height - 4);
                context.drawTexture(BUTTON_TEXTURE, this.getX(), this.getY() - 2, this.u, this.v, this.width, this.height - 4);
            // } else this.drawTexture(matrices, this.x, this.y, this.u, this.v, this.width, this.height);
        } else {
            matrices.push();
            //Move the top buttons 2 pixels up if they're selected
            if (this.index < 6) matrices.translate(0f, -2f, 0f);
            context.drawTexture(BUTTON_TEXTURE, this.getX(), this.getY(), this.u, this.v, this.width, this.height);
            matrices.pop();
        }
        // render button icon
        if (!this.toggled) {
            if (this.index >= 6)
                // CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 6);
                context.drawItem(this.icon, this.getX() + 5, this.getY() + 6);
            else
                // CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 9);
                context.drawItem(this.icon, this.getX() + 5, this.getY() + 7);
        } else {
            if (this.index >= 6)
                // CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 9);
                context.drawItem(this.icon, this.getX() + 5, this.getY() + 9);
            else
                // CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 6);
                context.drawItem(this.icon, this.getX() + 5, this.getY() + 6);
        }
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO Auto-generated method stub

    }
}
