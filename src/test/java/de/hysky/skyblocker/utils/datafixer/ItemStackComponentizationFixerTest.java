package de.hysky.skyblocker.utils.datafixer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class ItemStackComponentizationFixerTest {
	private final NbtCompound NBT = convertToNbt("{id:\"minecraft:diamond_sword\",Count:1,tag:{ExtraAttributes:{id:\"TEST\"}}}");
	private final Gson GSON = new Gson();

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
		JsonElement stackJson = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, fixedStack).result().orElseThrow();

		Assertions.assertEquals("{\"id\":\"minecraft:diamond_sword\",\"components\":{\"minecraft:custom_data\":{\"ExtraAttributes\":{\"id\":\"TEST\"}}}}", GSON.toJson(stackJson));
	}

	private static NbtCompound convertToNbt(String nbt) {
		try {
			return StringNbtReader.parse(nbt);
		} catch (Exception e) {
			return new NbtCompound();
		}
	}
}
