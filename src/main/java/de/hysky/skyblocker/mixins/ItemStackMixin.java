package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, SkyblockerStack {
	@Unique
	private int maxDamage;

	@Unique
	private String skyblockId;

	@Unique
	private String skyblockApiId;

	@Unique
	private String neuName;

	@Unique
	private String uuid;

	@Unique
	private PetInfo petInfo;

	@Unique
	private SkyblockItemRarity skyblockRarity;

	@Shadow
	public abstract int getDamage();

	@Shadow
	public abstract void setDamage(int damage);

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock()) {
			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(ItemUtils.getItemUuid(this), original);
		}

		return original;
	}

	@ModifyExpressionValue(method = "appendComponentTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/TooltipDisplayComponent;shouldDisplay(Lnet/minecraft/component/ComponentType;)Z"))
	private boolean skyblocker$hideVanillaEnchants(boolean shouldDisplay, @Local TooltipAppender component) {
		return shouldDisplay && !(Utils.isOnSkyblock() && component instanceof ItemEnchantmentsComponent);
	}

	@Inject(method = "appendTooltip",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DefaultedRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;")),
			at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER, ordinal = 0)
	)
	private void skyblocker$skyblockIdTooltip(CallbackInfo ci, @Local(argsOnly = true) Consumer<Text> textConsumer) {
		if (Utils.isOnSkyblock()) {
			String skyblockId = getSkyblockId();

			if (!skyblockId.isEmpty()) {
				textConsumer.accept(Text.literal("skyblock:" + skyblockId).formatted(Formatting.DARK_GRAY));
			}
		}
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
	private boolean skyblocker$shouldProcess() { // Durability bar renders atop of tooltips in ProfileViewer so disable on this screen
		return !(MinecraftClient.getInstance() != null && MinecraftClient.getInstance().currentScreen instanceof ProfileViewerScreen) && Utils.isOnSkyblock() && SkyblockerConfigManager.get().mining.enableDrillFuel && ItemUtils.hasCustomDurability((ItemStack) (Object) this);
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

	@Override
	@NotNull
	public String getSkyblockId() {
		if (skyblockId != null && !skyblockId.isEmpty()) return skyblockId;
		return skyblockId = ItemUtils.getItemId(this);
	}

	@Override
	@NotNull
	public String getSkyblockApiId() {
		if (skyblockApiId != null && !skyblockApiId.isEmpty()) return skyblockApiId;
		return skyblockApiId = ItemUtils.getSkyblockApiId(this);
	}

	@Override
	@NotNull
	public String getNeuName() {
		if (neuName != null && !neuName.isEmpty()) return neuName;
		return neuName = ItemUtils.getNeuId((ItemStack) (Object) this);
	}

	@Override
	@NotNull
	public String getUuid() {
		if (uuid != null && !uuid.isEmpty()) return uuid;
		return uuid = ItemUtils.getItemUuid(this);
	}

	@Override
	@NotNull
	public PetInfo getPetInfo() {
		if (petInfo != null) return petInfo;
		return petInfo = ItemUtils.getPetInfo((ItemStack) (Object) this);
	}

	@Override
	@NotNull
	public SkyblockItemRarity getSkyblockRarity() {
		if (skyblockRarity != null) return skyblockRarity;
		return skyblockRarity = ItemUtils.getItemRarity((ItemStack) (Object) this);
	}
}
