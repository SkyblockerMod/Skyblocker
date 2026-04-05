package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Bat.class)
public abstract class BatMixin extends AmbientCreature {
	protected BatMixin(EntityType<? extends AmbientCreature> entityType, Level world) {
		super(entityType, world);
	}

	@Override
	public void onClientRemoval() {
		super.onClientRemoval();
		if (this.getHealth() <= 0) {
			DungeonManager.onBatRemoved(this);
		}
	}
}
