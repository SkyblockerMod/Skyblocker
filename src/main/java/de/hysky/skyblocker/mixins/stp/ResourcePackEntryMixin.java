package de.hysky.skyblocker.mixins.stp;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.stp.SkyblockerPackMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(PackListWidget.ResourcePackEntry.class)
public class ResourcePackEntryMixin {
	@Shadow
	@Final
	private PackListWidget widget;
	@Shadow
	@Final
	protected MinecraftClient client;
	@Shadow
	@Final
	private ResourcePackOrganizer.Pack pack;

	@Shadow
	private static MultilineText createMultilineText(MinecraftClient client, Text text) {
		return null;
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void skyblocker$setSTPMetadata(CallbackInfo ci, @Share("skyblockerMetadata") LocalRef<SkyblockerPackMetadata> skyblockerMetadata) {
		if (this.pack instanceof AbstractPackAccessor abstractPack && abstractPack.getProfile().getSkyblockerMetadata() != null) {
			skyblockerMetadata.set(abstractPack.getProfile().getSkyblockerMetadata());
		}
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackCompatibility;isCompatible()Z"))
	private boolean skyblocker$showSTPIncompatibilities(boolean original, @Share("skyblockerMetadata") LocalRef<SkyblockerPackMetadata> skyblockerMetadata) {
		return skyblockerMetadata.get() != null && !skyblockerMetadata.get().compatibility().isCompatible() ? false : original;
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/pack/PackListWidget$ResourcePackEntry;compatibilityNotificationText:Lnet/minecraft/client/font/MultilineText;", opcode = Opcodes.GETFIELD))
	private MultilineText skyblocker$stpIncompatibleDescription(MultilineText original, @Share("skyblockerMetadata") LocalRef<SkyblockerPackMetadata> skyblockerMetadata) {
		if (skyblockerMetadata.get() != null && !skyblockerMetadata.get().compatibility().isCompatible()) {
			return createMultilineText(client, Text.translatable("skyblocker.pack.outdated").formatted(Formatting.GRAY));
		}

		return original;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V"))
	private void skyblocker$drawStarIcon(CallbackInfo ci, @Local(argsOnly = true) DrawContext context, @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int x, @Local(argsOnly = true, ordinal = 3) int entryWidth, @Share("skyblockerMetadata") LocalRef<SkyblockerPackMetadata> skyblockerMetadata) {
		if (skyblockerMetadata.get() != null) {
			boolean isScrollbarVisible = ((EntryListWidgetInvoker) this.widget).invokeIsScrollbarVisible();
			int rightX = x + entryWidth - 3 - (isScrollbarVisible ? 7 : 0);

			//The adjusted coordinates are so that it doesn't render off screen and the top of the star is underneath the border outline (if selected)
			context.drawTexture(ItemProtection.ITEM_PROTECTION_TEX, rightX - 16, y - 12, 0, 0, 16, 16, 16, 16);
		}
	}
}
