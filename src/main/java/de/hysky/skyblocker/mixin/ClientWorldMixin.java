package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @ModifyVariable(method = "removeEntity", at = @At(value = "LOAD", ordinal = 1))
    private Entity skyblocker$onEntityRemoved(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            DungeonManager.onItemPickup(itemEntity);
        }
        return entity;
    }
}
