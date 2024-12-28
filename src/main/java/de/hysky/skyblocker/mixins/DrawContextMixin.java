package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

	@Unique
	private static final Identifier ITEM_PROTECTION = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/item_protection.png");

	@Shadow
	public abstract void drawTexture(Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight);

	@Shadow
	public abstract MatrixStack getMatrices();

	@ModifyExpressionValue(method = "drawCooldownProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/ItemStack;F)F"))
    private float skyblocker$modifyItemCooldown(float cooldownProgress, @Local(argsOnly = true) ItemStack stack) {
        return Utils.isOnSkyblock() && ItemCooldowns.isOnCooldown(stack) ? ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent() : cooldownProgress;
    }

	@Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawCooldownProgress(Lnet/minecraft/item/ItemStack;II)V",
					shift = At.Shift.AFTER))
	private void skyblocker$onDrawStackOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, String stackCountText, CallbackInfo ci) {
		if (ItemProtection.isItemProtected(stack)) {
			RenderSystem.enableBlend();
			var matrices = this.getMatrices();
			matrices.push();
			matrices.translate(x, y, 233);
			matrices.scale(0.5F, 0.5F, 1F);
			this.drawTexture(RenderLayer::getGuiTextured, ITEM_PROTECTION, 16, 16, 0, 0, 16, 16, 16, 16);
			matrices.pop();
			RenderSystem.disableBlend();
		}
	}
}
