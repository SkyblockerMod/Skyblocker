package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();

    @Accessor
    int getBackgroundWidth();

    @Accessor
    int getBackgroundHeight();

    @Mutable
    @Accessor("handler")
    void setHandler(ScreenHandler handler);

    @Accessor("focusedSlot")
    Slot getFocusedSlot();

    @Invoker
    List<Text> invokeGetTooltipFromItem(ItemStack stack);
}
