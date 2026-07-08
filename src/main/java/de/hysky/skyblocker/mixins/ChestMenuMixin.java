package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

@Mixin(ChestMenu.class)
public abstract class ChestMenuMixin extends AbstractContainerMenu {
	protected ChestMenuMixin(@Nullable MenuType<?> type, int syncId) {
		super(type, syncId);
	}

	@Override
	public void setItem(int slot, int revision, ItemStack stack) {
		super.setItem(slot, revision, stack);
		ContainerSolverManager.markHighlightsDirty();

		Screen currentScreen = Minecraft.getInstance().screen;
		switch (currentScreen) {
			case PartyFinderScreen screen -> screen.markDirty();
			case null, default -> {}
		}
		broadcastChanges();
	}

	@Override
	public void initializeContents(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
		super.initializeContents(revision, stacks, cursorStack);
		ContainerSolverManager.markHighlightsDirty();
		broadcastChanges();
		if (Minecraft.getInstance().screen instanceof PartyFinderScreen screen) {
			screen.markDirty();
		}
	}
}
