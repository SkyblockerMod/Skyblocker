package de.hysky.skyblocker.skyblock.entity;

import com.google.common.collect.Streams;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.AttunementColors;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class MobGlow {
	/**
	 * The Nukekubi head texture id is eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3.
	 */
	private static final String NUKEKUBI_HEAD_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1MzQ5NjM0MzU5NjIsInByb2ZpbGVJZCI6ImQzNGFhMmI4MzFkYTRkMjY5NjU1ZTMzYzE0M2YwOTZjIiwicHJvZmlsZU5hbWUiOiJFbmRlckRyYWdvbiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=";
	private static final String FEL_HEAD_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcyMDAyNTQ4Njg2MywKICAicHJvZmlsZUlkIiA6ICIzZDIxZTYyMTk2NzQ0Y2QwYjM3NjNkNTU3MWNlNGJlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTcl83MUJsYWNrYmlyZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMjg2ZGFjYjBmMjE0NGQ3YTQxODdiZTM2YmJhYmU4YTk4ODI4ZjdjNzlkZmY1Y2UwMTM2OGI2MzAwMTU1NjYzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
	/**
	 * Cache for mob glow. Absence means the entity does not have custom glow.
	 * If an entity is in the cache, it must have custom glow.
	 */
	private static final Object2IntMap<Entity> CACHE = new Object2IntOpenHashMap<>();

	@Init
	public static void init() {
		// Clear the cache every tick
		ClientTickEvents.END_WORLD_TICK.register(client -> clearCache());
	}

	public static boolean atLeastOneMobHasCustomGlow() {
		return !CACHE.isEmpty();
	}

	public static boolean hasOrComputeMobGlow(Entity entity) {
		if (CACHE.containsKey(entity)) {
			return true;
		}
		int color = computeMobGlow(entity);
		if (color != 0) {
			CACHE.put(entity, color);
			return true;
		}
		return false;
	}

	public static int getMobGlow(Entity entity) {
		return CACHE.getInt(entity);
	}

	public static int getMobGlowOrDefault(Entity entity, int defaultColor) {
		return CACHE.getOrDefault(entity, defaultColor);
	}

	public static void clearCache() {
		CACHE.clear();
	}

	/**
	 * Computes the glow color for the given entity.
	 * <p>Only non-zero colors are valid.
	 */
	private static int computeMobGlow(Entity entity) {
		String name = entity.getName().getString();

		// Dungeons
		if (Utils.isInDungeons()) {
			return switch (entity) {
				// Minibosses
				case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Lost Adventurer") -> 0xfee15c;
				case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Shadow Assassin") -> 0x5b2cb2;
				case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Diamond Guy") -> 0x57c2f7;
				case PlayerEntity p when entity.getId() == LividColor.getCorrectLividId() && LividColor.shouldGlow(name) -> LividColor.getGlowColor(name);

				// Bats
				case BatEntity b when SkyblockerConfigManager.get().dungeons.starredMobGlow -> 0xf57738;

				// Fel Heads
				case ArmorStandEntity as when SkyblockerConfigManager.get().dungeons.starredMobGlow && as.isMarker() && as.hasStackEquipped(EquipmentSlot.HEAD) && ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD)).equals(FEL_HEAD_TEXTURE) -> 0xcc00fa; // Enderman eye color

				// Armor Stands
				case ArmorStandEntity _armorStand -> 0;

				// Regular Mobs
				case Entity e when SkyblockerConfigManager.get().dungeons.starredMobGlow && isStarred(entity) -> 0xf57738;
				default -> 0;
			};
		}

		// Slayer
		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.GLOW)) {
			return switch (entity) {
				case ArmorStandEntity e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				case BlazeEntity e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				default -> 0xf57738;
			};
		}

		return switch (entity) {
			// Rift Blobbercyst
			case PlayerEntity p when SkyblockerConfigManager.get().otherLocations.rift.blobbercystGlow && Utils.isInTheRift() && name.equals("Blobbercyst ") -> Formatting.GREEN.getColorValue();

			// Dojo Helpers
			case ZombieEntity zombie when Utils.isInCrimson() && DojoManager.inArena && DojoManager.shouldGlow(getArmorStandName(zombie)) -> DojoManager.getColor();

			//Kuudra
			case MagmaCubeEntity magmaCube when SkyblockerConfigManager.get().crimsonIsle.kuudra.kuudraGlow && Utils.isInKuudra() && magmaCube.getSize() == Kuudra.KUUDRA_MAGMA_CUBE_SIZE -> 0xf7510f;

			// Special Zealot && Slayer (Mini)Boss
			case EndermanEntity enderman when Utils.isInTheEnd() && TheEnd.isSpecialZealot(enderman) -> Formatting.RED.getColorValue();

			// Enderman Slayer's Nukekubi Skulls
			case ArmorStandEntity armorStand when SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads && Utils.isInTheEnd() && armorStand.isMarker() && SlayerManager.isInSlayer() && isNukekubiHead(armorStand) -> 0x990099;

			// Blaze Slayer's Demonic minions
			case WitherSkeletonEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15 -> AttunementColors.getColor(e);
			case ZombifiedPiglinEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15 -> AttunementColors.getColor(e);

			default -> 0;
		};
	}

	/**
	 * Checks if an entity is starred by checking if its armor stand contains a star in its name.
	 *
	 * @param entity the entity to check.
	 * @return true if the entity is starred, false otherwise
	 */
	public static boolean isStarred(Entity entity) {
		List<ArmorStandEntity> armorStands = getArmorStands(entity);
		return !armorStands.isEmpty() && armorStands.getFirst().getName().getString().contains("✯");
	}

	/**
	 * Returns name of entity by finding closed armor stand and getting name of that
	 *
	 * @param entity the entity to check
	 * @return the name string of the entities  label
	 */
	public static String getArmorStandName(Entity entity) {
		List<ArmorStandEntity> armorStands = getArmorStands(entity);
		if (armorStands.isEmpty()) {
			return "";
		}
		return armorStands.getFirst().getName().getString();
	}

	public static List<ArmorStandEntity> getArmorStands(Entity entity) {
		return getArmorStands(entity.getWorld(), entity.getBoundingBox());
	}

	public static List<ArmorStandEntity> getArmorStands(World world, Box box) {
		return world.getEntitiesByClass(ArmorStandEntity.class, box.expand(0, 2, 0), EntityPredicates.NOT_MOUNTED);
	}

	/**
	 * Compares the armor items of an armor stand to the Nukekubi head texture to determine if it is a Nukekubi head.
	 */
	private static boolean isNukekubiHead(ArmorStandEntity entity) {
		return Streams.stream(entity.getArmorItems()).map(ItemUtils::getHeadTexture).anyMatch(headTexture -> headTexture.contains(NUKEKUBI_HEAD_TEXTURE));
	}
}
