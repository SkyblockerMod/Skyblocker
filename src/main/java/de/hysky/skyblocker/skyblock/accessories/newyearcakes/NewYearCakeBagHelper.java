package de.hysky.skyblocker.skyblock.accessories.newyearcakes;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			for (Slot slot : client.player.containerMenu.slots) {
				NewYearCakesHelper.INSTANCE.addCake(NewYearCakesHelper.getCakeYear(slot.getItem()));
			}
		}
		return List.of();
	}
}
