package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.intellij.lang.annotations.Language;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SkyBlockEquipmentUpdater extends SimpleContainerSolver {
	public SkyBlockEquipmentUpdater() {
		super("^Your Equipment and Stats|(?:\\(\\d/\\d\\) )?Loadouts$");
	}

	public SkyBlockEquipmentUpdater(@Language("RegExp") String titlePattern) {
		super(titlePattern);
	}

	public ItemStack[] getEquipmentInColumn(Int2ObjectMap<ItemStack> slots, int column) {
		return slots.int2ObjectEntrySet().stream()
				.filter(entry -> entry.getIntKey() % 9 == column)
				.filter(entry -> !entry.getValue().is(Items.BLACK_STAINED_GLASS_PANE))
				.map(entry -> {
					String name = entry.getValue().getHoverName().getString().trim().toLowerCase(Locale.ENGLISH);
					boolean isEmpty = name.startsWith("empty") || name.startsWith("slot ");
					if (!isEmpty) return entry.getValue();
					return ItemStack.EMPTY;
				}).toArray(ItemStack[]::new);
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ItemStack[] equipment = getEquipmentInColumn(slots, 1);
		if (equipment.length < 4) return List.of();
		ItemStack[] destination = Utils.isInTheRift() ? SkyblockInventoryScreen.equipment_rift : SkyblockInventoryScreen.equipment;
		System.arraycopy(equipment, 0, destination, 0, 4);
		return List.of();
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory;
	}

	public static final class EquipmentWardrobe extends SkyBlockEquipmentUpdater {
		public EquipmentWardrobe() {
			super("^(?:\\(\\d/\\d\\) )?Equipment Sets$");
		}

		@Override
		public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
			Optional<Integer> selectedSet = slots.int2ObjectEntrySet().stream()
					.filter(entry -> entry.getIntKey() > 35 && entry.getIntKey() < 45)
					.filter(entry -> entry.getValue().is(Items.LIME_DYE))
					.map(entry -> entry.getIntKey() % 9)
					.findFirst();
			if (selectedSet.isEmpty()) return List.of();
			ItemStack[] equipment = getEquipmentInColumn(slots, selectedSet.get());
			if (equipment.length < 4) return List.of();
			System.arraycopy(equipment, 0, SkyblockInventoryScreen.equipment, 0, 4);
			return List.of();
		}

		@Override
		public boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
			if (stack.is(Items.LIME_DYE) && slot > 35 && slot < 45) {
				Arrays.fill(SkyblockInventoryScreen.equipment, ItemStack.EMPTY);
			}
			return false;
		}
	}
}
