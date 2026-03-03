package de.hysky.skyblocker.utils.datafixer;

import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFixer;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFirstVersion;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getLatestVersion;

import java.util.List;

import de.hysky.skyblocker.utils.Utils;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.TextTransformer;
import net.azureaaron.legacyitemdfu.TypeReferences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

public class LegacyItemStackFixer {
	private static final Logger LOGGER = LogUtils.getLogger();

	//Static import things to avoid class name conflicts
	public static ItemStack fixLegacyStack(CompoundTag nbt) {
		RegistryOps<Tag> ops = Utils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE);
		Dynamic<Tag> fixed = getFixer().update(TypeReferences.LEGACY_ITEM_STACK, new Dynamic<>(ops, nbt), getFirstVersion(), getLatestVersion());
		ItemStack stack = ItemStack.CODEC.parse(fixed)
				.setPartial(ItemStack.EMPTY)
				.resultOrPartial(LegacyItemStackFixer::log)
				.get();

		//Don't continue fixing up if it failed
		if (stack.isEmpty()) return stack;

		if (stack.has(DataComponents.CUSTOM_NAME)) {
			stack.set(DataComponents.CUSTOM_NAME, TextTransformer.fromLegacy(stack.get(DataComponents.CUSTOM_NAME).getString()));
		}

		if (stack.has(DataComponents.LORE)) {
			List<Component> fixedLore = stack.get(DataComponents.LORE).lines().stream()
					.map(Component::getString)
					.map(TextTransformer::fromLegacy)
					.map(Component.class::cast)
					.toList();

			stack.set(DataComponents.LORE, new ItemLore(fixedLore));
		}

		//Remap Custom Data
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stack.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("ExtraAttributes")));
		}

		//Hide Attributes & Vanilla Enchantments
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
				.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
				.withHidden(DataComponents.ENCHANTMENTS, true);
		stack.set(DataComponents.TOOLTIP_DISPLAY, display);

		return stack;
	}

	private static void log(String error) {
		LOGGER.error("[Skyblocker Legacy Item Fixer] Failed to fix up item! Error: {}", error);
	}
}
