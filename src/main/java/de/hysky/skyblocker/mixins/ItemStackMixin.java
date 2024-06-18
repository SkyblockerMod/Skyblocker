package de.hysky.skyblocker.mixins;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder, SkyblockerStack {

	@Shadow
	public abstract int getDamage();

	@Shadow
	public abstract void setDamage(int damage);

	@Unique
	private int maxDamage;

	@Unique
	private String skyblockId;

	@Unique
	private String skyblockApiId;

	@Unique
	private String neuName;

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock()) {
			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(ItemUtils.getItemUuid(this), original);
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
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().mining.enableDrillFuel && ItemUtils.hasCustomDurability((ItemStack) (Object) this);
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
	@Nullable
	public String getSkyblockId() {
		if (skyblockId != null && !skyblockId.isEmpty()) return skyblockId;
		return skyblockId = skyblocker$getSkyblockId(true);
	}

	@Override
	@Nullable
	public String getSkyblockApiId() {
		if (skyblockApiId != null && !skyblockApiId.isEmpty()) return skyblockApiId;
		return skyblockApiId = skyblocker$getSkyblockId(false);
	}

	@Override
	@Nullable
	public String getNeuName() {
		if (neuName != null && !neuName.isEmpty()) return neuName;
		String apiId = getSkyblockApiId();
		String id = getSkyblockId();
		if (apiId == null || id == null) return null;

		if (apiId.startsWith("ISSHINY_")) apiId = id;

		return neuName = ItemTooltip.getNeuName(id, apiId);
	}

	@Unique
	private String skyblocker$getSkyblockId(boolean internalIDOnly) {
		NbtCompound customData = ItemUtils.getCustomData((ItemStack) (Object) this);

		if (customData == null || !customData.contains(ItemUtils.ID, NbtElement.STRING_TYPE)) {
			return null;
		}
		String customDataString = customData.getString(ItemUtils.ID);

		if (internalIDOnly) {
			return customDataString;
		}

		// Transformation to API format.
		if (customData.contains("is_shiny")) {
			return "ISSHINY_" + customDataString;
		}

		switch (customDataString) {
			case "ENCHANTED_BOOK" -> {
				if (customData.contains("enchantments")) {
					NbtCompound enchants = customData.getCompound("enchantments");
					Optional<String> firstEnchant = enchants.getKeys().stream().findFirst();
					String enchant = firstEnchant.orElse("");
					return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getInt(enchant);
				}
			}
			case "PET" -> {
				if (customData.contains("petInfo")) {
					JsonObject petInfo = SkyblockerMod.GSON.fromJson(customData.getString("petInfo"), JsonObject.class);
					return "LVL_1_" + petInfo.get("tier").getAsString() + "_" + petInfo.get("type").getAsString();
				}
			}
			case "POTION" -> {
				String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
				String extended = customData.contains("extended") ? "_EXTENDED" : "";
				String splash = customData.contains("splash") ? "_SPLASH" : "";
				if (customData.contains("potion") && customData.contains("potion_level")) {
					return (customData.getString("potion") + "_" + customDataString + "_" + customData.getInt("potion_level")
							+ enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
				}
			}
			case "RUNE" -> {
				if (customData.contains("runes")) {
					NbtCompound runes = customData.getCompound("runes");
					Optional<String> firstRunes = runes.getKeys().stream().findFirst();
					String rune = firstRunes.orElse("");
					return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getInt(rune);
				}
			}
			case "ATTRIBUTE_SHARD" -> {
				if (customData.contains("attributes")) {
					NbtCompound shards = customData.getCompound("attributes");
					Optional<String> firstShards = shards.getKeys().stream().findFirst();
					String shard = firstShards.orElse("");
					return customDataString + "-" + shard.toUpperCase(Locale.ENGLISH) + "_" + shards.getInt(shard);
				}
			}
		}
		return customDataString;
	}
}
