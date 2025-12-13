package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	@Shadow
	public @Nullable LocalPlayer player;

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	public void skyblocker$handleInputEvents(CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			HotbarSlotLock.handleInputEvents(player);
			ItemProtection.handleHotbarKeyPressed(player);
		}
	}

	@WrapOperation(method = "handleKeybinds", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;"))
	private InventoryScreen skyblocker$skyblockInventoryScreen(Player player, Operation<InventoryScreen> original) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? new SkyblockInventoryScreen(player) : original.call(player);
	}
}
