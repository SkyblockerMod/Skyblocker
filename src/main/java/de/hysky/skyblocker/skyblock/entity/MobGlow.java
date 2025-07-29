package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class MobGlow {
	public static final int NO_GLOW = 0;
	private static final List<MobGlowAdder> ADDERS = new ArrayList<>();
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

	protected static void registerGlowAdder(MobGlowAdder adder) {
		ADDERS.add(adder);
	}

	public static boolean hasOrComputeMobGlow(Entity entity) {
		if (CACHE.containsKey(entity)) {
			return true;
		}
		int color = computeMobGlow(entity);
		if (color != NO_GLOW) {
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
		for (MobGlowAdder adder : ADDERS) {
			if (adder.isEnabled()) {
				int glowColour = adder.computeColour(entity);

				if (glowColour != NO_GLOW) return glowColour;
			}
		}

		// Slayer
		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.GLOW)) {
			return switch (entity) {
				case ArmorStandEntity e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				case BlazeEntity e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				default -> 0xf57738;
			};
		}

		if (Utils.isInGalatea()) {
			return switch (entity) {
				case ShulkerEntity shulker when shulker.getColor() == DyeColor.GREEN && SkyblockerConfigManager.get().hunting.huntingMobs.highlightHideonleaf -> DyeColor.YELLOW.getSignColor();

				// TODO: Try to add diff glows depending on the nametag (the state)
				case AxolotlEntity axolotl when SkyblockerConfigManager.get().hunting.huntingMobs.highlightCoralot -> DyeColor.YELLOW.getSignColor();

				case TurtleEntity turtle when SkyblockerConfigManager.get().hunting.huntingMobs.highlightShellwise -> DyeColor.PURPLE.getSignColor();

				default -> NO_GLOW;
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

			// Pests
			case ArmorStandEntity armorStand when SkyblockerConfigManager.get().farming.garden.pestHighlighter && Utils.isInGarden() && isPestHead(armorStand) -> 0xb62f00;

			// Blaze Slayer's Demonic minions
			case WitherSkeletonEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15 -> AttunementColors.getColor(e);
			case ZombifiedPiglinEntity e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 15 -> AttunementColors.getColor(e);

			//Chivalrous Carnival
			case ZombieEntity zombie when ZombieShootout.isInZombieShootout() -> ZombieShootout.getZombieGlowColor(zombie);
			case ArmorStandEntity armorStand when CatchAFish.isInCatchAFish() -> CatchAFish.getFishGlowColor(armorStand);

			default -> NO_GLOW;
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

		return NO_GLOW;
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
}
