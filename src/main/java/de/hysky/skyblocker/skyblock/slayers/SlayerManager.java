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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
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
 * <p>{@link #onChatMessage(Component, boolean)} detects slayer messages and updates the state of the slayer quest.
 * {@link #checkSlayerBoss(ArmorStand)} processes the given armor stand and detects if it is a slayer boss or miniboss.</p>
 */
public class SlayerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlayerManager.class);
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final Pattern PATTERN_XP_NEEDED = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern PATTERN_LVL_UP = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	private static final Title MINIBOSS_SPAWN = new Title(Component.translatable("skyblocker.slayer.miniBossSpawnAlert").withStyle(ChatFormatting.RED));
	private static final Title BOSS_SPAWN = new Title(Component.translatable("skyblocker.slayer.bossSpawnAlert").withStyle(ChatFormatting.RED));
	private static @Nullable SlayerQuest slayerQuest;
	private static @Nullable BossFight bossFight;

	private static boolean slayerExpBuffActive = false;
	private static float slayerExpBuff = 1.0f;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerManager::onChatMessage);
		SkyblockEvents.LOCATION_CHANGE.register(SlayerManager::onLocationChange);
		SkyblockEvents.AREA_CHANGE.register(SlayerManager::onAreaChange);
		SkyblockEvents.MAYOR_CHANGE.register(SlayerManager::onMayorChange);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	private static void onAreaChange(Area area) {
		if (area.equals(Area.CHATEAU)) {
			getSlayerBossInfo(false);
		}
	}

	private static void onMayorChange() {
		slayerExpBuffActive = false;
		slayerExpBuff = 1.0f;
		// TODO: Remove when Aura leaves office
		if (MayorUtils.getActivePerks().contains("Work Smarter")) {
			slayerExpBuffActive = true;
			slayerExpBuff *= 1.5f;
		}
		if (MayorUtils.getActivePerks().contains("Slayer XP Buff")) {
			slayerExpBuffActive = true;
			slayerExpBuff *= 1.25f;
		}
	}

	private static void onLocationChange(Location location) {
		slayerQuest = null;
		bossFight = null;
		Scheduler.INSTANCE.schedule(() -> getSlayerBossInfo(false), 20 * 2);
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onChatMessage(Component text, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		String message = text.getString();

		switch (message.stripLeading()) {
			case "Your Slayer Quest has been cancelled!", "SLAYER QUEST FAILED!" -> {
				slayerQuest = null;
				bossFight = null;
				CallMaddox.onSlayerFailed();
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
					CallMaddox.onBossKilled();
				}
				return true;
			}
			case "SLAYER QUEST COMPLETE!" -> {
				if (slayerQuest != null && bossFight != null && !bossFight.slain) {
					SlayerTimer.onBossDeath(bossFight.bossSpawnTime);
					CallMaddox.onBossKilled();
				}
				bossFight = null;
				return true;
			}
			case String s when s.startsWith("SLAYER MINI-BOSS") -> {
				if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert) {
					TitleContainer.addTitle(SlayerManager.MINIBOSS_SPAWN, 20);
					assert CLIENT.player != null;
					CLIENT.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
				}
			}
			default -> {}
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
		assert slayerQuest != null;
		int tier = slayerQuest.slayerTier.ordinal();
		if (tier == 0) {
			slayerQuest.bossesNeeded = -1;
			return;
		}

		int xpPerTier = slayerQuest.slayerType.xpPerTier[tier - 1];
		if (slayerExpBuffActive) {
			xpPerTier = (int) (xpPerTier * slayerExpBuff);
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
	 * {@link #findClosestMobEntity(EntityType, ArmorStand)} could be modified and run more than once to ensure the correct entity is found.
	 */
	public static void checkSlayerBoss(ArmorStand armorStand) {
		//noinspection DataFlowIssue - bossFight is checked in isBossSpawned()
		if (slayerQuest == null || !armorStand.hasCustomName() || (isBossSpawned() && bossFight.boss != null)) return;
		if (armorStand.getName().getString().contains(CLIENT.getUser().getName())) {
			for (Entity otherArmorStands : getEntityArmorStands(armorStand, 1.5f)) {
				Matcher matcher = SLAYER_PATTERN.matcher(otherArmorStands.getName().getString());
				if (matcher.find()) {
					if (bossFight != null && bossFight.boss == null) {
						bossFight.findBoss((ArmorStand) otherArmorStands);
						return;
					}
					bossFight = new BossFight((ArmorStand) otherArmorStands);
					return;
				}
			}
		}
		if (!armorStand.closerThan(CLIENT.player, 15)) return;
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
		return entity.level().getEntities(entity, entity.getBoundingBox().inflate(0.1F, expandY, 0.1F), x -> x instanceof ArmorStand && x.hasCustomName());
	}

	/**
	 * <p> Finds the closest matching Entity for the armorStand using entityType and armorStand age difference to filter
	 * out impossible candidates, returning the closest entity of those remaining in the search box by block distance </p>
	 *
	 * @param entityType the entity type of the Slayer (i.e. ZombieEntity.class)
	 * @param armorStand the entity that contains the display name of the Slayer (mini)boss
	 * @implNote This method is not perfect. Possible improvements could be sort by x and z distance only (ignore y difference).
	 */
	public static <T extends Entity> @Nullable T findClosestMobEntity(@Nullable EntityType<T> entityType, ArmorStand armorStand) {
		if (entityType == null) return null;
		List<T> mobEntities = armorStand.level().getEntities(entityType, armorStand.getBoundingBox().inflate(0, 1.5f, 0), SlayerManager::isValidSlayerMob);
		mobEntities.sort(Comparator.comparingDouble(armorStand::distanceToSqr));

		return switch (mobEntities.size()) {
			case 0 -> null;
			case 1 -> mobEntities.getFirst();
			default -> mobEntities.stream()
					.min(Comparator.comparingInt(entity -> Math.abs(entity.tickCount - armorStand.tickCount)))
					.get();
		};
	}

	/**
	 * Use this func to add checks to prevent accidental highlights
	 * i.e. Cavespider extends spider and thus will highlight the broodfather's head pet instead and
	 */
	private static boolean isValidSlayerMob(Entity entity) {
		return entity.isAlive() && // entity is alive
				!(entity instanceof Mob mob && mob.isBaby()) && // entity is not a baby
				!(entity instanceof CaveSpider); // entity is not a cave spider
	}

	/**
	 * Returns whether the given entity is a slayer miniboss or boss and should be highlighted based on the given highlight type.
	 */
	public static boolean shouldGlow(Entity entity, SlayersConfig.HighlightSlayerEntities highlightType) {
		if (!isInSlayer()) return false;
		//noinspection DataFlowIssue - slayerQuest is checked in isInSlayer()
		if (SkyblockerConfigManager.get().slayers.highlightMinis == highlightType && isInSlayer() && getSlayerQuest().minibosses.contains(entity)) return true;
		//noinspection DataFlowIssue - bossFight is checked in isBossSpawned()
		return SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && isBossSpawned() && getBossFight().boss == entity;
	}

	/**
	 * Returns the highlight bounding box for the given slayer boss armor stand entity.
	 * It's slightly larger and lower than the armor stand's bounding box.
	 */
	public static @Nullable AABB getSlayerMobBoundingBox(ArmorStand armorStand, float partialTick) {
		// lowkey we should figure out where the actual boss entity is, because the armor stand lags behind
		Vec3 lerpedPos = armorStand.getPosition(partialTick);
		return switch (getSlayerType()) {
			case SlayerType.REVENANT -> new AABB(lerpedPos.x - 0.4, lerpedPos.y - 0.1, lerpedPos.z - 0.4, lerpedPos.x + 0.4, lerpedPos.y - 2.2, lerpedPos.z + 0.4);
			case SlayerType.TARANTULA -> new AABB(lerpedPos.x - 0.9, lerpedPos.y - 0.2, lerpedPos.z - 0.9, lerpedPos.x + 0.9, lerpedPos.y - 1.2, lerpedPos.z + 0.9);
			case SlayerType.VOIDGLOOM -> new AABB(lerpedPos.x - 0.4, lerpedPos.y - 0.2, lerpedPos.z - 0.4, lerpedPos.x + 0.4, lerpedPos.y - 3, lerpedPos.z + 0.4);
			case SlayerType.SVEN -> new AABB(lerpedPos.x - 0.5, lerpedPos.y - 0.1, lerpedPos.z - 0.5, lerpedPos.x + 0.5, lerpedPos.y - 1, lerpedPos.z + 0.5);
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
		return isBossSpawned() && slayerQuest != null && slayerQuest.slayerType.equals(slayerType);
	}

	/**
	 * Checks if the player is in a Slayer Quest of the specified type,
	 * but no boss has spawned yet.
	 *
	 * @param slayerType The Slayer type to check against.
	 * @return True if in a Slayer Quest of the given type and waiting for a boss to spawn; false otherwise.
	 */
	public static boolean isInSlayerQuestType(SlayerType slayerType) {
		return !isBossSpawned() && slayerQuest != null && slayerQuest.slayerType.equals(slayerType);
	}

	/**
	 * Gets the current Boss Fight state.
	 *
	 * @return The BossFight instance, or null if no boss fight is active.
	 */
	public static @Nullable BossFight getBossFight() {
		return bossFight;
	}

	/**
	 * Gets the current Slayer Quest details.
	 *
	 * @return The SlayerQuest instance, or null if no Slayer Quest is active.
	 */
	public static @Nullable SlayerQuest getSlayerQuest() {
		return slayerQuest;
	}

	/**
	 * Gets the type of the current Slayer Quest.
	 *
	 * @return The SlayerType of the current quest, or null if no quest is active.
	 */
	public static @Nullable SlayerType getSlayerType() {
		return slayerQuest != null ? slayerQuest.slayerType : null;
	}

	/**
	 * Gets the tier of the current Slayer Quest.
	 *
	 * @return The SlayerTier of the current quest, or null if no quest is active.
	 */
	public static @Nullable SlayerTier getSlayerTier() {
		return slayerQuest != null ? slayerQuest.slayerTier : null;
	}

	/**
	 * Gets the armor stand entity associated with the Slayer boss.
	 *
	 * @return The armor stand entity, or null if no boss fight is active.
	 */
	public static @Nullable ArmorStand getSlayerBossArmorStand() {
		return bossFight != null ? bossFight.bossArmorStand : null;
	}

	/**
	 * Gets the entity representing the Slayer boss.
	 *
	 * @return The boss entity, or null if no boss fight is active.
	 */
	public static @Nullable Entity getSlayerBoss() {
		return bossFight != null ? bossFight.boss : null;
	}

	public static class BossFight {
		public @Nullable ArmorStand bossArmorStand;
		public @Nullable Entity boss;
		public Instant bossSpawnTime;
		public boolean slain = false;

		private BossFight(@Nullable ArmorStand armorStand) {
			findBoss(armorStand);
			bossSpawnTime = Instant.now();
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitle(BOSS_SPAWN, 20);
				assert CLIENT.player != null;
				CLIENT.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
			}
		}

		public void findBoss(@Nullable ArmorStand armorStand) {
			bossArmorStand = armorStand;
			//noinspection DataFlowIssue
			boss = armorStand != null ? findClosestMobEntity(slayerQuest.slayerType.mobType, armorStand) : null;
		}
	}

	public static class SlayerQuest {
		public SlayerType slayerType = SlayerType.UNKNOWN;
		public SlayerTier slayerTier = SlayerTier.UNKNOWN;
		public List<ArmorStand> minibossesArmorStand = new ArrayList<>();
		public List<Entity> minibosses = new ArrayList<>();
		public int level;
		public int xpRemaining;
		public int bossesNeeded;

		private void onMiniboss(ArmorStand armorStand, SlayerType type) {
			if (minibossesArmorStand.contains(armorStand)) return;
			minibossesArmorStand.add(armorStand);
			Entity miniboss = findClosestMobEntity(type.mobType, armorStand);
			if (miniboss == null) return;
			minibosses.add(miniboss);
		}
	}
}
