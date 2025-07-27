package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.ManiaIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.StakeIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.TwinClawsIndicator;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds all information related to slayer.
 * <p>{@link #onChatMessage(Text, boolean)} detects slayer messages and updates the state of the slayer quest.
 * {@link #checkSlayerBoss(ArmorStandEntity)} processes the given armor stand and detects if it is a slayer boss or miniboss.</p>
 */
public class SlayerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlayerManager.class);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final Pattern PATTERN_XP_NEEDED = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern PATTERN_LVL_UP = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	private static final Title MINIBOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.miniBossSpawnAlert").formatted(Formatting.RED));
	private static final Title BOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.bossSpawnAlert").formatted(Formatting.RED));
	private static SlayerQuest slayerQuest;
	private static BossFight bossFight;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerManager::onChatMessage);
		SkyblockEvents.LOCATION_CHANGE.register(SlayerManager::onLocationChange);
		SkyblockEvents.AREA_CHANGE.register(SlayerManager::onAreaChange);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	private static void onAreaChange(Area area) {
		if (area.equals(Area.CHATEAU)) {
			getSlayerBossInfo(false);
		}
	}

	private static void onLocationChange(Location location) {
		slayerQuest = null;
		bossFight = null;
		Scheduler.INSTANCE.schedule(() -> getSlayerBossInfo(false), 20 * 2);
	}

	private static boolean onChatMessage(Text text, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		String message = text.getString();

		switch (message.replaceFirst("^\\s+", "")) {
			case "Your Slayer Quest has been cancelled!", "SLAYER QUEST FAILED!" -> {
				slayerQuest = null;
				bossFight = null;
				return true;
			}
			case "SLAYER QUEST STARTED!" -> {
				if (slayerQuest == null) slayerQuest = new SlayerQuest();
				bossFight = null;
				return true;
			}
			case "NICE! SLAYER BOSS SLAIN!" -> {
				if (slayerQuest != null && bossFight != null) {
					bossFight.slain = true;
					SlayerTimer.onBossDeath(bossFight.bossSpawnTime);
				}
				return true;
			}
			case "SLAYER QUEST COMPLETE!" -> {
				if (slayerQuest != null && bossFight != null && !bossFight.slain)
					SlayerTimer.onBossDeath(bossFight.bossSpawnTime);
				bossFight = null;
				return true;
			}
		}

		if (slayerQuest == null) return true;
		Matcher matcherNextLvl = PATTERN_XP_NEEDED.matcher(message);
		Matcher matcherLvlUp = PATTERN_LVL_UP.matcher(message);

		if (matcherNextLvl.matches()) {
			if (message.contains("LVL MAXED OUT")) {
				slayerQuest.level = message.contains("Vampire") ? 5 : 9;
				slayerQuest.xpRemaining = -1;
				slayerQuest.bossesNeeded = -1;
			} else {
				String xpRemainingStr = matcherNextLvl.group(3);
				if (xpRemainingStr != null) {
					slayerQuest.level = Integer.parseInt(matcherNextLvl.group(2));
					slayerQuest.xpRemaining = Integer.parseInt(xpRemainingStr.replace(",", "").trim());
					calculateBossesNeeded();
				}
			}
		} else if (matcherLvlUp.matches()) {
			slayerQuest.level = Integer.parseInt(message.replaceAll("(\\d+).+", "$1"));
		}

		return true;
	}

	public static void calculateBossesNeeded() {
		int tier = slayerQuest.slayerTier.ordinal();
		if (tier == 0) {
			slayerQuest.bossesNeeded = -1;
			return;
		}

		int xpPerTier = slayerQuest.slayerType.xpPerTier[tier - 1];

		if (MayorUtils.getMayor().perks().stream().anyMatch(perk -> perk.name().equals("Slayer XP Buff")) || MayorUtils.getMinister().perk().name().equals("Slayer XP Buff")) {
			xpPerTier = (int) (xpPerTier * 1.25);
		}

		slayerQuest.bossesNeeded = (int) Math.ceil((double) slayerQuest.xpRemaining / xpPerTier);
	}

	public static void getSlayerBossInfo(boolean checkStatus) {
		if (checkStatus && slayerQuest == null) return;
		try {
			for (String line : Utils.STRING_SCOREBOARD) {
				Matcher matcher = SLAYER_TIER_PATTERN.matcher(line);
				if (matcher.find()) {
					if (slayerQuest == null || !matcher.group(1).equals(slayerQuest.slayerType.bossName) || !matcher.group(2).equals(slayerQuest.slayerTier.name())) {
						slayerQuest = new SlayerQuest();
					}
					slayerQuest.slayerType = SlayerType.fromBossName(matcher.group(1));
					slayerQuest.slayerTier = SlayerTier.valueOf(matcher.group(2));
				} else if (line.equals("Slay the boss!") && !isBossSpawned()) {
					bossFight = new BossFight(null);
				}
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to get slayer boss info", e);
		}
	}

	/**
	 * Checks if the given armor stand is a slayer boss or miniboss and saves it to the corresponding field.
	 * <p>This is the main mechanism for detecting slayer bosses and minibosses. All other features rely on information processed here.
	 *
	 * @implNote The resulting mob entity (not the armor stand entity) might not be entirely accurate.
	 * {@link #findClosestMobEntity(EntityType, ArmorStandEntity)} could be modified and run more than once to ensure the correct entity is found.
	 */
	public static void checkSlayerBoss(ArmorStandEntity armorStand) {
		if (slayerQuest == null || !armorStand.hasCustomName() || (isBossSpawned() && bossFight.boss != null)) return;
		if (armorStand.getName().getString().contains(CLIENT.getSession().getUsername())) {
			for (Entity otherArmorStands : getEntityArmorStands(armorStand, 1.5f)) {
				Matcher matcher = SLAYER_PATTERN.matcher(otherArmorStands.getName().getString());
				if (matcher.find()) {
					if (bossFight != null && bossFight.boss == null) {
						bossFight.findBoss((ArmorStandEntity) otherArmorStands);
						return;
					}
					bossFight = new BossFight((ArmorStandEntity) otherArmorStands);
					return;
				}
			}
		}
		if (!armorStand.isInRange(CLIENT.player, 15)) return;
		Arrays.stream(SlayerType.values()).forEach(type -> type.minibossNames.forEach((name) -> {
			if (armorStand.getName().getString().contains(name) && isInSlayerQuestType(type)) {
				slayerQuest.onMiniboss(armorStand, type);
			}
		}));
	}

	/**
	 * Gets nearby armor stands with custom names. Used to find other armor stands showing a different line of text above a slayer boss.
	 */
	public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
		return entity.getWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.1F, expandY, 0.1F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
	}

	/**
	 * <p> Finds the closest matching Entity for the armorStand using entityType and armorStand age difference to filter
	 * out impossible candidates, returning the closest entity of those remaining in the search box by block distance </p>
	 *
	 * @param entityType the entity type of the Slayer (i.e. ZombieEntity.class)
	 * @param armorStand the entity that contains the display name of the Slayer (mini)boss
	 * @implNote This method is not perfect. Possible improvements could be sort by x and z distance only (ignore y difference).
	 */
	public static <T extends Entity> T findClosestMobEntity(EntityType<T> entityType, ArmorStandEntity armorStand) {
		if (entityType == null) return null;
		List<T> mobEntities = armorStand.getWorld().getEntitiesByType(entityType, armorStand.getBoundingBox().expand(0, 1.5f, 0), SlayerManager::isValidSlayerMob);
		mobEntities.sort(Comparator.comparingDouble(armorStand::squaredDistanceTo));

		return switch (mobEntities.size()) {
			case 0 -> null;
			case 1 -> mobEntities.getFirst();
			default -> mobEntities.stream()
					.min(Comparator.comparingInt(entity -> Math.abs(entity.age - armorStand.age)))
					.get();
		};
	}

	/**
	 * Use this func to add checks to prevent accidental highlights
	 * i.e. Cavespider extends spider and thus will highlight the broodfather's head pet instead and
	 */
	private static boolean isValidSlayerMob(Entity entity) {
		return entity.isAlive() && // entity is alive
				!(entity instanceof MobEntity mob && mob.isBaby()) && // entity is not a baby
				!(entity instanceof CaveSpiderEntity); // entity is not a cave spider
	}

	/**
	 * Returns whether the given entity is a slayer miniboss or boss and should be highlighted based on the given highlight type.
	 */
	public static boolean shouldGlow(Entity entity, SlayersConfig.HighlightSlayerEntities highlightType) {
		if (!isInSlayer()) return false;
		if (SkyblockerConfigManager.get().slayers.highlightMinis == highlightType && isInSlayer() && getSlayerQuest().minibosses.contains(entity)) return true;
		return SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && isBossSpawned() && getBossFight().boss == entity;
	}

	/**
	 * Returns the highlight bounding box for the given slayer boss armor stand entity.
	 * It's slightly larger and lower than the armor stand's bounding box.
	 */
	public static Box getSlayerMobBoundingBox(ArmorStandEntity armorStand) {
		return switch (getSlayerType()) {
			case SlayerType.REVENANT -> new Box(armorStand.getX() - 0.4, armorStand.getY() - 0.1, armorStand.getZ() - 0.4, armorStand.getX() + 0.4, armorStand.getY() - 2.2, armorStand.getZ() + 0.4);
			case SlayerType.TARANTULA -> new Box(armorStand.getX() - 0.9, armorStand.getY() - 0.2, armorStand.getZ() - 0.9, armorStand.getX() + 0.9, armorStand.getY() - 1.2, armorStand.getZ() + 0.9);
			case SlayerType.VOIDGLOOM -> new Box(armorStand.getX() - 0.4, armorStand.getY() - 0.2, armorStand.getZ() - 0.4, armorStand.getX() + 0.4, armorStand.getY() - 3, armorStand.getZ() + 0.4);
			case SlayerType.SVEN -> new Box(armorStand.getX() - 0.5, armorStand.getY() - 0.1, armorStand.getZ() - 0.5, armorStand.getX() + 0.5, armorStand.getY() - 1, armorStand.getZ() + 0.5);
			case null -> null;
			default -> armorStand.getBoundingBox();
		};
	}

	/**
	 * Checks if the player is currently in a Slayer Quest.
	 * Note: This does not check whether a boss has spawned.
	 *
	 * @return True if the player is in a Slayer Quest; false otherwise.
	 */
	public static boolean isInSlayer() {
		return slayerQuest != null;
	}

	/**
	 * Checks if a Slayer Boss has spawned for the current Slayer Quest.
	 *
	 * @return True if the boss has spawned; false otherwise.
	 */
	public static boolean isBossSpawned() {
		return isInSlayer() && bossFight != null;
	}

	/**
	 * Checks if the player is in a Slayer Boss fight of the specified type.
	 *
	 * @param slayerType The Slayer type to check against.
	 * @return True if in a boss fight of the given Slayer type; false otherwise.
	 */
	public static boolean isInSlayerType(SlayerType slayerType) {
		return isBossSpawned() && slayerQuest.slayerType.equals(slayerType);
	}

	/**
	 * Checks if the player is in a Slayer Quest of the specified type,
	 * but no boss has spawned yet.
	 *
	 * @param slayerType The Slayer type to check against.
	 * @return True if in a Slayer Quest of the given type and waiting for a boss to spawn; false otherwise.
	 */
	public static boolean isInSlayerQuestType(SlayerType slayerType) {
		return !isBossSpawned() && slayerQuest.slayerType.equals(slayerType);
	}

	/**
	 * Gets the current Boss Fight state.
	 *
	 * @return The BossFight instance, or null if no boss fight is active.
	 */
	public static BossFight getBossFight() {
		return bossFight;
	}

	/**
	 * Gets the current Slayer Quest details.
	 *
	 * @return The SlayerQuest instance, or null if no Slayer Quest is active.
	 */
	public static SlayerQuest getSlayerQuest() {
		return slayerQuest;
	}

	/**
	 * Gets the type of the current Slayer Quest.
	 *
	 * @return The SlayerType of the current quest, or null if no quest is active.
	 */
	public static SlayerType getSlayerType() {
		return slayerQuest != null ? slayerQuest.slayerType : null;
	}

	/**
	 * Gets the tier of the current Slayer Quest.
	 *
	 * @return The SlayerTier of the current quest, or null if no quest is active.
	 */
	public static SlayerTier getSlayerTier() {
		return slayerQuest != null ? slayerQuest.slayerTier : null;
	}

	/**
	 * Gets the armor stand entity associated with the Slayer boss.
	 *
	 * @return The armor stand entity, or null if no boss fight is active.
	 */
	public static ArmorStandEntity getSlayerBossArmorStand() {
		return bossFight != null ? bossFight.bossArmorStand : null;
	}

	/**
	 * Gets the entity representing the Slayer boss.
	 *
	 * @return The boss entity, or null if no boss fight is active.
	 */
	public static Entity getSlayerBoss() {
		return bossFight != null ? bossFight.boss : null;
	}

	public static class BossFight {
		public ArmorStandEntity bossArmorStand;
		public Entity boss;
		public Instant bossSpawnTime;
		public boolean slain = false;

		private BossFight(ArmorStandEntity armorStand) {
			findBoss(armorStand);
			bossSpawnTime = Instant.now();
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitle(BOSS_SPAWN, 20);
				CLIENT.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
			}
		}

		public void findBoss(ArmorStandEntity armorStand) {
			bossArmorStand = armorStand;
			boss = armorStand != null ? findClosestMobEntity(slayerQuest.slayerType.mobType, armorStand) : null;
		}
	}

	public static class SlayerQuest {
		public SlayerType slayerType = SlayerType.UNKNOWN;
		public SlayerTier slayerTier = SlayerTier.UNKNOWN;
		public List<ArmorStandEntity> minibossesArmorStand = new ArrayList<>();
		public List<Entity> minibosses = new ArrayList<>();
		public int level;
		public int xpRemaining;
		public int bossesNeeded;

		private void onMiniboss(ArmorStandEntity armorStand, SlayerType type) {
			if (minibossesArmorStand.contains(armorStand)) return;
			minibossesArmorStand.add(armorStand);
			minibosses.add(findClosestMobEntity(type.mobType, armorStand));
			if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert) {
				TitleContainer.addTitle(SlayerManager.MINIBOSS_SPAWN, 20);
				CLIENT.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
			}
		}
	}
}
