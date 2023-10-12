package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;

import java.util.List;

public class StarredMobGlow {

	public static boolean shouldMobGlow(Entity entity) {
		Box box = entity.getBoundingBox();

		if (Utils.isInDungeons() && !entity.isInvisible() && OcclusionCulling.isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
			// Minibosses
			if (entity instanceof PlayerEntity) {
				switch (entity.getName().getString()) {
					case "Lost Adventurer", "Shadow Assassin", "Diamond Guy" -> {
						return true;
					}
				}
			}

			// Regular Mobs
			if (!(entity instanceof ArmorStandEntity)) {
				Box searchBox = box.expand(0, 2, 0);
				List<ArmorStandEntity> armorStands = entity.getWorld().getEntitiesByClass(ArmorStandEntity.class, searchBox, EntityPredicates.NOT_MOUNTED);

				if (!armorStands.isEmpty() && armorStands.get(0).getName().getString().contains("✯")) return true;
			}

			// Bats
			return entity instanceof BatEntity;
		}

		return false;
	}

	public static int getGlowColor(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return switch (entity.getName().getString()) {
				case "Lost Adventurer" -> 0xfee15c;
				case "Shadow Assassin" -> 0x5b2cb2;
				case "Diamond Guy" -> 0x57c2f7;
				default -> 0xf57738;
			};
		}

		return 0xf57738;
	}
}
