package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipAppender;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	public abstract int getDamage();

	@Shadow
	public abstract void setDamage(int damage);

	@Unique
	private int maxDamage;

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock()) {
			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(ItemUtils.getItemUuid((ItemStack) (Object) this), original);
		}

		return original;
	}

	@ModifyVariable(method = "appendTooltip", at = @At("STORE"))
	private TooltipAppender skyblocker$hideVanillaEnchants(TooltipAppender original) {
		return Utils.isOnSkyblock() && original instanceof ItemEnchantmentsComponent component ? component.withShowInTooltip(false) : original;
	}

	/**
	 * Updates the durability of this item stack every tick when in the inventory.
	 */
	@Inject(method = "inventoryTick", at = @At("TAIL"))
	private void skyblocker$updateDamage(CallbackInfo ci) {
		if (!skyblocker$shouldProcess()) {
			return;
		}
		skyblocker$getAndCacheDurability();
	}

	@ModifyReturnValue(method = "getDamage", at = @At("RETURN"))
	private int skyblocker$handleDamage(int original) {
		// If the durability is already calculated, the original value should be the damage
		if (!skyblocker$shouldProcess() || maxDamage != 0) {
			return original;
		}
		return skyblocker$getAndCacheDurability() ? getDamage() : original;
	}

	@ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
	private int skyblocker$handleMaxDamage(int original) {
		if (!skyblocker$shouldProcess()) {
			return original;
		}
		// If the max damage is already calculated, return it
		if (maxDamage != 0) {
			return maxDamage;
		}
		return skyblocker$getAndCacheDurability() ? maxDamage : original;
	}

	@ModifyReturnValue(method = "isDamageable", at = @At("RETURN"))
	private boolean skyblocker$handleDamageable(boolean original) {
		return skyblocker$shouldProcess() || original;
	}

	@ModifyReturnValue(method = "isDamaged", at = @At("RETURN"))
	private boolean skyblocker$handleDamaged(boolean original) {
		return skyblocker$shouldProcess() || original;
	}

	@Unique
	private boolean skyblocker$shouldProcess() {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().locations.dwarvenMines.enableDrillFuel && ItemUtils.hasCustomDurability((ItemStack) (Object) this);
	}

	@Unique
	private boolean skyblocker$getAndCacheDurability() {
		// Calculate the durability
		IntIntPair durability = ItemUtils.getDurability((ItemStack) (Object) this);
		// Return if calculating the durability failed
		if (durability == null) {
			return false;
		}
		// Saves the calculated durability
		maxDamage = durability.rightInt();
		setDamage(durability.rightInt() - durability.leftInt());
		return true;
	}
}
