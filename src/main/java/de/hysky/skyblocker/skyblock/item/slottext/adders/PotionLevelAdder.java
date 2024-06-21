package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PotionLevelAdder extends SlotTextAdder {
    @Override
    public @NotNull List<SlotText> getText(@NotNull ItemStack stack, int slotId) {
        NbtCompound customData = ItemUtils.getCustomData(stack);
        String title = stack.getName().getString();
        if (customData.contains("potion_level", NbtElement.INT_TYPE) && !title.contains("Healer Class") && !title.contains("Class Passives")) {
            if (title.contains("Healer Level ")){
                String level = title.replaceAll("\\D", "");
                return List.of(SlotText.bottomRight(Text.literal(level).withColor(0xFFFFFF)));
            } else {
                int level = customData.getInt("potion_level");
                return List.of(SlotText.bottomRight(Text.literal(String.valueOf(level)).withColor(0xFFDDC1)));
            }
        }
        return List.of();
    }
}
