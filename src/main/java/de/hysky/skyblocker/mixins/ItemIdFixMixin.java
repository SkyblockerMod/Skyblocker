package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DataFixUtils;

import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.datafixer.fix.ItemIdFix;

@Mixin(ItemIdFix.class)
public class ItemIdFixMixin {
	/**
	 * 1.8 Id Mappings From: https://www.minecraftinfo.com/IDList.htm
	 */
	@Unique
	private static final Int2ObjectMap<String> MISSING_ID_CONVERSIONS = DataFixUtils.make(new Int2ObjectOpenHashMap<>(), map -> {
		map.put(165, "minecraft:slime");
		map.put(166, "minecraft:barrier");
		map.put(167, "minecraft:iron_trapdoor");
		map.put(168, "minecraft:prismarine");
		map.put(169, "minecraft:sea_lantern");
		map.put(179, "minecraft:red_sandstone");
		map.put(180, "minecraft:red_sandstone_stairs");
		map.put(181, "minecraft:double_stone_slab2");
		map.put(182, "minecraft:stone_slab2");
		map.put(183, "minecraft:spruce_fence_gate");
		map.put(184, "minecraft:birch_fence_gate");
		map.put(185, "minecraft:jungle_fence_gate");
		map.put(186, "minecraft:dark_oak_fence_gate");
		map.put(187, "minecraft:acacia_fence_gate");
		map.put(188, "minecraft:spruce_fence");
		map.put(189, "minecraft:birch_fence");
		map.put(190, "minecraft:jungle_fence");
		map.put(191, "minecraft:dark_oak_fence");
		map.put(192, "minecraft:acacia_fence");
		map.put(409, "minecraft:prismarine_shard");
		map.put(410, "minecraft:prismarine_crystals");
		map.put(411, "minecraft:rabbit");
		map.put(412, "minecraft:cooked_rabbit");
		map.put(413, "minecraft:rabbit_stew");
		map.put(414, "minecraft:rabbit_foot");
		map.put(415, "minecraft:rabbit_hide");
		map.put(416, "minecraft:armor_stand");
		map.put(423, "minecraft:mutton");
		map.put(424, "minecraft:cooked_mutton");
		map.put(425, "minecraft:banner");
		map.put(427, "minecraft:spruce_door");
		map.put(428, "minecraft:birch_door");
		map.put(429, "minecraft:jungle_door");
		map.put(430, "minecraft:acacia_door");
		map.put(431, "minecraft:dark_oak_door");

		map.defaultReturnValue("minecraft:air");
	});

	//Mojang's map doesn't contain any post 1.7 numeric id -> string id mappings
	@ModifyReturnValue(method = "fromId", at = @At("RETURN"))
	private static String skyblocker$correctMissingIds(String original, int numericId) {
		return original.equals("minecraft:air") && LegacyItemStackFixer.ENABLE_DFU_FIXES.get() ? MISSING_ID_CONVERSIONS.get(numericId) : original;
	}
}
