package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public final class SkyBlockEquipmentUpdater extends SimpleContainerSolver {
	public SkyBlockEquipmentUpdater() {
		super("^Your Equipment and Stats|(?:\\(\\d/\\d\\) )?Loadouts$");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ItemStack[] equipment = slots.int2ObjectEntrySet().stream()
				.filter(entry -> entry.getIntKey() % 9 == 1 && entry.getIntKey() > 9)
				.map(entry -> {
					boolean isEmpty = entry.getValue().getHoverName().getString().trim().toLowerCase(Locale.ENGLISH).startsWith("empty");
					if (!isEmpty) return entry.getValue();
					return ItemStack.EMPTY;
				}).toArray(ItemStack[]::new);
		if (equipment.length == 0) return List.of();
		ItemStack[] destination = Utils.isInTheRift() ? SkyblockInventoryScreen.equipment_rift : SkyblockInventoryScreen.equipment;
		System.arraycopy(equipment, 0, destination, 0, 4);
		return List.of();
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory;
	}
}
