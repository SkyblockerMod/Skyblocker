package de.hysky.skyblocker.skyblock.accessories;

import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccessoriesContainerSolver extends SimpleContainerSolver {
	private static final int COLOR = ColorHelper.withAlpha(0.7f, Colors.GREEN);
	public static final AccessoriesContainerSolver INSTANCE = new AccessoriesContainerSolver();

	@Nullable String highlightedAccessory;

	protected AccessoriesContainerSolver() {
		super(AccessoriesHelper.ACCESSORY_BAG_TITLE);
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		if (highlightedAccessory == null) return List.of();
		return slots.int2ObjectEntrySet().stream()
				.filter(entry -> entry.getValue().getSkyblockId().equals(highlightedAccessory))
				.map(entry -> new ColorHighlight(entry.getIntKey(), COLOR))
				.toList();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
