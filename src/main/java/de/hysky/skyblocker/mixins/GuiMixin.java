package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Unique
	private static final Supplier<Identifier> SLOT_LOCK_ICON = () -> SkyblockerConfigManager.get().general.itemProtection.slotLockStyle.tex;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private boolean isQuiverSlot = false;

	@Inject(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0))
	public void skyblocker$extractHotbarItemLockOrBackground(CallbackInfo ci, @Local(name = "graphics") GuiGraphicsExtractor graphics, @Local(name = "i") int index, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "player") Player player) {
		if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(player.getInventory().getNonEquipmentItems().get(index), graphics, x, y);

			// slot lock
			if (HotbarSlotLock.isLocked(index)) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, SLOT_LOCK_ICON.get(), x, y, 0, 0, 16, 16, 16, 16);
			}

			//item protection
			if (ItemProtection.isItemProtected(player.getInventory().getNonEquipmentItems().get(index))) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
			}
			isQuiverSlot = index == 8;
		}
	}

	@Unique
	private static int prevHash = 0;
	@Unique
	private static boolean prevQuiverSlot = false;

	@Unique
	private static boolean isQuiverItem(ItemStack stack) {
		int hashCode = System.identityHashCode(stack);
		if (hashCode == prevHash) {
			return prevQuiverSlot;
		}
		prevHash = hashCode;
		CustomData component = stack.get(DataComponents.CUSTOM_DATA);
		if (component == null) return false;
		CompoundTag compound = component.copyTag();
		Tag element = compound.get("quiver_arrow");
		prevQuiverSlot = element != null && (element.asBoolean().orElse(false) || element.asString().orElse("false").equals("true"));
		return prevQuiverSlot;
	}

	@WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
	private void skyblocker$extractQuiverAmount(GuiGraphicsExtractor graphics, Font textRenderer, ItemStack stack, int x, int y, Operation<Void> original) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.trueQuiverCount && isQuiverSlot && isQuiverItem(stack)) {
			String arrow = ItemUtils.getLoreLineIf(stack, s -> s.trim().startsWith("Active Arrow"));
			if (arrow == null) {
				original.call(graphics, textRenderer, stack, x, y);
				return;
			}
			int i = arrow.lastIndexOf('(');
			int j = arrow.lastIndexOf(')');
			if (i == -1 || j == -1 || i > j) {
				original.call(graphics, textRenderer, stack, x, y);
				return;
			}
			arrow = arrow.substring(i + 1, j);
			OptionalInt anInt = Utils.parseInt(arrow);
			if (anInt.isEmpty()) {
				original.call(graphics, textRenderer, stack, x, y);
				return;
			}
			String format = Formatters.SHORT_INTEGER_NUMBERS.format(anInt.getAsInt());
			graphics.itemDecorations(textRenderer, stack, x, y, format);
		} else {
			original.call(graphics, textRenderer, stack, x, y);
		}
	}

	@ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	private float skyblocker$modifyAttackIndicatorCooldown(float cooldownProgress) {
		if (Utils.isOnSkyblock() && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (ItemCooldowns.isOnCooldown(stack)) {
				return ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent();
			}
		}

		return cooldownProgress;
	}
}
