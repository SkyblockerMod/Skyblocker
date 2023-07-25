package me.xmrvizzy.skyblocker.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.regex.Pattern;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract void fill(RenderLayer layer, int x1, int x2, int y1, int y2, int color);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    public void skyblocker$renderItemBar(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfig.get().locations.dwarvenMines.enableDrillFuel) {
            if (!stack.isEmpty()) {
                NbtCompound tag = stack.getNbt();
                if (tag != null && tag.contains("ExtraAttributes")) {
                    if (tag.getCompound("ExtraAttributes").contains("drill_fuel")) {
                        float current = 3000.0F;
                        float max = 3000.0F;

                        for (String line : ItemUtils.getTooltipStrings(stack)) {
                            if (line.contains("Fuel: ")) {
                                String clear = Pattern.compile("[^0-9 /]").matcher(line).replaceAll("").trim();
                                String[] split = clear.split("/");
                                current = Integer.parseInt(split[0]);
                                max = Integer.parseInt(split[1]) * 1000;
                                break;
                            }
                        }

                        matrices.push();
                        matrices.translate(0f, 0f, 200f);
                        RenderSystem.disableDepthTest();

                        float hue = Math.max(0.0F, 1.0F - (max - current) / max);
                        int width = Math.round(current / max * 13.0F);
                        Color color = Color.getHSBColor(hue / 3.0F, 1.0F, 1.0F);
                        this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 13, x + 15, y + 15, 0xFF000000);
                        this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 13, x + 2 + width, y + 14, ColorHelper.Argb.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));

                        matrices.pop();
                        RenderSystem.enableDepthTest();
                    }
                }
            }
        }
    }
}