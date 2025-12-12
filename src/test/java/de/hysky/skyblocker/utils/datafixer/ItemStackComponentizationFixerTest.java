package de.hysky.skyblocker.utils.datafixer;

import de.hysky.skyblocker.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ItemStackComponentizationFixerTest {
	private final CompoundTag NBT = convertToNbt("{id:\"minecraft:diamond_sword\",Count:1,tag:{ExtraAttributes:{id:\"TEST\"}}}");
	private final Gson GSON = new Gson();
	private final ItemStack TEST_STACK = Util.make(new ItemStack(Items.DIAMOND_SWORD, 1), item -> {
		ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

		builder.upgrade(Utils.getRegistryWrapperLookup().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 1);
		item.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
	});

	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testNbtConversion() {
		Assertions.assertNotEquals(NBT, new CompoundTag());
	}

	@Test
	void testDataFixer() {
		ItemStack fixedStack = ItemStackComponentizationFixer.fixUpItem(NBT);
		JsonElement stackJson = ItemStack.CODEC.encodeStart(Utils.getRegistryWrapperLookup().createSerializationContext(JsonOps.INSTANCE), fixedStack).getOrThrow();

		Assertions.assertEquals("{\"id\":\"minecraft:diamond_sword\",\"count\":1,\"components\":{\"minecraft:custom_data\":{\"ExtraAttributes\":{\"id\":\"TEST\"}}}}", GSON.toJson(stackJson));
	}

	@Test
	void testComponentsAsString() {
		String componentString = ItemStackComponentizationFixer.componentsAsString(TEST_STACK);

		Assertions.assertEquals("[minecraft:enchantments={\"minecraft:sharpness\":1}]", componentString);
	}

	@Test
	void testFromComponentsString() {
		String componentString = "[minecraft:enchantments={\"minecraft:sharpness\":1}]";
		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString("minecraft:diamond_sword", 1, componentString);

		Assertions.assertTrue(ItemStack.isSameItemSameComponents(stack, TEST_STACK));
	}

	@Test
	void testFromComponentsStringWithInvalidItem() {
		String componentString = "[minecraft:enchantments={\"minecraft:sharpness\":1}]";
		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString("minecraft:does_not_exist", 1, componentString);

		Assertions.assertEquals(stack, ItemStack.EMPTY);
	}

	@Test
	void testNbtToComponentsString() {
		ItemStack fixedStack = ItemStackComponentizationFixer.fixUpItem(NBT);
		String componentsString = ItemStackComponentizationFixer.componentsAsString(fixedStack);

		Assertions.assertEquals("[minecraft:custom_data={ExtraAttributes:{id:\"TEST\"}}]", componentsString);
	}

	private static CompoundTag convertToNbt(String nbt) {
		try {
			return TagParser.parseCompoundFully(nbt);
		} catch (Exception e) {
			return new CompoundTag();
		}
	}
}
