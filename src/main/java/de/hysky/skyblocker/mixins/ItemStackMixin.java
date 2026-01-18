package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.utils.ItemAbility;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.ItemEnchantments;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, SkyblockerStack {
	@Unique
	private float durabilityBarFill = -1;

	@Unique
	private @Nullable String skyblockId;

	@Unique
	private @Nullable String skyblockApiId;

	@Unique
	private @Nullable String neuName;

	@Unique
	private @Nullable String uuid;

	@Unique
	private @Nullable List<String> loreString;

	@Unique
	private @Nullable List<ItemAbility> abilities;

	@Unique
	private @Nullable PetInfo petInfo;

	@Unique
	private @Nullable SkyblockItemRarity skyblockRarity;

	@ModifyReturnValue(method = "getHoverName", at = @At("RETURN"))
	private Component skyblocker$customItemNames(Component original) {
		if (Utils.isOnSkyblock()) {
			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(this.getUuid(), original);
		}

		return original;
	}

	@ModifyExpressionValue(method = "addToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/TooltipDisplay;shows(Lnet/minecraft/core/component/DataComponentType;)Z"))
	private boolean skyblocker$hideVanillaEnchants(boolean shouldDisplay, @Local TooltipProvider component) {
		return shouldDisplay && !(Utils.isOnSkyblock() && component instanceof ItemEnchantments);
	}

	@Inject(method = "addDetailsToTooltip",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;")),
			at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER, ordinal = 0)
	)
	private void skyblocker$skyblockIdTooltip(CallbackInfo ci, @Local(argsOnly = true) Consumer<Component> textConsumer) {
		if (Utils.isOnSkyblock()) {
			String skyblockId = getSkyblockId();

			if (!skyblockId.isEmpty()) {
				textConsumer.accept(Component.literal("skyblock:" + skyblockId).withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}

	/**
	 * Updates the durability of this item stack every tick when in the inventory.
	 */
	@Inject(method = "inventoryTick", at = @At("TAIL"))
	private void skyblocker$updateDamage(CallbackInfo ci) {
		skyblocker$getAndCacheDurability();
	}

	@ModifyReturnValue(method = "isBarVisible", at = @At("RETURN"))
	private boolean modifyItemBarVisible(boolean original) {
		return original || durabilityBarFill >= 0f;
	}

	@ModifyReturnValue(method = "getBarWidth", at = @At("RETURN"))
	private int modifyItemBarStep(int original) {
		return durabilityBarFill >= 0 ? (int) (durabilityBarFill * 13) : original;
	}

	@ModifyReturnValue(method = "getBarColor", at = @At("RETURN"))
	private int modifyItemBarColor(int original) {
		return durabilityBarFill >= 0 ? OkLabColor.interpolate(CommonColors.RED, CommonColors.GREEN, durabilityBarFill) : original;
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		skyblocker$getAndCacheDurability();
	}

	@Inject(method = "set*", at = @At("TAIL"))
	private <T> void skyblocker$resetFields(DataComponentType<T> type, @Nullable T value, CallbackInfoReturnable<T> cir) {
		if (type == DataComponents.CUSTOM_DATA) {
			uuid = null;
			skyblockId = null;
			skyblockApiId = null;
			neuName = null;
			petInfo = null;
		}
		if (type == DataComponents.LORE) {
			loreString = null;
			skyblockRarity = null;
			abilities = null;
		}
	}

	@Unique
	private boolean skyblocker$shouldProcess() { // Durability bar renders atop of tooltips in ProfileViewer so disable on this screen
		return !(Minecraft.getInstance() != null && Minecraft.getInstance().screen instanceof ProfileViewerScreen) && Utils.isOnSkyblock() && SkyblockerConfigManager.get().mining.enableDrillFuel && ItemUtils.hasCustomDurability((ItemStack) (Object) this);
	}

	@Unique
	private void skyblocker$getAndCacheDurability() {
		if (!skyblocker$shouldProcess()) {
			durabilityBarFill = -1;
			return;
		}
		// Calculate the durability
		IntIntPair durability = ItemUtils.getDurability((ItemStack) (Object) this);
		// Return if calculating the durability failed
		if (durability == null) {
			durabilityBarFill = -1;
			return;
		}
		// Saves the calculated durability
		durabilityBarFill = (float) durability.firstInt() / durability.secondInt();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getSkyblockId() {
		if (skyblockId != null && !skyblockId.isEmpty()) return skyblockId;
		return skyblockId = ItemUtils.getItemId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getSkyblockApiId() {
		if (skyblockApiId != null && !skyblockApiId.isEmpty()) return skyblockApiId;
		return skyblockApiId = ItemUtils.getSkyblockApiId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getNeuName() {
		if (neuName != null && !neuName.isEmpty()) return neuName;
		return neuName = ItemUtils.getNeuId((ItemStack) (Object) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getUuid() {
		if (uuid != null) return uuid;
		return uuid = ItemUtils.getItemUuid(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> skyblocker$getLoreStrings() {
		if (loreString != null) return loreString;
		return loreString = ItemUtils.getLore((ItemStack) (Object) this).stream().map(Component::getString).toList();
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemAbility> skyblocker$getAbilities() {
		if (abilities != null) return abilities;
		return abilities = ItemAbility.getAbilities((ItemStack) (Object) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public PetInfo getPetInfo() {
		if (petInfo != null) return petInfo;
		return petInfo = ItemUtils.getPetInfo((ItemStack) (Object) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public SkyblockItemRarity getSkyblockRarity() {
		if (skyblockRarity != null) return skyblockRarity;
		return skyblockRarity = ItemUtils.getItemRarity((ItemStack) (Object) this);
	}
}
