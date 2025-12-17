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
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
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
	private static final Set<Entity> ENTITIES_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
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
		SkyblockEvents.PROFILE_CHANGE.register((_prev, _profile) -> slayerQuest = null);
		Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
		Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
	}

	public static void onAttackEntity(Entity entity) {
		if (ENTITIES_CACHE.contains(entity)) return;
		ENTITIES_CACHE.add(entity);

		// Return if player's slayer boss is detected
		if (isInSlayer() && bossFight != null && bossFight.playerBoss) return;
		if (Arrays.stream(SlayerType.values()).noneMatch(slayerType -> slayerType.mobType == entity.getType())) return;
		if (!isValidSlayerMob(entity)) return;

		ArmorStandEntity bossArmorStand = null;
		SlayerType slayerType = null;
		boolean playerBoss = false;

		String player = CLIENT.getSession().getUsername();
		for (Entity e : getEntityArmorStands(entity, 1.5f)) {
			if (e instanceof ArmorStandEntity armorStandEntity) {
				Matcher matcher = SLAYER_PATTERN.matcher(armorStandEntity.getName().getString());
				if (matcher.find()) {
					bossArmorStand = armorStandEntity;
					slayerType = SlayerType.fromBossName(matcher.group(0));
				} else if (armorStandEntity.getName().getString().contains(player)) {
					playerBoss = true;
				}
			}
		}

		if (bossArmorStand != null && slayerType != null) {
			bossFight = new BossFight(bossArmorStand, entity, playerBoss, slayerType);
		}
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
				CallMaddox.onSlayerFailed();
				return true;
			}
			case "SLAYER QUEST STARTED!" -> {
				return true;
			}
			case "NICE! SLAYER BOSS SLAIN!", "SLAYER QUEST COMPLETE!" -> {
				if (slayerQuest != null) {
					SlayerTimer.sendMessage();
					CallMaddox.onBossKilled();
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
				String bossName = matcher.group(1);
				String bossTier = matcher.group(2);
				if (slayerQuest == null ||
						!bossName.equals(slayerQuest.slayerType.bossName) ||
						!bossTier.equals(slayerQuest.slayerTier.name())) {
					SlayerType slayerType = SlayerType.fromBossName(bossName);
					assert slayerType != null;
					slayerQuest = new SlayerQuest(slayerType, SlayerTier.valueOf(bossTier));
				}
				active = true;
			} else if (line.equals("Slay the boss!")) {
				bossSpawned = true;
			}
		}

		if (slayerQuest != null) {
			slayerQuest.active = active;
			if (active && bossSpawned) slayerQuest.onBossSpawn();
			slayerQuest.bossSpawned = bossSpawned;
		}
	}

	/**
	 * Checks if the given armor stand is associated with a slayer boss or miniboss.
	 * <p>For boss detection, it looks for armor stands that contain the player's name and matches nearby armor stands against known slayer name patterns.</p>
	 * <p>For miniboss detection, it checks if the armor stand is within range and matches the current slayer type's minibosses.</p>
	 *
	 * @param armorStand the armor stand entity to check
	 */
	public static void checkSlayerBoss(ArmorStandEntity armorStand) {
		if (!isInSlayer() || (bossFight != null && bossFight.playerBoss) || !armorStand.hasCustomName()) return;
		String EntityName = armorStand.getName().getString();

		// Slayer Boss
		if (EntityName.contains(CLIENT.getSession().getUsername())) {
			for (Entity entity : getEntityArmorStands(armorStand, 1.5f)) {
				if (entity instanceof ArmorStandEntity armorStandEntity) {
					Matcher matcher = SLAYER_PATTERN.matcher(armorStandEntity.getName().getString());
					if (matcher.find()) {
						Entity boss = findClosestMobEntity(slayerQuest.slayerType.mobType, armorStand);
						if (boss != null) {
							bossFight = new BossFight(armorStandEntity, boss, true, slayerQuest.slayerType);
							slayerQuest.onBossSpawn();
							BossFight.alert();
						}
						return;
					}
				}
			}
		}

		// Slayer Miniboss
		if (armorStand.isInRange(CLIENT.player, 15) && slayerQuest.slayerType.isMiniboss(EntityName, slayerQuest.slayerTier)) {
			slayerQuest.onMinibossSpawn(armorStand);
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
	 * i.e. Cavespider extends spider and thus will highlight the broodfather's head pet instead
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
		if (SkyblockerConfigManager.get().slayers.highlightMinis == highlightType && isInSlayer() && slayerQuest.minibosses.contains(entity)) return true;
		return SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && isSelectedBoss(entity.getUuid());
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
	 * Gets the armor stand entity associated with the Slayer boss.
	 *
	 * @return The armor stand entity, or null if no boss fight is active.
	 */
	@Nullable
	public static ArmorStandEntity getSlayerBossArmorStand() {
		return bossFight != null ? bossFight.bossArmorStand : null;
	}

	public static boolean isSelectedBoss(UUID uuid) {
		return bossFight != null && bossFight.boss.getUuid().equals(uuid);
	}

	public static boolean isFightingSlayer() {
		return bossFight != null;
	}

	public static boolean isFightingSlayerType(SlayerType slayerType) {
		return bossFight != null && bossFight.slayerType.equals(slayerType);
	}

	public static class BossFight {
		public final ArmorStandEntity bossArmorStand;
		public final Entity boss;
		public final boolean playerBoss;
		public final SlayerType slayerType;

		private BossFight(ArmorStandEntity bossArmorStand, Entity boss, boolean playerBoss, SlayerType slayerType) {
			this.bossArmorStand = bossArmorStand;
			this.boss = boss;
			this.playerBoss = playerBoss;
			this.slayerType = slayerType;
		}

		private static void alert() {
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitleAndPlaySound(BOSS_SPAWN, 20);
			}
		}

		public static void remove() {
			SlayerManager.bossFight = null;
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

		public @Nullable Instant bossSpawnTime;
		public @Nullable Instant bossDeathTime;
		public boolean bossSpawned;

		private SlayerQuest(SlayerType slayerType, SlayerTier slayerTier) {
			this.slayerType = slayerType;
			this.slayerTier = slayerTier;
			var slayersData = SLAYERS_DATA.get();
			if (slayersData != null && slayersData.containsKey(slayerType)) {
				SlayerInfo slayerInfo = slayersData.get(slayerType);
				update(slayerInfo.level, slayerInfo.xpRemaining, false);
			} else {
				update(-1, -1, false);
			}
		}

		private void save() {
			var slayers = SLAYERS_DATA.computeIfAbsent(Object2ObjectOpenHashMap::new);
			if (slayers != null) {
				slayers.put(slayerType, new SlayerInfo(level, xpRemaining));
				SLAYERS_DATA.save();
			}
		}

		private void update(int level, int xpRemaining, boolean saveCache) {
			this.level = level;
			this.xpRemaining = xpRemaining <= 0 && level != -1 && level < slayerType.maxLevel ?
					slayerType.levelMilestones[level + 1] - slayerType.levelMilestones[level] : xpRemaining;
			if (saveCache) save();

			if (this.xpRemaining <= 0) {
				bossesNeeded = -1;
			} else {
				int xpPerTier = (int) (slayerType.xpPerTier[slayerTier.ordinal()] * slayerExpBuff);
				bossesNeeded = (int) Math.ceil((double) this.xpRemaining / xpPerTier);
			}
		}

		private void onBossSpawn() {
			if (!bossSpawned) {
				bossSpawnTime = Instant.now();
				bossDeathTime = null;
				bossSpawned = true;
			}
		}

		private void onMinibossSpawn(ArmorStandEntity armorStand) {
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
