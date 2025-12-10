package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.LivingEntity;

@Mixin(InventoryScreen.class)
public interface InventoryScreenInvoker {

	// TODO rename this after mojmap because this method nothing to do with drawing entities but filling the render state
	// for the GUI
	@Invoker
	static EntityRenderState invokeDrawEntity(LivingEntity entity) {
		throw new UnsupportedOperationException();
	}
}
