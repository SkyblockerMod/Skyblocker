package de.hysky.skyblocker.utils.datafixer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.command.argument.ItemStringReader.ItemResult;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

/**
 * Contains a data fixer to convert legacy item NBT to the new components system, among other fixers related to the item components system.
 *
 * @see net.minecraft.datafixer.fix.ItemStackComponentizationFix
 */
public class ItemStackComponentizationFixer {
	private static final int ITEM_NBT_DATA_VERSION = 3817;
	private static final int ITEM_COMPONENTS_DATA_VERSION = 3825;
	private static final WrapperLookup LOOKUP = BuiltinRegistries.createWrapperLookup();

	public static ItemStack fixUpItem(NbtCompound nbt) {
		Dynamic<NbtElement> dynamic = Schemas.getFixer().update(TypeReferences.ITEM_STACK, new Dynamic<>(getRegistryLookup().getOps(NbtOps.INSTANCE), nbt), ITEM_NBT_DATA_VERSION, ITEM_COMPONENTS_DATA_VERSION);

		return ItemStack.CODEC.parse(dynamic).getOrThrow();
	}

	/**
	 * Modified version of {@link net.minecraft.command.argument.ItemStackArgument#asString(net.minecraft.registry.RegistryWrapper.WrapperLookup)} to only care about changed components.
	 *
	 * @return The {@link ItemStack}'s components as a string which is in the format that the {@code /give} command accepts.
	 */
	public static String componentsAsString(ItemStack stack) {
		RegistryOps<NbtElement> nbtRegistryOps = getRegistryLookup().getOps(NbtOps.INSTANCE);

		return Arrays.toString(stack.getComponentChanges().entrySet().stream().map(entry -> {
			ComponentType<?> componentType = entry.getKey();
			Identifier componentId = Registries.DATA_COMPONENT_TYPE.getId(componentType);
			if (componentId == null) return null;

			Optional<?> component = entry.getValue();
			if (component.isEmpty()) return "!" + componentId;

			Optional<NbtElement> encodedComponent = Component.of(componentType, component.get()).encode(nbtRegistryOps).result();

			if (encodedComponent.isEmpty()) return null;
			return componentId + "=" + encodedComponent.orElseThrow();
		}).filter(Objects::nonNull).toArray());
	}

	/**
	 * Constructs an {@link ItemStack} from an {@code itemId}, with item components in string format as returned by {@link #componentsAsString(ItemStack)}, and with a specified stack count.
	 *
	 * @return an {@link ItemStack} or {@link ItemStack#EMPTY} if there was an exception thrown.
	 */
	public static ItemStack fromComponentsString(String itemId, int count, String componentsString) {
		ItemStringReader reader = new ItemStringReader(getRegistryLookup());

		try {
			ItemResult result = reader.consume(new StringReader(itemId + componentsString));
			ItemStack stack = new ItemStack(result.item(), count);

			//Vanilla skips validation with /give so we will too
			stack.applyUnvalidatedChanges(result.components());

			return stack;
		} catch (Exception ignored) {}

		return ItemStack.EMPTY;
	}

	/**
	 * Tries to get the dynamic registry manager instance currently in use or else returns {@link #LOOKUP}
	 */
	public static WrapperLookup getRegistryLookup() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client != null && client.getNetworkHandler() != null && client.getNetworkHandler().getRegistryManager() != null ? client.getNetworkHandler().getRegistryManager() : LOOKUP;
	}
}
