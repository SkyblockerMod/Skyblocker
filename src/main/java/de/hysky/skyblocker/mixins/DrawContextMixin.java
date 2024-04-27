package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.AttributeShards;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void skyblocker$renderAttributeShardDisplay(CallbackInfo ci, @Local(argsOnly = true) TextRenderer textRenderer, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true) LocalRef<String> countOverride) {
        if (!SkyblockerConfigManager.get().general.itemInfoDisplay.attributeShardInfo) return;

        if (Utils.isOnSkyblock()) {
            NbtCompound customData = ItemUtils.getCustomData(stack);

            if (ItemUtils.getItemId(stack).equals("ATTRIBUTE_SHARD")) {
                NbtCompound attributesTag = customData.getCompound("attributes");
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
    private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(argsOnly = true) ItemStack stack) {
        return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
    }
}
