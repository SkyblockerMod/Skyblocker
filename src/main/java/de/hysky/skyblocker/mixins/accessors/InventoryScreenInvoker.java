package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InventoryScreen.class)
public interface InventoryScreenInvoker {

	// TODO rename this after mojmap because this method nothing to do with drawing entities but filling the render state
	// for the GUI
	@Invoker
	static EntityRenderState invokeExtractRenderState(LivingEntity entity) {
		throw new UnsupportedOperationException();
	}
}
