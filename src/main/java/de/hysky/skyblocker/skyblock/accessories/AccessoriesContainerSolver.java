package de.hysky.skyblocker.skyblock.accessories;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AccessoriesContainerSolver extends SimpleContainerSolver {
	private static final int UNIQUE_COLOR = ARGB.color(0.7f, CommonColors.GREEN);
	private static final int DUPLICATE_COLOR = ARGB.color(0.7f, CommonColors.RED);
	public static final AccessoriesContainerSolver INSTANCE = new AccessoriesContainerSolver();

	@Nullable String highlightedAccessory;

	protected AccessoriesContainerSolver() {
		super(AccessoriesHelper.ACCESSORY_BAG_TITLE);
	}

	@Override
public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
	return slots.int2ObjectEntrySet().stream()
			.map(entry -> {
				int slot = entry.getIntKey();
				ItemStack stack = entry.getValue();

				String id = stack.getSkyblockId();

				if (id.isEmpty()) return null;

				if (SkyblockerConfigManager.get().helpers.accessories.enableAccessoriesHelperWidget
						&& SkyblockerConfigManager.get().helpers.accessories.showDuplicateAccessories
						&& AccessoriesHelper.duplicateSlots.contains(slot)) {
					return new ColorHighlight(slot, DUPLICATE_COLOR);
				}

				return new ColorHighlight(slot, UNIQUE_COLOR);
			})
			.filter(java.util.Objects::nonNull)
			.toList();
}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.accessories.showDuplicateAccessories;
}
}
