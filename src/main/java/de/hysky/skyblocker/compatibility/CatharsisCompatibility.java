package de.hysky.skyblocker.compatibility;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class CatharsisCompatibility {
	private static BiConsumer<ItemStack, Identifier> idConsumer = (_, _) -> {};
	private static BiConsumer<ItemStack, Boolean> disabledConsumer = (_, _) -> {};
	private static Predicate<String> hiddenModElementsProvider = (_) -> false;

	public static void id(BiConsumer<ItemStack, Identifier> consumer) {
		idConsumer = consumer;
	}

	public static void disabled(BiConsumer<ItemStack, Boolean> consumer) {
		disabledConsumer = consumer;
	}

	public static void hiddenGuiElements(Predicate<String> provider) {
		hiddenModElementsProvider = provider;
	}

	public static boolean isGuiElementHidden(String element) {
		return hiddenModElementsProvider.test(element);
	}

	public static ItemStack disableCatharsisModifications(ItemStack stack) {
		disabledConsumer.accept(stack, true);
		return stack;
	}

	public static ItemStack withCatharsisId(ItemStack stack, Identifier identifier) {
		idConsumer.accept(stack, identifier);
		return stack;
	}

	public static ItemStack withCatharsisId(Item item, Identifier identifier) {
		ItemStack stack = item.getDefaultInstance();
		idConsumer.accept(stack, identifier);
		return stack;
	}
}
