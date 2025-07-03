package de.hysky.skyblocker.skyblock.garden.visitor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public record Visitor(Text name, ItemStack head, Object2IntMap<Text> requiredItems) {
	public Visitor(Text name, ItemStack head) {
		this(name, head, new Object2IntOpenHashMap<>());
	}

	public void addRequiredItem(Text item, int amount) {
		requiredItems.put(item, requiredItems.getOrDefault(item, 0) + amount);
	}
}
