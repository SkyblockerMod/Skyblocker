package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(EnderMan.class)
public interface EndermanEntityAccessor {
	@Accessor
	static EntityDataAccessor<Optional<BlockState>> getDATA_CARRY_STATE() {
		throw new UnsupportedOperationException();
	}
}
