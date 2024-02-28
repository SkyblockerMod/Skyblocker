package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.utils.render.gui.HandlerBackedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof PartyFinderScreen screen) {
            screen.markDirty();
        } else if (currentScreen instanceof HandlerBackedScreen screen) {
            screen.markDirty();
        }
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        super.updateSlotStacks(revision, stacks, cursorStack);
        SkyblockerMod.getInstance().containerSolverManager.markDirty();
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof PartyFinderScreen screen) {
            screen.markDirty();
        } else if (currentScreen instanceof HandlerBackedScreen screen) {
            screen.markDirty();
        }
    }
}
