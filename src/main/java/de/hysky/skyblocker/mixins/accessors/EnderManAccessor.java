package de.hysky.skyblocker.mixins.accessors;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(EnderMan.class)
public interface EnderManAccessor {
	@Accessor
	static EntityDataAccessor<Optional<BlockState>> getDATA_CARRY_STATE() {
		throw new UnsupportedOperationException();
	}
}
