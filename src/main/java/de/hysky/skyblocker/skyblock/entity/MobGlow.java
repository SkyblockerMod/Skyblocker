package de.hysky.skyblocker.skyblock.entity;

import com.mojang.authlib.properties.Property;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class MobGlow {

	public static boolean shouldMobGlow(Entity entity) {
		Box box = entity.getBoundingBox();

		if (OcclusionCulling.getReducedCuller().isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
			String name = entity.getName().getString();

			// Dungeons
			if (Utils.isInDungeons() && !entity.isInvisible()) {
				return switch (entity) {
					// Minibosses
					case PlayerEntity p when name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy") -> SkyblockerConfigManager.get().locations.dungeons.starredMobGlow;
					case PlayerEntity p when LividColor.LIVID_NAMES.contains(name) -> LividColor.shouldGlow(name);

					// Bats
					case BatEntity b -> SkyblockerConfigManager.get().locations.dungeons.starredMobGlow || SkyblockerConfigManager.get().locations.dungeons.starredMobBoundingBoxes;

					// Armor Stands
					case ArmorStandEntity _armorStand -> false;

					// Regular Mobs
					default -> SkyblockerConfigManager.get().locations.dungeons.starredMobGlow && isStarred(entity);
				};
			}

			return switch (entity) {
				// Rift
				case PlayerEntity p when Utils.isInTheRift() && !entity.isInvisible() && name.equals("Blobbercyst ") -> SkyblockerConfigManager.get().locations.rift.blobbercystGlow;

				// Enderman Slayer
				// Highlights Nukekubi Heads
				case ArmorStandEntity armorStand when Utils.isInTheEnd() && SlayerUtils.isInSlayer() && isNukekubiHead(armorStand) -> SkyblockerConfigManager.get().slayer.endermanSlayer.highlightNukekubiHeads;

				// Special Zelot
				case EndermanEntity enderman when Utils.isInTheEnd() && !entity.isInvisible() -> TheEnd.isSpecialZealot(enderman);

				default -> false;
			};
		}

		return false;
	}

	/**
	 * Checks if an entity is starred by checking if its armor stand contains a star in its name.
	 * @param entity the entity to check.
	 * @return true if the entity is starred, false otherwise
	 */
	public static boolean isStarred(Entity entity) {
		List<ArmorStandEntity> armorStands = getArmorStands(entity);
		return !armorStands.isEmpty() && armorStands.getFirst().getName().getString().contains("✯");
	}

	public static List<ArmorStandEntity> getArmorStands(Entity entity) {
		return getArmorStands(entity.getWorld(), entity.getBoundingBox());
	}

	public static List<ArmorStandEntity> getArmorStands(World world, Box box) {
		return world.getEntitiesByClass(ArmorStandEntity.class, box.expand(0, 2, 0), EntityPredicates.NOT_MOUNTED);
	}

	public static int getGlowColor(Entity entity) {
		String name = entity.getName().getString();

		return switch (entity) {
			case PlayerEntity p when name.equals("Lost Adventurer") -> 0xfee15c;
			case PlayerEntity p when name.equals("Shadow Assassin") -> 0x5b2cb2;
			case PlayerEntity p when name.equals("Diamond Guy") -> 0x57c2f7;
			case PlayerEntity p when LividColor.LIVID_NAMES.contains(name) -> LividColor.getGlowColor(name);
			case PlayerEntity p when name.equals("Blobbercyst ") -> Formatting.GREEN.getColorValue();

			case EndermanEntity enderman when TheEnd.isSpecialZealot(enderman) -> Formatting.RED.getColorValue();
			case ArmorStandEntity armorStand when isNukekubiHead(armorStand) -> 0x990099;

			default -> 0xf57738;
		};
	}

	private static boolean isNukekubiHead(ArmorStandEntity entity) {
		for (ItemStack armorItem : entity.getArmorItems()) {
			// eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3 is texture id for the nukekubi head,
			// compare against it to exclusively find armorstands that are nukekubi heads
			// get the texture of the nukekubi head item itself and compare it
			String texture = ItemUtils.getHeadTexture(armorItem);

			return texture.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=");
		}
		return false;
	}
}
