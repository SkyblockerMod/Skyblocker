package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PotionLevelAdder extends SlotTextAdder {
    @Override
    public @NotNull List<PositionedText> getText(Slot slot) {
        final ItemStack stack = slot.getStack();
        NbtCompound customData = ItemUtils.getCustomData(stack);
        if (customData.contains("potion_level", NbtElement.INT_TYPE)) {
            int level = customData.getInt("potion_level");
            return List.of(PositionedText.BOTTOM_RIGHT(Text.literal(String.valueOf(level)).formatted(Formatting.AQUA)));
        }
        return List.of();
    }
}
