package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.ManiaIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.StakeIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.TwinClawsIndicator;
import de.hysky.skyblocker.skyblock.slayers.features.SlainTime;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SlayerManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlayerManager.class);
	private static final Map<SlayerAction, Runnable> actions = new HashMap<>();
	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final Pattern PATTERN_FIXED = Pattern.compile("\\s*(?:Your Slayer Quest has been cancelled!|SLAYER QUEST STARTED!|NICE! SLAYER BOSS SLAIN!|SLAYER QUEST FAILED!)\\s*");
	private static final Pattern PATTERN_XP_NEEDED = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern PATTERN_LVL_UP = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	public static String slayerType = "";
	public static String slayerTier = "";
	public static int xpRemaining = 0;
	public static int level = -1;
	public static boolean bossSpawned;
	private static ArmorStandEntity slayerArmorStandEntity;
	private static MobEntity slayerEntity;
	private static Instant startTime;
	private static SlayerQuest quest;

	@Init
	public static void init() {
		actions.put(SlayerAction.CANCELLED, () -> quest = null);
		actions.put(SlayerAction.FAILED, () -> quest = null);
		actions.put(SlayerAction.STARTED, () -> quest = new SlayerQuest());
		actions.put(SlayerAction.SLAIN, () -> {
			if (quest != null) {
				quest.slain = true;
				SlainTime.onBossDeath(startTime);
			}
		});
		actions.put(SlayerAction.COMPLETE, () -> {
			if (quest != null && !quest.slain)
				SlainTime.onBossDeath(startTime);
			quest = null;
		});

		ClientReceiveMessageEvents.GAME.register(SlayerManager::onChatMessage);
		Scheduler.INSTANCE.scheduleCyclic(SlayerManager::getSlayerBossInfo, 20);
		Scheduler.INSTANCE.scheduleCyclic(SlayerManager::bossSpawnAlert, 10);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	private static void onChatMessage(Text text, boolean b) {
		String message = text.getString();
		Matcher matcherFixed = PATTERN_FIXED.matcher(message);
		Matcher matcherNextLvl = PATTERN_XP_NEEDED.matcher(message);
		Matcher matcherLvlUp = PATTERN_LVL_UP.matcher(message);

		if (matcherFixed.matches()) {
			for (SlayerAction action : SlayerAction.values()) {
				if (message.toLowerCase().contains(action.name().toLowerCase())) {
					actions.get(action).run();
					break;
				}
			}
		} else if (matcherNextLvl.matches()) {
			if (message.contains("LVL MAXED OUT")) {
				level = message.contains("Vampire") ? 5 : 9;
				xpRemaining = -1;
			} else {
				int xpIndex = message.indexOf("Next LVL in ") + "Next LVL in ".length();
				int xpEndIndex = message.indexOf(" XP!", xpIndex);
				if (xpEndIndex != -1) {
					level = Integer.parseInt(Pattern.compile("\\d+").matcher(message).results().map(m -> m.group()).findFirst().orElse(null));
					xpRemaining = Integer.parseInt(message.substring(xpIndex, xpEndIndex).trim().replace(",", ""));
				} else LOGGER.error("[Skyblocker] error getting xpNeeded (xpEndIndex == -1)");
			}
			actions.get(SlayerAction.COMPLETE).run();
		} else if (matcherLvlUp.matches()) {
			level = Integer.parseInt(message.replaceAll("(\\d+).+", "$1"));
			actions.get(SlayerAction.COMPLETE).run();
		}
	}

	private static void bossSpawnAlert() {
		try {
			for (String line : Utils.STRING_SCOREBOARD) {
				if (line.contains("Slay the boss!")) {
					if (quest != null && !bossSpawned && !quest.slain) {
						if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
							TitleContainer.addTitle(new Title(Text.literal(I18n.translate("skyblocker.slayer.bossSpawnAlert")).formatted(Formatting.RED)), 20);
							MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 0.1f);
						}
						bossSpawned = true;
						quest.lfMinis = false;
						startTime = Instant.now();
					}
					return;
				}
			}
			bossSpawned = false;
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to make a boss spawn alert", e);
		}
	}

	private static void getSlayerBossInfo() {
		try {
			for (String line : Utils.STRING_SCOREBOARD) {
				//if (line.equals("Boss slain!") && quest != null) quest.slain = true;
				Matcher matcher = SLAYER_TIER_PATTERN.matcher(line);
				if (matcher.find()) {
					if (!slayerType.isEmpty() && !matcher.group(1).equals(slayerType)) {
						xpRemaining = 0;
						level = -1;
					} else if (slayerType.isEmpty()) quest = new SlayerQuest();
					slayerType = matcher.group(1);
					slayerTier = matcher.group(2);
					return;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to get slayer boss info", e);
		}
	}

	//TODO: cache it (currently called every render tick)
	public static int calculateBossesNeeded() {
		int tier = RomanNumerals.romanToDecimal(slayerTier);
		if (tier == 0) return -1;

		int xpPerTier;
		if (slayerType.equals("Vampire")) {
			xpPerTier = SlayerConstants.vampireXpPerTier[tier - 1];
		} else {
			xpPerTier = SlayerConstants.regularXpPerTier[tier - 1];
		}

		if(MayorUtils.getMayor().perks().stream().anyMatch(perk -> perk.name().equals("Slayer XP Buff")) || MayorUtils.getMinister().perk().name().equals("Slayer XP Buff")) {
			xpPerTier = (int)(xpPerTier * 1.25);
		}

		return (int) Math.ceil((double) xpRemaining / xpPerTier);
	}

	//TODO: Cache this, probably included in Packet system
	public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
		return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.3F, expandY, 0.3F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
	}

	//Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
	public static ArmorStandEntity getSlayerArmorStandEntity() {
		// TODO: This should be set when the system to detect isInSlayer is made event-driven
		if (slayerArmorStandEntity != null && slayerArmorStandEntity.isAlive()) {
			return slayerArmorStandEntity;
		}

		if (MinecraftClient.getInstance().world != null) {
			for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
				if (entity.hasCustomName()) {
					String entityName = entity.getCustomName().getString();
					Matcher matcher = SLAYER_PATTERN.matcher(entityName);
					if (matcher.find()) {
						String username = MinecraftClient.getInstance().getSession().getUsername();
						for (Entity armorStand : getEntityArmorStands(entity, 1.5f)) {
							if (armorStand.getDisplayName().getString().contains(username)) {
								slayerArmorStandEntity = (ArmorStandEntity) entity;
								return slayerArmorStandEntity;
							}
						}
					}
				}
			}
		}

		slayerArmorStandEntity = null;
		return null;
	}

	public static MobEntity getSlayerEntity(Class<? extends MobEntity> entityClass) {
		if (slayerEntity != null && slayerEntity.isAlive()) {
			return slayerEntity;
		}

		ArmorStandEntity armorStand = getSlayerArmorStandEntity();
		if (armorStand != null) {
			slayerEntity = findClosestMobEntity(entityClass, armorStand);
			return slayerEntity;
		}

		slayerEntity = null;
		return null;
	}

	/**
	 * <p> Finds the closest matching MobEntity for the armorStand using entityClass and armorStand age difference to filter
	 * out impossible candidates, returning the closest mob of those remaining in the search box by block distance </p>
	 *
	 * @param entityClass the mob type of the Slayer (i.e. ZombieEntity.class)
	 * @param armorStand  the entity that contains the display name of the Slayer (mini)boss
	 */
	public static MobEntity findClosestMobEntity(Class<? extends MobEntity> entityClass, ArmorStandEntity armorStand) {
		List<MobEntity> mobEntities = armorStand.getWorld().getEntitiesByClass(entityClass, armorStand.getDimensions(null)
						.getBoxAt(armorStand.getPos()).expand(0.3f, 1.5f, 0.3f), entity -> !entity.isDead())
				.stream()
				.filter(SlayerManager::isValidSlayerMob)
				.sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(armorStand)))
				.collect(Collectors.toList());

		return switch (mobEntities.size()) {
			case 0 -> null;
			case 1 -> mobEntities.getFirst();
			default -> mobEntities.stream()
					.filter(entity -> entity.age > armorStand.age - 4 && entity.age < armorStand.age + 4)
					.findFirst()
					.orElse(mobEntities.getFirst());
		};
	}

	/**
	 * Use this func to add checks to prevent accidental highlights
	 * i.e. Cavespider extends spider and thus will highlight the broodfather's head pet instead and
	 */
	private static boolean isValidSlayerMob(MobEntity entity) {
		return !(entity instanceof CaveSpiderEntity) && !(entity.isBaby());
	}

	/**
	 * Returns whether client is in Slayer Quest or no.
	 * Note: does not check if boss spawned or no.
	 */
	public static boolean isInSlayer() {
		return quest != null;
	}

	/**
	 * Returns whether Slayer Boss Spawned or no.
	 */
	public static boolean isBossSpawned() {
		return quest != null && bossSpawned;
	}

	public static boolean isInSlayerType(String slayer) {
		return quest != null && bossSpawned && slayerType.equals(slayer);
	}

	public static boolean isInSlayerQuestType(String slayer) {
		return quest != null && slayerType.equals(slayer);
	}

	public static String getSlayerType() {
		return slayerType;
	}

	public static String getSlayerTier() {
		return slayerTier;
	}

	public static SlayerQuest getSlayerQuest() {
		return quest;
	}

	enum SlayerAction {
		CANCELLED,
		STARTED,
		COMPLETE,
		SLAIN,
		FAILED,
		NEXT_LVL,
		MAXED_OUT,
		LVL_UP
	}

	public static class SlayerQuest {

		public boolean slain = false;
		public boolean lfMinis = true;

		public SlayerQuest() {
		}

		public boolean isSlain() {
			return slain;
		}

		public boolean isLfMinis() {
			return lfMinis;
		}
	}

}
