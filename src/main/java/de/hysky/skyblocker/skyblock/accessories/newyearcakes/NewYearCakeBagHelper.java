package de.hysky.skyblocker.skyblock.accessories.newyearcakes;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.Map;

public class NewYearCakeBagHelper extends ContainerSolver {
    public NewYearCakeBagHelper() {
        super("New Year Cake Bag");
    }

    @Override
    protected boolean isEnabled() {
        return SkyblockerConfigManager.get().general.enableNewYearCakesHelper;
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            for (Slot slot : client.player.currentScreenHandler.slots) {
                NewYearCakesHelper.INSTANCE.addCake(NewYearCakesHelper.getCakeYear(slot.getStack()));
            }
        }
        return List.of();
    }
}
