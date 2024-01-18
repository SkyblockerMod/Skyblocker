package de.hysky.skyblocker.skyblock.entity;

import java.util.List;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobGlow {
	public static boolean shouldMobGlow(Entity entity) {
		Box box = entity.getBoundingBox();

		if (!entity.isInvisible() && OcclusionCulling.getReducedCuller().isVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
			String name = entity.getName().getString();

			// Dungeons
			if (Utils.isInDungeons()) {

				// Minibosses
				if (entity instanceof PlayerEntity) {
					switch (name) {
						case "Lost Adventurer", "Shadow Assassin", "Diamond Guy": return SkyblockerConfigManager.get().locations.dungeons.starredMobGlow;
						case "Arcade Livid", "Crossed Livid", "Doctor Livid", "Frog Livid", "Hockey Livid",
						"Purple Livid", "Scream Livid", "Smile Livid", "Vendetta Livid": return LividColor.shouldGlow(name);
					}
				}

				// Regular Mobs
				if (!(entity instanceof ArmorStandEntity)) {
					List<ArmorStandEntity> armorStands = getArmorStands(entity.getWorld(), box);

					if (!armorStands.isEmpty() && armorStands.get(0).getName().getString().contains("✯")) return SkyblockerConfigManager.get().locations.dungeons.starredMobGlow;
				}

				// Bats
				return SkyblockerConfigManager.get().locations.dungeons.starredMobGlow && entity instanceof BatEntity;
			}

			// Rift
			if (Utils.isInTheRift()) {
				if (entity instanceof PlayerEntity) {
					switch (name) {
						// They have a space in their name for some reason...
						case "Blobbercyst ": return SkyblockerConfigManager.get().locations.rift.blobbercystGlow;
					}
				}
			}



		}

		// Enderman Slayer
		// Highlights Nukekubi Heads
		if(SkyblockerConfigManager.get().slayer.endermanSlayer.highlightNukekubiHeads
				&& entity instanceof ArmorStandEntity) {
			// check for items in the armor sets
			for (net.minecraft.item.ItemStack it : entity.getArmorItems()) {
				// hacky way to check if an item is a player head w/o
				// some shenanigans
				if(!it.toString().startsWith("1 player_head"))
					continue;


				if (it.hasNbt()) {
					assert it.getNbt() != null;
					// eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3 is texture id
					// for the nukekubi head, compare against it to exclusively find
					// armorstands that are nukekubi heads
					if (it.getNbt().contains("SkullOwner")) {
						// get the texture of the nukekubi head item itself and
						// compare it
						var texture = it
								.getNbt()
								.getCompound("SkullOwner")
								.getCompound("Properties")
								.getList("textures", NbtElement.COMPOUND_TYPE)
								.getCompound(0)
								.getString("Value");

						return texture.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=");

					}
				}
			}
		}

		return false;
	}

	private static List<ArmorStandEntity> getArmorStands(World world, Box box) {
        return world.getEntitiesByClass(ArmorStandEntity.class, box.expand(0, 2, 0), EntityPredicates.NOT_MOUNTED);
	}

	public static int getGlowColor(Entity entity) {
		String name = entity.getName().getString();

		if (entity instanceof PlayerEntity) {
			return switch (name) {
				case "Lost Adventurer" -> 0xfee15c;
				case "Shadow Assassin" -> 0x5b2cb2;
				case "Diamond Guy" -> 0x57c2f7;
				case "Arcade Livid", "Crossed Livid", "Doctor Livid", "Frog Livid", "Hockey Livid",
				"Purple Livid", "Scream Livid", "Smile Livid", "Vendetta Livid" -> LividColor.getGlowColor(name);
				case "Blobbercyst " -> Formatting.GREEN.getColorValue();
				default -> 0xf57738;
			};
		}

		// copypaste nukekebi head logic
		if(entity instanceof ArmorStandEntity) {
			for (net.minecraft.item.ItemStack it : entity.getArmorItems()) {
				// hacky way to check if an item is a player head w/o
				// some shenanigans
				if(!it.toString().startsWith("1 player_head"))
					continue;


				if (it.hasNbt()) {
					assert it.getNbt() != null;
					// eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3 is texture id
					// for the nukekubi head, compare against it to exclusively find
					// armorstands that are nukekubi heads
					if (it.getNbt().contains("SkullOwner")) {
						// get the texture of the nukekebi head item itself and
						// compare it
						var texture = it
								.getNbt()
								.getCompound("SkullOwner")
								.getCompound("Properties")
								.getList("textures", NbtElement.COMPOUND_TYPE)
								.getCompound(0)
								.getString("Value");

						if(texture.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=")) {
							return 0x990099;
						}
					}
				}
			}
		}

		return 0xf57738;
	}
}
