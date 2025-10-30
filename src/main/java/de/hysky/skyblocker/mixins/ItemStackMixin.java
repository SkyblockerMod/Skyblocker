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
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, SkyblockerStack {
	@Unique
	private float durabilityBarFill = -1;

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

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock()) {
			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(this.getUuid(), original);
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

	@ModifyReturnValue(method = "isItemBarVisible", at = @At("RETURN"))
	private boolean modifyItemBarVisible(boolean original) {
		return original || durabilityBarFill >= 0f;
	}

	@ModifyReturnValue(method = "getItemBarStep", at = @At("RETURN"))
	private int modifyItemBarStep(int original) {
		return durabilityBarFill >= 0 ? (int) (durabilityBarFill * 13) : original;
	}

	@ModifyReturnValue(method = "getItemBarColor", at = @At("RETURN"))
	private int modifyItemBarColor(int original) {
		return durabilityBarFill >= 0 ? OkLabColor.interpolate(Colors.RED, Colors.GREEN, durabilityBarFill) : original;
	}

	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		skyblocker$getAndCacheDurability();
	}

	@Inject(method = "set", at = @At("TAIL"))
	private <T> void skyblocker$resetUuid(ComponentType<T> type, @Nullable T value, CallbackInfoReturnable<T> cir) {
		if (type == DataComponentTypes.CUSTOM_DATA) uuid = null;
	}

	@Unique
	private boolean skyblocker$shouldProcess() { // Durability bar renders atop of tooltips in ProfileViewer so disable on this screen
		return !(MinecraftClient.getInstance() != null && MinecraftClient.getInstance().currentScreen instanceof ProfileViewerScreen) && Utils.isOnSkyblock() && SkyblockerConfigManager.get().mining.enableDrillFuel && ItemUtils.hasCustomDurability((ItemStack) (Object) this);
	}

	@Unique
	private void skyblocker$getAndCacheDurability() {
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
	@NotNull
	public String getSkyblockId() {
		if (skyblockId != null && !skyblockId.isEmpty()) return skyblockId;
		return skyblockId = ItemUtils.getItemId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public String getSkyblockApiId() {
		if (skyblockApiId != null && !skyblockApiId.isEmpty()) return skyblockApiId;
		return skyblockApiId = ItemUtils.getSkyblockApiId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public String getNeuName() {
		if (neuName != null && !neuName.isEmpty()) return neuName;
		return neuName = ItemUtils.getNeuId((ItemStack) (Object) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public String getUuid() {
		if (uuid != null) return uuid;
		return uuid = ItemUtils.getItemUuid(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public PetInfo getPetInfo() {
		if (petInfo != null) return petInfo;
		return petInfo = ItemUtils.getPetInfo((ItemStack) (Object) this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public SkyblockItemRarity getSkyblockRarity() {
		if (skyblockRarity != null) return skyblockRarity;
		return skyblockRarity = ItemUtils.getItemRarity((ItemStack) (Object) this);
	}
}
