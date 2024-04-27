package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(GenericContainerScreenHandler.class)
public abstract class GenericContainerScreenHandlerMixin extends ScreenHandler {
    protected GenericContainerScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        super.setStackInSlot(slot, revision, stack);
        SkyblockerMod.getInstance().containerSolverManager.markDirty();
        if (MinecraftClient.getInstance().currentScreen instanceof PartyFinderScreen screen) {
            screen.markDirty();
        }
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        super.updateSlotStacks(revision, stacks, cursorStack);
        SkyblockerMod.getInstance().containerSolverManager.markDirty();
        if (MinecraftClient.getInstance().currentScreen instanceof PartyFinderScreen screen) {
            screen.markDirty();
        }
    }
}
