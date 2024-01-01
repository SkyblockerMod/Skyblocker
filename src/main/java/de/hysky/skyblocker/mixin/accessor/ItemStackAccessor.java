package de.hysky.skyblocker.mixin.accessor;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    @Accessor
    static Style getLORE_STYLE() {
        throw new UnsupportedOperationException();
    }
}
