package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.cbyrne.betterinject.annotations.Arg;
import dev.cbyrne.betterinject.annotations.Inject;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.item.ItemCooldowns;
import me.xmrvizzy.skyblocker.skyblock.item.AttributeShards;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.Color;
import java.util.regex.Pattern;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract void fill(RenderLayer layer, int x1, int x2, int y1, int y2, int color);

    @Shadow
    public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    public void skyblocker$renderItemBar(@Arg ItemStack stack, @Arg(ordinal = 0) int x, @Arg(ordinal = 1) int y) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfig.get().locations.dwarvenMines.enableDrillFuel || stack.isEmpty()) {
            return;
        }

        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("ExtraAttributes")) {
            return;
        }

        NbtCompound extraAttributes = tag.getCompound("ExtraAttributes");
        if (!extraAttributes.contains("drill_fuel") && !extraAttributes.getString("id").equals("PICKONIMBUS")) {
            return;
        }
        matrices.push();
        matrices.translate(0f, 0f, 200f);
        RenderSystem.disableDepthTest();

        float current = 0.0F;
        float max = 0.0F;
        String clearFormatting = "";

        for (String line : ItemUtils.getTooltipStrings(stack)) {
            clearFormatting = Formatting.strip(line);
            if (line.contains("Fuel: ")) {
                if (clearFormatting != null) {
                    String clear = Pattern.compile("[^0-9 /]").matcher(clearFormatting).replaceAll("").trim();
                    String[] split = clear.split("/");
                    current = Integer.parseInt(split[0]);
                    max = Integer.parseInt(split[1]) * 1000;
                }
                break;
            } else if (line.contains("uses.")) {
                if (clearFormatting != null) {
                    int startIndex = clearFormatting.lastIndexOf("after") + 6;
                    int endIndex = clearFormatting.indexOf("uses", startIndex);
                    if (startIndex >= 0 && endIndex > startIndex) {
                        String usesString = clearFormatting.substring(startIndex, endIndex).trim();
                        current = Integer.parseInt(usesString);
                        max = 5000;
                    }
                }
                break;
            }
        }

        float hue = Math.max(0.0F, 1.0F - (max - current) / max);
        int width = Math.round(current / max * 13.0F);
        Color color = Color.getHSBColor(hue / 3.0F, 1.0F, 1.0F);
        this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        this.fill(RenderLayer.getGuiOverlay(), x + 2, y + 13, x + 2 + width, y + 14, ColorHelper.Argb.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));

        matrices.pop();
        RenderSystem.enableDepthTest();
    }

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void skyblocker$renderAttributeShardDisplay(@Arg TextRenderer textRenderer, @Arg ItemStack stack, @Arg(ordinal = 0) int x, @Arg(ordinal = 1) int y, @Local(argsOnly = true) LocalRef<String> countOverride) {
    	if (!SkyblockerConfig.get().general.itemInfoDisplay.attributeShardInfo) return;

    	NbtCompound nbt = stack.getNbt();

    	if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
    		NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");

    		if (extraAttributes.getString("id").equals("ATTRIBUTE_SHARD")) {
    			NbtCompound attributesTag = extraAttributes.getCompound("attributes");
    			String[] attributes = attributesTag.getKeys().toArray(String[]::new);

    			if (attributes.length != 0) {
    				String attributeId = attributes[0];
    				int attributeLevel = attributesTag.getInt(attributeId);

    				//Set item count
    				countOverride.set(Integer.toString(attributeLevel));

    				//Draw the attribute name
    				this.matrices.push();
    				this.matrices.translate(0f, 0f, 200f);

    				String attributeInitials = AttributeShards.getShortName(attributeId);

    				this.drawText(textRenderer, attributeInitials, x, y, Formatting.AQUA.getColorValue(), true); 

    				this.matrices.pop();
    			}
    		}
    	}
    }

    @ModifyExpressionValue(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/Item;F)F"))
    private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local ItemStack stack) {
        if (Utils.isOnSkyblock() && ItemCooldowns.isItemOnCooldown(stack)) {
            return ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent();
        }

        return cooldownProgress;
    }
}
