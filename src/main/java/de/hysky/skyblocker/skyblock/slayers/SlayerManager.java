package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.ManiaIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.StakeIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.TwinClawsIndicator;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds all information related to slayer.
 * <p>{@link #onChatMessage(Text, boolean)} detects slayer messages and updates the state of the slayer quest.
 * {@link #checkSlayerBoss(ArmorStandEntity)} processes the given armor stand and detects if it is a slayer boss or miniboss.</p>
 */
public class SlayerManager {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Atoned Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend");
	private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
	private static final Pattern XP_NEEDED_PATTERN = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern LVL_UP_PATTERN = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	private static final Title BOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.bossSpawnAlert").formatted(Formatting.RED));
	private static final Title MINIBOSS_SPAWN = new Title(Text.translatable("skyblocker.slayer.miniBossSpawnAlert").formatted(Formatting.RED));
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("slayers_data.json");
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, SlayerInfo>> SLAYERS_DATA = new ProfiledData<>(FILE, SlayerInfo.CODEC);
	private static @Nullable SlayerQuest slayerQuest;
	private static @Nullable BossFight bossFight;

	private static float slayerExpBuff = 1.0f;

	public static void sendTestMessage(String text) {
		var player = MinecraftClient.getInstance().player;
		if (player != null) {
			player.sendMessage(Text.of("[Slayers] " + text), false);
		}
	}

	@Init
	public static void init() {
		SLAYERS_DATA.load();
		ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerManager::onChatMessage);
		SkyblockEvents.MAYOR_CHANGE.register(SlayerManager::onMayorChange);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	private static void onMayorChange() {
		slayerExpBuff = 1.0f;
		// TODO: Remove when Aura leaves office
		if (MayorUtils.getActivePerks().contains("Work Smarter")) {
			slayerExpBuff *= 1.5f;
		}
		if (MayorUtils.getActivePerks().contains("Slayer XP Buff")) {
			slayerExpBuff *= 1.25f;
		}
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onChatMessage(Text text, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		String message = text.getString();

		switch (message.replaceFirst("^\\s+", "")) {
			case "Your Slayer Quest has been cancelled!", "SLAYER QUEST FAILED!" -> {
				slayerQuest = null;
				bossFight = null;
				CallMaddox.onSlayerFailed();
				return true;
			}
			case "SLAYER QUEST STARTED!" -> {
				bossFight = null;
				return true;
			}
			case "NICE! SLAYER BOSS SLAIN!", "SLAYER QUEST COMPLETE!" -> {
				if (slayerQuest != null && bossFight != null) {
					SlayerTimer.onBossDeath(bossFight);
					CallMaddox.onBossKilled();
					bossFight = null;
				}
				return true;
			}
		}

		if (slayerQuest != null) {
			Matcher matcherNextLvl = XP_NEEDED_PATTERN.matcher(message);
			if (matcherNextLvl.matches()) {
				if (message.contains("LVL MAXED OUT")) {
					int level = message.contains("Vampire") ? 5 : 9;
					slayerQuest.update(level, -1, true);
				} else {
					String xpRemainingStr = matcherNextLvl.group(3);
					if (xpRemainingStr != null) {
						int level = Integer.parseInt(matcherNextLvl.group(2));
						int xpRemaining = Integer.parseInt(xpRemainingStr.replace(",", "").trim());
						slayerQuest.update(level, xpRemaining, true);
					}
				}
			} else if (LVL_UP_PATTERN.matcher(message).matches()) {
				int level = Integer.parseInt(message.replaceAll("(\\d+).+", "$1"));
				slayerQuest.update(level, -1, true);
			}
		}

		return true;
	}

	public static void checkSlayerQuest() {
		boolean active = false;
		boolean bossSpawned = false;

		for (String line : Utils.STRING_SCOREBOARD) {
			Matcher matcher = SLAYER_TIER_PATTERN.matcher(line);
			if (matcher.find()) {
				active = true;
				String bossName = matcher.group(1);
				String bossTier = matcher.group(2);
				if (slayerQuest == null ||
						!bossName.equals(slayerQuest.slayerType.bossName) ||
						!bossTier.equals(slayerQuest.slayerTier.name())) {
					SlayerType slayerType = SlayerType.fromBossName(bossName);
					assert slayerType != null;
					slayerQuest = new SlayerQuest(slayerType, SlayerTier.valueOf(bossTier));
				}
			} else if (line.equals("Slay the boss!") && bossFight == null) {
				bossSpawned = true;
			}
		}

		if (slayerQuest != null) {
			slayerQuest.active = active;
			if (active && bossSpawned) {
				bossFight = new BossFight(null);
			}
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
		//noinspection DataFlowIssue - bossFight is checked in isBossSpawned()
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
		if (!isBossSpawned() || !armorStand.isInRange(CLIENT.player, 15)) return;
		if (slayerQuest.slayerType.isMiniboss(armorStand.getName().getString(), slayerQuest.slayerTier)) {
			slayerQuest.onMiniboss(armorStand);
		}
	}

	/**
	 * Gets nearby armor stands with custom names. Used to find other armor stands showing a different line of text above a slayer boss.
	 */
	public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
		return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.1F, expandY, 0.1F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
	}

	/**
	 * <p> Finds the closest matching Entity for the armorStand using entityType and armorStand age difference to filter
	 * out impossible candidates, returning the closest entity of those remaining in the search box by block distance </p>
	 *
	 * @param entityType the entity type of the Slayer (i.e. ZombieEntity.class)
	 * @param armorStand the entity that contains the display name of the Slayer (mini)boss
	 * @implNote This method is not perfect. Possible improvements could be sort by x and z distance only (ignore y difference).
	 */
	@Nullable
	private static <T extends Entity> T findClosestMobEntity(@Nullable EntityType<T> entityType, ArmorStandEntity armorStand) {
		if (entityType == null) return null;
		List<T> mobEntities = armorStand.getEntityWorld().getEntitiesByType(entityType, armorStand.getBoundingBox().expand(0, 1.5f, 0), SlayerManager::isValidSlayerMob);
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
		//noinspection DataFlowIssue - bossFight is checked in isBossSpawned()
		return SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && isBossSpawned() && getBossFight().boss == entity;
	}

	/**
	 * Returns the highlight bounding box for the given slayer boss armor stand entity.
	 * It's slightly larger and lower than the armor stand's bounding box.
	 */
	@Nullable
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
		return slayerQuest != null && slayerQuest.active;
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
	 *
	 * @param slayerType The Slayer type to check against.
	 * @return True if in a Slayer Quest of the given type; false otherwise.
	 */
	public static boolean isInSlayerQuestType(SlayerType slayerType) {
		return isInSlayer() && slayerQuest.slayerType.equals(slayerType);
	}

	/**
	 * Gets the current Boss Fight state.
	 *
	 * @return The BossFight instance, or null if no boss fight is active.
	 */
	@Nullable
	public static BossFight getBossFight() {
		return bossFight;
	}

	/**
	 * Gets the current Slayer Quest details.
	 *
	 * @return The SlayerQuest instance, or null if no Slayer Quest is active.
	 */
	@Nullable
	public static SlayerQuest getSlayerQuest() {
		return slayerQuest;
	}

	/**
	 * Gets the type of the current Slayer Quest.
	 *
	 * @return The SlayerType of the current quest, or null if no quest is active.
	 */
	@Nullable
	public static SlayerType getSlayerType() {
		return slayerQuest != null ? slayerQuest.slayerType : null;
	}

	/**
	 * Gets the tier of the current Slayer Quest.
	 *
	 * @return The SlayerTier of the current quest, or null if no quest is active.
	 */
	@Nullable
	public static SlayerTier getSlayerTier() {
		return slayerQuest != null ? slayerQuest.slayerTier : null;
	}

	/**
	 * Gets the armor stand entity associated with the Slayer boss.
	 *
	 * @return The armor stand entity, or null if no boss fight is active.
	 */
	@Nullable
	public static ArmorStandEntity getSlayerBossArmorStand() {
		return bossFight != null ? bossFight.bossArmorStand : null;
	}

	/**
	 * Gets the entity representing the Slayer boss.
	 *
	 * @return The boss entity, or null if no boss fight is active.
	 */
	@Nullable
	public static Entity getSlayerBoss() {
		return bossFight != null ? bossFight.boss : null;
	}

	public static class BossFight {
		public @Nullable ArmorStandEntity bossArmorStand;
		public @Nullable Entity boss;
		public Instant bossSpawnTime;
		public boolean sentTime = false;

		private BossFight(@Nullable ArmorStandEntity armorStand) {
			findBoss(armorStand);
			bossSpawnTime = Instant.now();
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitleAndPlaySound(BOSS_SPAWN, 20);
			}
		}

		public void findBoss(@Nullable ArmorStandEntity armorStand) {
			bossArmorStand = armorStand;
			//noinspection DataFlowIssue
			boss = armorStand != null ? findClosestMobEntity(slayerQuest.slayerType.mobType, armorStand) : null;
		}
	}

	public static class SlayerQuest {
		public final SlayerType slayerType;
		public final SlayerTier slayerTier;
		public final List<ArmorStandEntity> minibossesArmorStand = new ArrayList<>();
		public final List<Entity> minibosses = new ArrayList<>();
		public boolean active = true;
		public int level;
		public int xpRemaining;
		public int bossesNeeded;

		private SlayerQuest(SlayerType slayerType, SlayerTier slayerTier) {
			this.slayerType = slayerType;
			this.slayerTier = slayerTier;
			var slayersData = SLAYERS_DATA.get();
			if (slayersData != null && slayersData.containsKey(slayerType)) {
				SlayerInfo slayerInfo = slayersData.get(slayerType);
				update(slayerInfo.level, slayerInfo.xpRemaining, false);
			}
		}

		private void update(int level, int xpRemaining, boolean updateCache) {
			this.level = level;
			this.xpRemaining = xpRemaining <= 0 && level < slayerType.maxLevel ? slayerType.levelMilestones[level] : xpRemaining;
			if (updateCache) updateCache();

			if (this.xpRemaining <= 0) {
				bossesNeeded = -1;
			} else {
				int tier = slayerTier.ordinal();
				int xpPerTier = (int) (slayerType.xpPerTier[tier - 1] * slayerExpBuff);
				bossesNeeded = (int) Math.ceil((double) this.xpRemaining / xpPerTier);
			}
		}

		private void updateCache() {
			var slayers = SLAYERS_DATA.computeIfAbsent(Object2ObjectOpenHashMap::new);
			if (slayers != null) {
				slayers.put(slayerType, new SlayerInfo(level, xpRemaining));
				SLAYERS_DATA.save();
			}
		}

		private void onMiniboss(ArmorStandEntity armorStand) {
			if (minibossesArmorStand.contains(armorStand)) return;
			minibossesArmorStand.add(armorStand);

			Entity miniboss = findClosestMobEntity(slayerType.mobType, armorStand);
			if (miniboss == null) return;
			minibosses.add(miniboss);

			if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert) {
				Title title = SkyblockerConfigManager.get().slayers.showMiniBossNameInAlert ?
						new Title((MutableText) armorStand.getCustomName()) : MINIBOSS_SPAWN;
				TitleContainer.addTitleAndPlaySound(title, 20);
			}
		}
	}

	public record SlayerInfo(int level, int xpRemaining) {
		private static final Codec<SlayerInfo> SLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("level").forGetter(SlayerInfo::level),
				Codec.INT.fieldOf("xpRemaining").forGetter(SlayerInfo::xpRemaining)
		).apply(instance, SlayerInfo::new));

		private static final Codec<Object2ObjectOpenHashMap<SlayerType, SlayerInfo>> CODEC = Codec.unboundedMap(SlayerType.CODEC, SLAYER_CODEC)
				.xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}
}
