package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
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
			case ContainerScreen screen when screen.getTitle().getString().toLowerCase(Locale.ENGLISH).contains("equipment") -> {
				int line = slot / 9;
				if (line > 0 && line < 5 && slot % 9 == 1) {
					boolean empty = stack.getHoverName().getString().trim().toLowerCase(Locale.ENGLISH).startsWith("empty");
					if (Utils.isInTheRift())
						SkyblockInventoryScreen.equipment_rift[line - 1] = empty ? ItemStack.EMPTY : stack;
					else
						SkyblockInventoryScreen.equipment[line - 1] = empty ? ItemStack.EMPTY : stack;
				}
			}
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
