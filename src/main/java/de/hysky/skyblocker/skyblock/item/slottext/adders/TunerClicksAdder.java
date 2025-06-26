package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.galatea.TunerSolver;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TunerClicksAdder extends SimpleSlotTextAdder {
    public TunerClicksAdder() {
        super("^Tune Frequency$");
    }

    @Override
    public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
        if (slotId == 46 && TunerSolver.isColorSolved()) {
            return SlotText.bottomRightList(Text.literal(String.valueOf(TunerSolver.getColorClicks())).withColor(SlotText.LIGHT_GREEN));
        }
        if (slotId == 48 && TunerSolver.isSpeedSolved()) {
            return SlotText.bottomRightList(Text.literal(String.valueOf(TunerSolver.getSpeedClicks())).withColor(SlotText.LIGHT_GREEN));
        }
        if (slotId == 50 && TunerSolver.isPitchSolved()) {
            return SlotText.bottomRightList(Text.literal(String.valueOf(TunerSolver.getPitchClicks())).withColor(SlotText.LIGHT_GREEN));
        }
        return List.of();
    }
}
