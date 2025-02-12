package de.hysky.skyblocker.skyblock.garden.visitor;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

public class Visitor {
	private final Text name;
	private final ItemStack head;
	private final Map<Text, Integer> requiredItems;

	public Visitor(Text name, ItemStack head) {
		this.name = name;
		this.head = head;
		this.requiredItems = new HashMap<>();
	}

	public Text name() {
		return name;
	}

	public ItemStack head() {
		return head;
	}

	public Map<Text, Integer> requiredItems() {
		return requiredItems;
	}

	public void addRequiredItem(Text item, int amount) {
		requiredItems.put(item, requiredItems.getOrDefault(item, 0) + amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Visitor visitor = (Visitor) o;
		return Objects.equals(name, visitor.name) &&
				ItemStack.areEqual(head, visitor.head) &&
				Objects.equals(requiredItems, visitor.requiredItems);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, head, requiredItems);
	}
}
