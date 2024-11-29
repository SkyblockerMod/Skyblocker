package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.ManiaIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.StakeIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.TwinClawsIndicator;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final Pattern PATTERN_XP_NEEDED = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern PATTERN_LVL_UP = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	public static final Title MINIBOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.miniBossSpawnAlert").formatted(Formatting.RED));
	private static final Title BOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.bossSpawnAlert").formatted(Formatting.RED));
	public static int xpRemaining = 0;
	public static int level = -1;
	public static int bossesNeeded = -1;
	private static SlayerQuest quest;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.GAME.register(SlayerManager::onChatMessage);
		Scheduler.INSTANCE.scheduleCyclic(SlayerManager::getSlayerBossInfo, 20);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	private static void onChatMessage(Text text, boolean b) {
		String message = text.getString();

		Matcher matcherNextLvl = PATTERN_XP_NEEDED.matcher(message);
		Matcher matcherLvlUp = PATTERN_LVL_UP.matcher(message);

		switch (message.replaceFirst("^\\s+", "")) {
			case "Your Slayer Quest has been cancelled!", "SLAYER QUEST FAILED!":
				quest = null;
				return;
			case "SLAYER QUEST STARTED!":
				quest = new SlayerQuest();
				return;
			case "NICE! SLAYER BOSS SLAIN!":
				if (quest != null) {
					quest.slain = true;
					SlayerTimer.onBossDeath(quest.bossSpawnTime);
				}
				return;
			case "SLAYER QUEST COMPLETE!":
				if (quest != null && !quest.slain)
					SlayerTimer.onBossDeath(quest.bossSpawnTime);
				quest = null;
				return;
		}

		if (matcherNextLvl.matches()) {
			if (message.contains("LVL MAXED OUT")) {
				level = message.contains("Vampire") ? 5 : 9;
				xpRemaining = -1;
				bossesNeeded = -1;
			} else {
				// TODO: turn below into a regex
				int xpIndex = message.indexOf("Next LVL in ") + "Next LVL in ".length();
				int xpEndIndex = message.indexOf(" XP!", xpIndex);
				if (xpEndIndex != -1) {
					level = Integer.parseInt(Pattern.compile("\\d+").matcher(message).results().map(m -> m.group()).findFirst().orElse(null));
					xpRemaining = Integer.parseInt(message.substring(xpIndex, xpEndIndex).trim().replace(",", ""));
					calculateBossesNeeded();
				} else LOGGER.error("[Skyblocker] error getting xpNeeded (xpEndIndex == -1)");
			}
		} else if (matcherLvlUp.matches()) {
			level = Integer.parseInt(message.replaceAll("(\\d+).+", "$1"));
		}
	}

	public static void calculateBossesNeeded() {
		int tier = RomanNumerals.romanToDecimal(quest.slayerTier.name());
		if (tier == 0) {
			bossesNeeded = -1;
			return;
		}

		int xpPerTier = quest.slayerType.xpPerTier[tier - 1];

		if (MayorUtils.getMayor().perks().stream().anyMatch(perk -> perk.name().equals("Slayer XP Buff")) || MayorUtils.getMinister().perk().name().equals("Slayer XP Buff")) {
			xpPerTier = (int) (xpPerTier * 1.25);
		}

		bossesNeeded = (int) Math.ceil((double) xpRemaining / xpPerTier);
	}

	private static void getSlayerBossInfo() {
		if (quest == null) return;
		try {
			for (String line : Utils.STRING_SCOREBOARD) {
				Matcher matcher = SLAYER_TIER_PATTERN.matcher(line);
				if (matcher.find()) {
					if ((!quest.slayerType.isUnknown() && !matcher.group(1).equals(quest.slayerType.name()))
							|| (!quest.slayerTier.isUnknown() && !matcher.group(2).equals(quest.slayerTier.name()))) {
						xpRemaining = 0;
						level = -1;
						bossesNeeded = -1;
					} else if (quest.slayerType.isUnknown()) quest = new SlayerQuest();
					quest.slayerType = SlayerType.valueOf(matcher.group(1));
					quest.slayerTier = SlayerTier.valueOf(matcher.group(2));
					return;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to get slayer boss info", e);
		}
	}

	/**
	 * Checks if the given armor stand is a slayer boss or miniboss and saves it to the corresponding field.
	 * <p>This is the main mechanism for detecting slayer bosses and minibosses. All other features rely on information processed here.
	 * @implNote The resulting mob entity (not the armor stand entity) might not be entirely accurate.
	 * {@link #findClosestMobEntity(EntityType, ArmorStandEntity)} could be modified and run more than once to ensure the correct entity is found.
	 */
	public static void checkSlayerBoss(ArmorStandEntity armorStand) {
		if (quest == null || !armorStand.hasCustomName() || !armorStand.isInRange(MinecraftClient.getInstance().player, 15)) return;
		if (quest.waitingForMiniboss()) {
			Arrays.stream(SlayerType.values()).forEach(type -> type.minibossNames.forEach((name) -> {
				if (armorStand.getName().getString().contains(name) && isInSlayerQuestType(type)) {
					quest.onMiniboss(armorStand, type);
				}
			}));
		} else if (quest.waitingForBoss()) {
			Matcher matcher = SLAYER_PATTERN.matcher(armorStand.getName().getString());
			if (matcher.find()) {
				String username = MinecraftClient.getInstance().getSession().getUsername();
				for (Entity otherArmorStands : getEntityArmorStands(armorStand, 1.5f)) {
					if (otherArmorStands.getName().getString().contains(username)) {
						quest.onBoss(armorStand);
						break;
					}
				}
			}
		}
	}

	/**
	 * Gets nearby armor stands with custom names. Used to find other armor stands showing a different line of text above a slayer boss.
	 */
	public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
		return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0, expandY, 0), x -> x instanceof ArmorStandEntity && x.hasCustomName());
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
		List<T> mobEntities = armorStand.getWorld().getEntitiesByType(entityType, armorStand.getBoundingBox().expand(0, 1.5f, 0), SlayerManager::isValidSlayerMob);
		mobEntities.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(armorStand)));

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
		if (SkyblockerConfigManager.get().slayers.highlightMinis == highlightType && getSlayerQuest().miniboss == entity) return true;
		if (SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && getSlayerQuest().boss == entity) return true;
		return false;
	}

	/**
	 * Returns the highlight bounding box for the given slayer mob entity. It's slightly larger than the entity's bounding box.
	 */
	public static Box getSlayerMobBoundingBox(LivingEntity entity) {
		return switch (getSlayerType()) {
			case SlayerType.REVENANT -> new Box(entity.getX() - 0.4, entity.getY() - 0.1, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 2.2, entity.getZ() + 0.4);
			case SlayerType.TARANTULA -> new Box(entity.getX() - 0.9, entity.getY() - 0.2, entity.getZ() - 0.9, entity.getX() + 0.9, entity.getY() - 1.2, entity.getZ() + 0.9);
			case SlayerType.VOIDGLOOM -> new Box(entity.getX() - 0.4, entity.getY() - 0.2, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 3, entity.getZ() + 0.4);
			case SlayerType.SVEN -> new Box(entity.getX() - 0.5, entity.getY() - 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 1, entity.getZ() + 0.5);
			default -> entity.getBoundingBox();
		};
	}

	/**
	 * Returns whether client is in Slayer Quest or no.
	 * Note: does not check if boss spawned or no.
	 */
	public static boolean isInSlayer() {
		return quest != null;
	}

	/**
	 * Returns whether Slayer Boss Spawned or not.
	 */
	public static boolean isBossSpawned() {
		return quest != null && !quest.waitingForBoss();
	}

	/**
	 * Returns whether the player is in a slayer boss fight of the given type or not.
	 */
	public static boolean isInSlayerType(SlayerType slayer) {
		return isBossSpawned() && quest.slayerType.equals(slayer);
	}

	public static boolean isInSlayerQuestType(SlayerType slayer) {
		return quest != null && quest.slayerType.equals(slayer);
	}

	public static SlayerQuest getSlayerQuest() {
		return quest;
	}

	public static SlayerType getSlayerType() {
		return quest.slayerType;
	}

	public static SlayerTier getSlayerTier() {
		return quest.slayerTier;
	}

	public static ArmorStandEntity getSlayerBossArmorStand() {
		return quest.bossArmorStand;
	}

	/**
	 * @deprecated Use {@link #getSlayerBossArmorStand()} instead.
	 */
	@Deprecated
	public static ArmorStandEntity getSlayerArmorStandEntity() {
		return quest.bossArmorStand;
	}

	public static Entity getSlayerBoss() {
		return quest.boss;
	}

	public static class SlayerQuest {
		public SlayerType slayerType;
		public SlayerTier slayerTier;
		public ArmorStandEntity minibossArmorStand;
		public Entity miniboss;
		public ArmorStandEntity bossArmorStand;
		public Entity boss;
		public Instant bossSpawnTime;
		public boolean slain = false;

		public boolean waitingForMiniboss() {
			return minibossArmorStand == null;
		}

		public boolean waitingForBoss() {
			return bossArmorStand == null;
		}

		private void onMiniboss(ArmorStandEntity armorStand, SlayerType type) {
			minibossArmorStand = armorStand;
			miniboss = findClosestMobEntity(type.mobType, armorStand);
			if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert) {
				TitleContainer.addTitle(SlayerManager.MINIBOSS_SPAWN, 20);
				MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
			}
		}

		private void onBoss(ArmorStandEntity armorStand) {
			bossArmorStand = armorStand;
			boss = findClosestMobEntity(slayerType.mobType, armorStand);
			bossSpawnTime = Instant.now();
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitle(BOSS_SPAWN, 20);
				MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
			}
		}
	}
}
