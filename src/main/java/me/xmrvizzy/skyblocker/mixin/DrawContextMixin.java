package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import dev.cbyrne.betterinject.annotations.Arg;
import dev.cbyrne.betterinject.annotations.Inject;
import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.skyblock.item.AttributeShards;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void skyblocker$renderAttributeShardDisplay(@Arg TextRenderer textRenderer, @Arg ItemStack stack, @Arg(ordinal = 0) int x, @Arg(ordinal = 1) int y, @Local(argsOnly = true) LocalRef<String> countOverride) {
    	if (!SkyblockerConfigManager.get().general.itemInfoDisplay.attributeShardInfo) return;

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
}