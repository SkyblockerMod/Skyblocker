package de.hysky.skyblocker.skyblock.garden.visitor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record Visitor(Component name, ItemStack head, Object2IntMap<Component> requiredItems) {
	public Visitor(Component name, ItemStack head) {
		this(name, head, new Object2IntOpenHashMap<>());
	}

	public void addRequiredItem(Component item, int amount) {
		requiredItems.put(item, requiredItems.getOrDefault(item, 0) + amount);
	}
}
