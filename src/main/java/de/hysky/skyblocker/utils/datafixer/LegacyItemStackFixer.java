package de.hysky.skyblocker.utils.datafixer;

import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFixer;

import java.util.List;

import org.slf4j.Logger;

import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.FIRST_VERSION;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.LATEST_VERSION;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.TextTransformer;
import net.azureaaron.legacyitemdfu.TypeReferences;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;

public class LegacyItemStackFixer {
	private static final Logger LOGGER = LogUtils.getLogger();

	//Static import things to avoid class name conflicts
	@SuppressWarnings("deprecation")
	public static ItemStack fixLegacyStack(NbtCompound nbt) {
		RegistryOps<NbtElement> ops = ItemStackComponentizationFixer.getRegistryLookup().getOps(NbtOps.INSTANCE);
		Dynamic<NbtElement> fixed = getFixer().update(TypeReferences.LEGACY_ITEM_STACK, new Dynamic<>(ops, nbt), FIRST_VERSION, LATEST_VERSION);
		ItemStack stack = ItemStack.CODEC.parse(fixed)
				.setPartial(ItemStack.EMPTY)
				.resultOrPartial(LegacyItemStackFixer::log)
				.get();

		//Don't continue fixing up if it failed
		if (stack.isEmpty()) return stack;

		if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
			stack.set(DataComponentTypes.CUSTOM_NAME, TextTransformer.fromLegacy(stack.get(DataComponentTypes.CUSTOM_NAME).getString()));
		}

		if (stack.contains(DataComponentTypes.LORE)) {
			List<Text> fixedLore = stack.get(DataComponentTypes.LORE).lines().stream()
					.map(Text::getString)
					.map(TextTransformer::fromLegacy)
					.map(Text.class::cast)
					.toList();

			stack.set(DataComponentTypes.LORE, new LoreComponent(fixedLore));
		}

		//Remap Custom Data
		if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().getCompound("ExtraAttributes")));
		}

		//Hide Vanilla Attributes
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT.withShowInTooltip(false));

		//Hide Vanilla Enchantments
		stack.set(DataComponentTypes.ENCHANTMENTS, stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).withShowInTooltip(false));

		return stack;
	}

	private static void log(String error) {
		LOGGER.error("[Skyblocker Legacy Item Fixer] Failed to fix up item! Error: {}", error);
	}
}
