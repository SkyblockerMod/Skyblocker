package de.hysky.skyblocker.skyblock.accessories.newyearcakes;

import de.hysky.skyblocker.annotations.RegisterContainerSolver;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;

@RegisterContainerSolver
public class NewYearCakeBagHelper extends SimpleContainerSolver {
    public NewYearCakeBagHelper() {
        super("New Year Cake Bag");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().helpers.enableNewYearCakesHelper;
    }

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            for (Slot slot : client.player.currentScreenHandler.slots) {
                NewYearCakesHelper.INSTANCE.addCake(NewYearCakesHelper.getCakeYear(slot.getStack()));
            }
        }
        return List.of();
    }
}
