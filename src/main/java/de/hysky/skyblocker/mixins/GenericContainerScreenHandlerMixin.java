package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
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
        switch (currentScreen) {
            case PartyFinderScreen screen -> screen.markDirty();
            case GenericContainerScreen screen when screen.getTitle().getString().toLowerCase().contains("equipment") -> {
                int line = slot/9;
                if (line > 0 && line < 5 && slot % 9 == 1) {
                    boolean empty = stack.getName().getString().trim().toLowerCase().startsWith("empty");
                    SkyblockInventoryScreen.equipment[line - 1] = empty ? ItemStack.EMPTY : stack;
                }
            }
            case null, default -> {}
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
