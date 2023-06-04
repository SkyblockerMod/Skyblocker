package me.xmrvizzy.skyblocker.mixin;

import java.awt.Color;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ColorHelper;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    public void skyblocker$renderItemBar(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel, CallbackInfo ci) {

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

                        RenderSystem.disableDepthTest();
                        float hue = Math.max(0.0F, 1.0F - (max - current) / max);
                        int width = Math.round(current / max * 13.0F);
                        Color color = Color.getHSBColor(hue / 3.0F, 1.0F, 1.0F);
                        DrawableHelper.fill(matrices, x + 2, y + 13, x + 15, y + 15, 0xFF000000);
                        DrawableHelper.fill(matrices, x + 2, y + 13, x + 2 + width, y + 14, ColorHelper.Argb.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
                        RenderSystem.enableDepthTest();
                    }
                }
            }
        }
    }
}