package me.xmrvizzy.skyblocker.skyblock.quicknav;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class QuickNavButton extends ClickableWidget {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final Identifier BUTTON_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    public enum Type {
        TOP,
        BOTTOM,
    }

    private final Type type;
    private boolean toggled;
    private int u;
    private int v;
    private final String command;
    private final ItemStack icon;

    public QuickNavButton(int x, int y, Type type, boolean toggled, String command, ItemStack icon) {
        super(x, y, 28, 32, LiteralText.EMPTY);
        this.type = type;
        if (type == Type.BOTTOM) {
            this.u = 28;
            this.v = 64;
        } else {
            this.u = 28;
            this.v = 0;
        }
        this.toggled = toggled;
        if (toggled) this.v += 32;
        this.command = command;
        this.icon = icon;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.toggled) {
            this.toggled = true;
            this.v += 32;
            CLIENT.player.sendChatMessage(command);
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);
        RenderSystem.disableDepthTest();
        // render button background
        if (!this.toggled) {
            if (this.type == Type.BOTTOM)
                this.drawTexture(matrices, this.x, this.y + 4, this.u, this.v + 4, this.width, this.height - 4);
            else
                this.drawTexture(matrices, this.x, this.y, this.u, this.v, this.width, this.height - 4);
        } else this.drawTexture(matrices, this.x, this.y, this.u, this.v, this.width, this.height);
        // render button icon
        if (!this.toggled) {
            if (this.type == Type.BOTTOM)
                CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 6);
            else
                CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 9);
        } else {
            if (this.type == Type.BOTTOM)
                CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 9);
            else
                CLIENT.getItemRenderer().renderInGui(this.icon,this.x + 6, this.y + 6);
        }
        RenderSystem.enableDepthTest();
    }
}
