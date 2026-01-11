package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int getX();

	@Accessor("topPos")
	int getY();

	@Accessor("leftPos")
	void setX(int x);

	@Accessor("topPos")
	void setY(int y);

	@Accessor
	int getImageWidth();

	@Accessor
	int getImageHeight();

	@Mutable
	@Accessor("menu")
	void setHandler(AbstractContainerMenu handler);

	@Accessor("hoveredSlot")
	Slot getFocusedSlot();

	@Accessor
	static Identifier getSLOT_HIGHLIGHT_BACK_SPRITE() {
		throw new UnsupportedOperationException();
	}

	@Accessor
	static Identifier getSLOT_HIGHLIGHT_FRONT_SPRITE() {
		throw new UnsupportedOperationException();
	}
}
