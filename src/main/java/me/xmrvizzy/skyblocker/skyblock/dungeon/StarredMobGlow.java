package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.util.List;

import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.culling.OcclusionCulling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;

public class StarredMobGlow {

	public static boolean shouldMobGlow(Entity entity) {
		Box box = entity.getBoundingBox();
		
		if (Utils.isInDungeons() && !entity.isInvisible() && OcclusionCulling.isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
			//Minibosses
			if (entity instanceof PlayerEntity) {
				switch (entity.getName().getString()) {
					case "Lost Adventurer": return true;
					case "Shadow Assassin": return true;
					case "Diamond Guy": return true;
				}
			}
			
			//Regular Mobs
			if (!(entity instanceof ArmorStandEntity)) {
				Box searchBox = box.expand(0, 2, 0);
				List<ArmorStandEntity> armorStands = entity.getWorld().getEntitiesByClass(ArmorStandEntity.class, searchBox, EntityPredicates.NOT_MOUNTED);
				
				if (!armorStands.isEmpty() && armorStands.get(0).getName().getString().contains("✯")) return true;
			}
		}
		
		return false;
	}
	
	public static int getGlowColor(Entity entity) {
		if (entity instanceof PlayerEntity) {
			switch (entity.getName().getString()) {
				case "Lost Adventurer": return 0xfee15c;
				case "Shadow Assassin": return 0x5b2cb2;
				case "Diamond Guy": return 0x57c2f7;
			}
		}
		
		return 0xf57738;
	}
}
