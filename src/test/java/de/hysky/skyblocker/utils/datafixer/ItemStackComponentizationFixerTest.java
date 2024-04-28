package de.hysky.skyblocker.utils.datafixer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Util;

public class ItemStackComponentizationFixerTest {
	private final NbtCompound NBT = convertToNbt("{id:\"minecraft:diamond_sword\",Count:1,tag:{ExtraAttributes:{id:\"TEST\"}}}");
	private final Gson GSON = new Gson();
	private final ItemStack TEST_STACK = Util.make(new ItemStack(Items.DIAMOND_SWORD, 1), item -> {
		ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

		builder.add(Enchantments.SHARPNESS, 1);
		item.set(DataComponentTypes.ENCHANTMENTS, builder.build());
	});

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testNbtConversion() {
		Assertions.assertNotEquals(NBT, new NbtCompound());
	}

	@Test
	void testDataFixer() {
		ItemStack fixedStack = ItemStackComponentizationFixer.fixUpItem(NBT);
		JsonElement stackJson = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, fixedStack).getOrThrow();

		Assertions.assertEquals("{\"id\":\"minecraft:diamond_sword\",\"count\":1,\"components\":{\"minecraft:custom_data\":{\"ExtraAttributes\":{\"id\":\"TEST\"}}}}", GSON.toJson(stackJson));
	}

	@Test
	void testComponentsAsString() {
		String componentString = ItemStackComponentizationFixer.componentsAsString(TEST_STACK);

		Assertions.assertEquals("[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]", componentString);
	}

	@Test
	void testFromComponentsString() {
		String componentString = "[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]";
		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString("minecraft:diamond_sword", 1, componentString);

		Assertions.assertTrue(ItemStack.areItemsAndComponentsEqual(stack, TEST_STACK));
	}

	@Test
	void testFromComponentsStringWithInvalidItem() {
		String componentString = "[minecraft:enchantments={levels:{\"minecraft:sharpness\":1}}]";
		ItemStack stack = ItemStackComponentizationFixer.fromComponentsString("minecraft:does_not_exist", 1, componentString);

		Assertions.assertEquals(stack, ItemStack.EMPTY);
	}

	@Test
	void testNbtToComponentsString() {
		ItemStack fixedStack = ItemStackComponentizationFixer.fixUpItem(NBT);
		String componentsString = ItemStackComponentizationFixer.componentsAsString(fixedStack);

		Assertions.assertEquals("[minecraft:custom_data={ExtraAttributes:{id:\"TEST\"}}]", componentsString);
	}

	private static NbtCompound convertToNbt(String nbt) {
		try {
			return StringNbtReader.parse(nbt);
		} catch (Exception e) {
			return new NbtCompound();
		}
	}
}
