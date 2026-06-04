package de.hysky.skyblocker.utils.datafixer;

import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFixer;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getFirstVersion;
import static net.azureaaron.legacyitemdfu.LegacyItemStackFixer.getLatestVersion;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.FlexibleItemStack;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

public class LegacyItemStackFixer {
	private static final Logger LOGGER = LogUtils.getLogger();

	// Static import things to avoid class name conflicts
	@SuppressWarnings("unchecked")
	public static <T extends ItemInstance> T fixLegacyStack(CompoundTag nbt, Codec<T> codec) {
		RegistryOps<Tag> ops = RegistryUtils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE);
		Dynamic<Tag> fixed = getFixer().update(TypeReferences.LEGACY_ITEM_STACK, new Dynamic<>(ops, nbt), getFirstVersion(), getLatestVersion());
		ItemInstance stack = codec.parse(fixed)
				.setPartial((T) ItemStack.EMPTY)
				.resultOrPartial(LegacyItemStackFixer::log)
				.get();

		// Don't continue fixing up if it failed
		if (stack.is(Items.AIR)) return (T) stack;

		Predicate<DataComponentType<?>> contains = type -> switch (stack) {
			case FlexibleItemStack flexible -> flexible.get(type) != null;
			case ItemStack normal -> normal.has(type);
			default -> throw new UnsupportedOperationException();
		};
		BiConsumer<DataComponentType<?>, @Nullable Object> setter = (type, value) -> {
			switch (stack) {
				case FlexibleItemStack flexible -> flexible.set((DataComponentType<Object>) type, value);
				case ItemStack normal -> normal.set((DataComponentType<Object>) type, value);
				default -> throw new UnsupportedOperationException();
			};
		};

		if (contains.test(DataComponents.CUSTOM_NAME)) {
			setter.accept(DataComponents.CUSTOM_NAME, TextTransformer.fromLegacy(stack.get(DataComponents.CUSTOM_NAME).getString()));
		}

		if (contains.test(DataComponents.LORE)) {
			List<Component> fixedLore = stack.get(DataComponents.LORE).lines().stream()
					.map(Component::getString)
					.map(TextTransformer::fromLegacy)
					.map(Component.class::cast)
					.toList();

			setter.accept(DataComponents.LORE, new ItemLore(fixedLore));
		}

		// Remap Custom Data
		if (contains.test(DataComponents.CUSTOM_DATA)) {
			setter.accept(DataComponents.CUSTOM_DATA, CustomData.of(stack.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("ExtraAttributes")));
		}

		// Hide Attributes & Vanilla Enchantments
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
				.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
				.withHidden(DataComponents.ENCHANTMENTS, true);
		setter.accept(DataComponents.TOOLTIP_DISPLAY, display);

		return (T) stack;
	}

	private static void log(String error) {
		LOGGER.error("[Skyblocker Legacy Item Fixer] Failed to fix up item! Error: {}", error);
	}
}
