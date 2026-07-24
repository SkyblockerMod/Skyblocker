package de.hysky.skyblocker.utils.render.text;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.Font;

import java.util.Set;

public class GridComponentManager {
	private static final int PADDING = 4;
	private final Object2ObjectMap<String, IntList> widths = new Object2ObjectOpenHashMap<>();
	private final Set<GridTooltipComponent> handledComponents = new ObjectOpenHashSet<>();

	public GridComponentManager() {
		widths.defaultReturnValue(IntList.of());
	}

	void updateWidths(GridTooltipComponent component, Font font) {
		if (!handledComponents.add(component)) return;
		GridFormattedCharSequence sequence = component.sequence();
		IntList list = widths.computeIfAbsent(sequence.gridContents().group(), _ -> new IntArrayList());
		list.size(sequence.gridContents().components().size());
		for (int i = 0; i < sequence.gridContents().components().size(); i++) {
			list.set(i, Math.max(list.getInt(i), font.width(sequence.gridContents().components().get(i))));
		}
	}

	int getTotalWidth(String group) {
		return widths.get(group).intStream().sum() + (widths.size() - 1) * PADDING;
	}

	int getWidth(String group, int column) {
		IntList list = widths.get(group);
		if (list.size() <= column) return 0;
		return list.getInt(column) + PADDING;
	}
}
