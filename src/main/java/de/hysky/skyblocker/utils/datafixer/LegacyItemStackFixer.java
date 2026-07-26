package de.hysky.skyblocker.utils.datafixer;

import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFixer;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFirstVersion;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getLatestVersion;

import java.util.List;

import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.RegistryUtils;
import de.hysky.skyblocker.utils.TextTransformer;
import net.azureaaron.legacyitemdfu.TypeReferences;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

public class LegacyItemStackFixer {
	private static final Logger LOGGER = LogUtils.getLogger();

	// Static import things to avoid class name conflicts
	public static <T extends ItemInstance> T fixLegacyStack(CompoundTag nbt, Codec<T> codec, T defaultValue, @SuppressWarnings("rawtypes") TriConsumer<T, DataComponentType, Object> setComponent) {
		RegistryOps<Tag> ops = RegistryUtils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE);
		Dynamic<Tag> fixed = getFixer().update(TypeReferences.LEGACY_ITEM_STACK, new Dynamic<>(ops, nbt), getFirstVersion(), getLatestVersion());
		T stack = codec.parse(fixed)
				.resultOrPartial(LegacyItemStackFixer::log)
				.orElse(defaultValue);

		// Don't continue fixing up if it failed
		if (stack.is(Items.AIR)) return stack;

		Component name = stack.get(DataComponents.CUSTOM_NAME);
		if (name != null) {
			setComponent.accept(stack, DataComponents.CUSTOM_NAME, TextTransformer.fromLegacy(name.getString()));
		}

		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore != null) {
			List<Component> fixedLore = lore.lines().stream()
					.map(Component::getString)
					.map(TextTransformer::fromLegacy)
					.map(Component.class::cast)
					.toList();

			setComponent.accept(stack, DataComponents.LORE, new ItemLore(fixedLore));
		}

		// Remap Custom Data
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData != null) {
			setComponent.accept(stack, DataComponents.CUSTOM_DATA, CustomData.of(customData.copyTag().getCompoundOrEmpty("ExtraAttributes")));
		}

		// Hide Attributes & Vanilla Enchantments
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
				.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
				.withHidden(DataComponents.ENCHANTMENTS, true);
		setComponent.accept(stack, DataComponents.TOOLTIP_DISPLAY, display);

		return stack;
	}

	private static void log(String error) {
		LOGGER.error("[Skyblocker Legacy Item Fixer] Failed to fix up item! Error: {}", error);
	}
}
