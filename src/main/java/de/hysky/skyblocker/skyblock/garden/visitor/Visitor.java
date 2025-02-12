package de.hysky.skyblocker.skyblock.garden.visitor;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public record Visitor(Text name, ItemStack head, Map<Text, Integer> requiredItems) {
	public Visitor(Text name, ItemStack head) {
		this(name, head, new HashMap<>());
	}

	public void addRequiredItem(Text item, int amount) {
		requiredItems.put(item, requiredItems.getOrDefault(item, 0) + amount);
	}
}
