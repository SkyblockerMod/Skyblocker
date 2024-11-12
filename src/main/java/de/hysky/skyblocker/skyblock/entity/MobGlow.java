package de.hysky.skyblocker.skyblock.entity;

import com.google.common.collect.Streams;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.crimson.slayer.AttunementColors;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.slayers.SlayerEntitiesGlow;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.WolfEntity;
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

	public static boolean shouldMobGlow(Entity entity) {
		return computeShouldMobGlow(entity);
	}

	private static boolean computeShouldMobGlow(Entity entity) {
		// Dungeons
		if (Utils.isInDungeons()) {
			String name = entity.getName().getString();

			return switch (entity) {
				// Minibosses
				case PlayerEntity p when (name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy")) && !DungeonManager.getBoss().isFloor(4) -> SkyblockerConfigManager.get().dungeons.starredMobGlow;
				case PlayerEntity p when entity.getId() == LividColor.getCorrectLividId() -> LividColor.shouldGlow(name);

				// Bats
				case BatEntity b -> SkyblockerConfigManager.get().dungeons.starredMobGlow || SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes;

				// Armor Stands
				case ArmorStandEntity _armorStand -> false;

				// Regular Mobs
				default -> SkyblockerConfigManager.get().dungeons.starredMobGlow && isStarred(entity);
			};
		}

		return switch (entity) {
			// Rift Blobbercyst
			case PlayerEntity p when Utils.isInTheRift() && p.getName().getString().equals("Blobbercyst ") -> SkyblockerConfigManager.get().otherLocations.rift.blobbercystGlow;

			// Dojo Helpers
			case ZombieEntity zombie when Utils.isInCrimson() && DojoManager.inArena -> DojoManager.shouldGlow(getArmorStandName(zombie));

			//Kuudra
			case MagmaCubeEntity magmaCube when Utils.isInKuudra() -> SkyblockerConfigManager.get().crimsonIsle.kuudra.kuudraGlow && magmaCube.getSize() == Kuudra.KUUDRA_MAGMA_CUBE_SIZE;

			// Special Zealot && Slayer (Mini)Boss
			case EndermanEntity enderman when Utils.isInTheEnd() -> TheEnd.isSpecialZealot(enderman) || SlayerEntitiesGlow.shouldGlow(enderman.getUuid());
			case ZombieEntity zombie when !(zombie instanceof ZombifiedPiglinEntity) && SlayerUtils.isInSlayerQuestType(SlayerUtils.REVENANT) -> SlayerEntitiesGlow.shouldGlow(zombie.getUuid());
			case SpiderEntity spider when SlayerUtils.isInSlayerQuestType(SlayerUtils.TARA) -> SlayerEntitiesGlow.shouldGlow(spider.getUuid());
			case WolfEntity wolf when SlayerUtils.isInSlayerQuestType(SlayerUtils.SVEN) -> SlayerEntitiesGlow.shouldGlow(wolf.getUuid());
			case BlazeEntity blaze when SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) -> SlayerEntitiesGlow.shouldGlow(blaze.getUuid());

			// Enderman Slayer's Nukekubi Skulls
			case ArmorStandEntity armorStand when Utils.isInTheEnd() && SlayerUtils.isInSlayer() && isNukekubiHead(armorStand) -> SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads;

			// Blaze Slayer's Demonic minions
			case WitherSkeletonEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW -> SlayerUtils.isInSlayerType(SlayerUtils.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15;
			case ZombifiedPiglinEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW -> SlayerUtils.isInSlayerType(SlayerUtils.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15;

			default -> false;
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

	public static int getGlowColor(Entity entity) {
		String name = entity.getName().getString();

		return switch (entity) {
			case PlayerEntity p when name.equals("Lost Adventurer") -> 0xfee15c;
			case PlayerEntity p when name.equals("Shadow Assassin") -> 0x5b2cb2;
			case PlayerEntity p when name.equals("Diamond Guy") -> 0x57c2f7;
			case PlayerEntity p when entity.getId() == LividColor.getCorrectLividId() -> LividColor.getGlowColor(name);
			case PlayerEntity p when name.equals("Blobbercyst ") -> Formatting.GREEN.getColorValue();

			case EndermanEntity enderman when TheEnd.isSpecialZealot(enderman) -> Formatting.RED.getColorValue();
			case ArmorStandEntity armorStand when isNukekubiHead(armorStand) -> 0x990099;
			case ZombieEntity zombie when Utils.isInCrimson() && DojoManager.inArena -> DojoManager.getColor();
			case MagmaCubeEntity magmaCube when Utils.isInKuudra() -> 0xf7510f;

			// Blaze Slayer Attunement Colours
			case ArmorStandEntity armorStand when SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) -> AttunementColors.getColor(armorStand);
			case BlazeEntity blaze when SlayerUtils.isInSlayer() -> AttunementColors.getColor(blaze);
			case ZombifiedPiglinEntity piglin when SlayerUtils.isInSlayer() -> AttunementColors.getColor(piglin);
			case WitherSkeletonEntity wSkelly when SlayerUtils.isInSlayer() -> AttunementColors.getColor(wSkelly);

			default -> 0xf57738;
		};
	}

	/**
	 * Compares the armor items of an armor stand to the Nukekubi head texture to determine if it is a Nukekubi head.
	 */
	private static boolean isNukekubiHead(ArmorStandEntity entity) {
		return Streams.stream(entity.getArmorItems()).map(ItemUtils::getHeadTexture).anyMatch(headTexture -> headTexture.contains(NUKEKUBI_HEAD_TEXTURE));
	}
}
