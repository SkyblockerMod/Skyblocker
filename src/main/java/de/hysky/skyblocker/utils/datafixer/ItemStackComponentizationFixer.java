package de.hysky.skyblocker.utils.datafixer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.arguments.item.ItemParser.ItemResult;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

/**
 * Contains a data fixer to convert legacy item NBT to the new components system, among other fixers related to the item components system.
 *
 * @see net.minecraft.util.datafix.fixes.ItemStackComponentizationFix
 */
public class ItemStackComponentizationFixer {
	private static final int ITEM_NBT_DATA_VERSION = 3817;
	private static final int ITEM_COMPONENTS_DATA_VERSION = 4325;

	public static ItemStack fixUpItem(CompoundTag nbt) {
		Dynamic<Tag> dynamic = DataFixers.getDataFixer().update(References.ITEM_STACK, new Dynamic<>(Utils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE), nbt), ITEM_NBT_DATA_VERSION, ITEM_COMPONENTS_DATA_VERSION);

		return ItemStack.CODEC.parse(dynamic).getOrThrow();
	}

	/**
	 * Modified version of {@link net.minecraft.commands.arguments.item.ItemInput#serialize(net.minecraft.core.HolderLookup.Provider)} to only care about changed components.
	 *
	 * @return The {@link ItemStack}'s components as a string which is in the format that the {@code /give} command accepts.
	 */
	public static String componentsAsString(ItemStack stack) {
		RegistryOps<Tag> nbtRegistryOps = Utils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE);

		return Arrays.toString(stack.getComponentsPatch().entrySet().stream().map(entry -> {
			DataComponentType<?> componentType = entry.getKey();
			Identifier componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
			if (componentId == null) return null;

			Optional<?> component = entry.getValue();
			if (component.isEmpty()) return "!" + componentId;

			Optional<Tag> encodedComponent = TypedDataComponent.createUnchecked(componentType, component.get()).encodeValue(nbtRegistryOps).result();

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
		ItemParser reader = new ItemParser(Utils.getRegistryWrapperLookup());

		try {
			ItemResult result = reader.parse(new StringReader(itemId + componentsString));
			ItemStack stack = new ItemStack(result.item(), count);

			//Vanilla skips validation with /give so we will too
			stack.applyComponents(result.components());

			return stack;
		} catch (Exception ignored) {}

		return ItemStack.EMPTY;
	}

}
