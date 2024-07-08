package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.crimson.slayer.AttunementColours;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.slayers.SlayerEntitiesGlow;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

public class MobGlow {

	private static final long GLOW_CACHE_DURATION = 1000; // 1 seconds for glow computation cache
	private static final long CAN_SEE_CACHE_DURATION = 100; // 100 milliseconds for canSee cache (every 2 ticks)
	private static final ConcurrentHashMap<Entity, CacheEntry<Boolean>> glowCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Entity, CacheEntry<Boolean>> canSeeCache = new ConcurrentHashMap<>();
	private static final RandomGenerator generator = RandomGenerator.getDefault();

	public static boolean shouldMobGlow(Entity entity) {
		CacheEntry<Boolean> cachedCanSee = canSeeCache.get(entity);
		long currentTime = System.currentTimeMillis();

		if (cachedCanSee != null && (currentTime - cachedCanSee.timestamp) < CAN_SEE_CACHE_DURATION) return cachedCanSee.value;

		CacheEntry<Boolean> cachedGlow = glowCache.get(entity);

		if (cachedGlow != null && cachedGlow.value && (currentTime - cachedGlow.timestamp) < GLOW_CACHE_DURATION){
			boolean canSee = MinecraftClient.getInstance().player.canSee(entity);
			canSeeCache.put(entity, new CacheEntry<>(canSee, currentTime));
			return canSee;
		} else if (cachedGlow != null && (currentTime - cachedGlow.timestamp) < GLOW_CACHE_DURATION) return false;

		boolean result = computeShouldMobGlow(entity);
		glowCache.put(entity, new CacheEntry<>(result, currentTime + generator.nextLong(50)));
		if (!result) return false;

		boolean canSee = MinecraftClient.getInstance().player.canSee(entity);
		canSeeCache.put(entity, new CacheEntry<>(canSee, currentTime));
		return canSee;
	}

	private static boolean computeShouldMobGlow(Entity entity) {
		String name = entity.getName().getString();

		// Dungeons
		if (Utils.isInDungeons() && !entity.isInvisible()) {
			return switch (entity) {
				case PlayerEntity p when name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy") -> SkyblockerConfigManager.get().dungeons.starredMobGlow;
				case PlayerEntity p when LividColor.LIVID_NAMES.contains(name) -> LividColor.shouldGlow(name);
				case BatEntity b -> SkyblockerConfigManager.get().dungeons.starredMobGlow || SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes;
				case ArmorStandEntity _armorStand -> false;
				default -> SkyblockerConfigManager.get().dungeons.starredMobGlow && isStarred(entity);
			};
		}


		return switch (entity) {
			// Rift
			case PlayerEntity p when Utils.isInTheRift() && !entity.isInvisible() && name.equals("Blobbercyst ") -> SkyblockerConfigManager.get().otherLocations.rift.blobbercystGlow;

			// Enderman Slayer
			// Highlights Nukekubi Heads
			case ArmorStandEntity armorStand when Utils.isInTheEnd() && SlayerUtils.isInSlayer() && isNukekubiHead(armorStand) -> SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads;

			//dojo
			case ZombieEntity zombie when Utils.isInCrimson() && DojoManager.inArena -> DojoManager.shouldGlow(getArmorStandName(zombie));

			// Special Zealot && Slayer (Mini)Boss
			case EndermanEntity enderman when Utils.isInTheEnd() && !entity.isInvisible() ->
					TheEnd.isSpecialZealot(enderman) || SlayerEntitiesGlow.shouldGlow(enderman.getUuid());
			case ZombieEntity zombie when !(zombie instanceof ZombifiedPiglinEntity) && SlayerUtils.isInSlayerQuestType(SlayerUtils.REVENANT) -> SlayerEntitiesGlow.shouldGlow(zombie.getUuid());
			case SpiderEntity spider when SlayerUtils.isInSlayerQuestType(SlayerUtils.TARA) -> SlayerEntitiesGlow.shouldGlow(spider.getUuid());
			case WolfEntity wolf when SlayerUtils.isInSlayerQuestType(SlayerUtils.SVEN)-> SlayerEntitiesGlow.shouldGlow(wolf.getUuid());
			case BlazeEntity blaze when SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) -> SlayerEntitiesGlow.shouldGlow(blaze.getUuid());
			// Blaze Slayer's Demons
			case WitherSkeletonEntity e when SkyblockerConfigManager.get().slayers.highlightBosses.toString().equals("GLOW") ->
					SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 10;
			case ZombifiedPiglinEntity e when SkyblockerConfigManager.get().slayers.highlightBosses.toString().equals("GLOW") ->
					SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) && e.distanceTo(MinecraftClient.getInstance().player) <= 10;
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
			case PlayerEntity p when LividColor.LIVID_NAMES.contains(name) -> LividColor.getGlowColor(name);
			case PlayerEntity p when name.equals("Blobbercyst ") -> Formatting.GREEN.getColorValue();

			case EndermanEntity enderman when TheEnd.isSpecialZealot(enderman) -> Formatting.RED.getColorValue();
			case ArmorStandEntity armorStand when isNukekubiHead(armorStand) -> 0x990099;
			case ZombieEntity zombie when Utils.isInCrimson() && DojoManager.inArena -> DojoManager.getColor();

			// Blaze Slayer Attunement Colours
			case ArmorStandEntity armorStand when SlayerUtils.isInSlayerQuestType(SlayerUtils.DEMONLORD) && SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights -> AttunementColours.	getColour(armorStand);
			case BlazeEntity blaze when SlayerUtils.isInSlayer() && SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights -> AttunementColours.getColour(blaze) ;
			case ZombifiedPiglinEntity piglin when SlayerUtils.isInSlayer() && SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights -> AttunementColours.getColour(piglin);
			case WitherSkeletonEntity wSkelly when SlayerUtils.isInSlayer() && SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights -> AttunementColours.getColour(wSkelly);

			default -> 0xf57738;
		};
	}

	private static boolean isNukekubiHead(ArmorStandEntity entity) {
		for (ItemStack armorItem : entity.getArmorItems()) {
			// eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3 is texture id for the nukekubi head,
			// compare against it to exclusively find armorstands that are nukekubi heads
			// get the texture of the nukekubi head item itself and compare it
			String texture = ItemUtils.getHeadTexture(armorItem);
			if (texture.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=")) {
				return true;
			}
		}
		return false;
	}

	private static class CacheEntry<T> {
		final T value;
		final long timestamp;

		CacheEntry(T value, long timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}
	}

	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(MobGlow::clearCache, 100*20);
	}

	private static void clearCache() {
		canSeeCache.clear();
		glowCache.clear();
	}
}
