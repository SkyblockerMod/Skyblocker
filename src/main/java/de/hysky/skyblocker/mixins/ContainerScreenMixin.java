package de.hysky.skyblocker.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {

	public ContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@ModifyVariable(
			method = "renderBg",
			at = @At("STORE"),
			index = 5)
	public int x(int ignored) {
		return leftPos;
	}

	@ModifyVariable(
			method = "renderBg",
			at = @At("STORE"),
			index = 6)
	public int y(int ignored) {
		return topPos;
	}
}
