package de.hysky.skyblocker.utils.datafixer;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.TextTransformer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;

/**
 * Contains a to convert legacy {@code ItemStack}s in the 1.8 format to modern ones, taking into account
 * how Hypixel displays these items to clients.
 */
public class LegacyItemStackFixer {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final ThreadLocal<Boolean> ENABLE_DFU_FIXES = ThreadLocal.withInitial(() -> false);
	private static final int FIRST_DATA_VERSION = 100; //15w32a (1.9 snapshot)
	private static final int CURRENT_DATA_VERSION = SharedConstants.getGameVersion().getSaveVersion().getId();

	//Static so that this can be changed via reflection
	private static boolean shouldLog = FabricLoader.getInstance().isDevelopmentEnvironment();

	@SuppressWarnings("deprecation")
	public static ItemStack fixLegacyStack(NbtCompound nbt) {
		ENABLE_DFU_FIXES.set(true);

		RegistryOps<NbtElement> ops = ItemStackComponentizationFixer.getRegistryLookup().getOps(NbtOps.INSTANCE);
		Dynamic<NbtElement> fixed = Schemas.getFixer().update(TypeReferences.ITEM_STACK, new Dynamic<>(ops, nbt), FIRST_DATA_VERSION, CURRENT_DATA_VERSION);
		ItemStack stack = ItemStack.CODEC.parse(fixed)
				.setPartial(ItemStack.EMPTY)
				.resultOrPartial(LegacyItemStackFixer::tryLog)
				.get();

		ENABLE_DFU_FIXES.remove(); //Free memory

		//Convert Custom Name & Lore to components
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
		stack.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT.withShowInTooltip(false));

		return stack;
	}

	private static void tryLog(String error) {
		if (shouldLog) LOGGER.error("[Skyblocker Legacy Item Fixer] Failed to fix up item! Error: {}", error);
	}
}
