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
import de.hysky.skyblocker.skyblock.slayers.features.CallMaddox;
import de.hysky.skyblocker.skyblock.slayers.features.SlayerTimer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.Nullable;

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
 * Holds all information related to slayers.
 *
 * <p>This class keeps two pieces of state:<br>
 * - {@link SlayerQuest}: the player's slayer quest<br>
 * - {@link BossFight}: the boss the player is fighting
 * </p>
 *
 * <p>Core methods:<br>
 * - {@link #checkSlayerQuest()} reads the scoreboard to detect whether a quest is active and updates tier/progress.<br>
 * - {@link #checkSlayerBoss(ArmorStand)} inspects armorStands to detect the player's spawned boss or minibosses.<br>
 * - {@link #onEntityAttack(Player, Level, InteractionHand, Entity, EntityHitResult)} detects other players' bosses on hit, also serves as a fallback for the player's own boss.<br>
 * - {@link #findBoss(Entity)} is the shared resolver that converts an armorStand or a mob into a {@link BossFight}
 * </p>
 */
public class SlayerManager {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern SLAYER_PATTERN = Pattern.compile("\\b(Revenant Horror|Atoned Horror|Tarantula Broodfather|Conjoined Brood|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend|Bloodfiend)(?:\\s+(V|IV|III|II|I))?\\b");
	private static final Pattern XP_NEEDED_PATTERN = Pattern.compile("\\s*(Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL ([0-9]) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)\\s*");
	private static final Pattern LVL_UP_PATTERN = Pattern.compile("\\s*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9]\\s*");
	private static final Title BOSS_SPAWN = new Title(Component.translatable("skyblocker.slayer.bossSpawnAlert").withStyle(ChatFormatting.RED));
	private static final Title MINIBOSS_SPAWN = new Title(Component.translatable("skyblocker.slayer.miniBossSpawnAlert").withStyle(ChatFormatting.RED));
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("slayers_data.json");
	private static final ProfiledData<Object2ObjectOpenHashMap<SlayerType, SlayerInfo>> SLAYERS_DATA = new ProfiledData<>(FILE, SlayerInfo.CODEC);
	private static final Set<Entity> ENTITIES_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
	private static @Nullable SlayerQuest slayerQuest;
	private static @Nullable BossFight bossFight;

	private static float slayerExpBuff = 1.0f;

	@Init
	public static void init() {
		SLAYERS_DATA.load();
		ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerManager::onChatMessage);
		SkyblockEvents.MAYOR_CHANGE.register(SlayerManager::onMayorChange);
		SkyblockEvents.PROFILE_CHANGE.register((_prev, _profile) -> slayerQuest = null);
		ClientPlayConnectionEvents.JOIN.register((_c, _p, _m) -> bossFight = null);
		AttackEntityCallback.EVENT.register(SlayerManager::onEntityAttack);
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
	private static boolean onChatMessage(Component text, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		String message = text.getString();

		switch (message.stripLeading()) {
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
			case String s when s.startsWith("SLAYER MINI-BOSS") -> {
				if (SkyblockerConfigManager.get().slayers.miniBossSpawnAlert && !SkyblockerConfigManager.get().slayers.alertOtherMinibosses) {
					TitleContainer.addTitleAndPlaySound(MINIBOSS_SPAWN, 20);
				}
			}
			default -> {}
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

	/**
	 * Called when the player attacks an entity.
	 *
	 * <p>Primarily used to detect other players' Slayer bosses when you hit them.</p>
	 * <p>If the player is currently fighting their own boss, this method will not switch the selected boss to another
	 * player's boss.</p>
	 * <p>As a secondary role, it may also detect the player's own boss when {@link #checkSlayerBoss(ArmorStand)}
	 * missed it (e.g., the boss spawned far away).</p>
	 */
	@SuppressWarnings("SameReturnValue")
	private static InteractionResult onEntityAttack(Player player, Level level, InteractionHand interactionHand, Entity entity, @Nullable EntityHitResult entityHitResult) {
		if (ENTITIES_CACHE.contains(entity)) return InteractionResult.PASS;
		ENTITIES_CACHE.add(entity);

		if (Arrays.stream(SlayerType.values()).noneMatch(slayer -> slayer.mobType == entity.getType())) return InteractionResult.PASS;

		BossFight boss = findBoss(entity);
		if (boss != null) {
			if (!isSlayerArmorStandAlive() || (boss.playerBoss && bossFight != null && bossFight.armorStand != boss.armorStand)) {
				bossFight = boss;
				ENTITIES_CACHE.clear();
				ENTITIES_CACHE.add(entity);
			}
		}

		return InteractionResult.PASS;
	}

	/**
	 * Detects and updates the player's {@link SlayerQuest} from the scoreboard.
	 *
	 * <p>This method is responsible for determining whether a Slayer quest is active and updating it.</p>
	 */
	public static void checkSlayerQuest() {
		boolean active = false;
		boolean bossSpawned = false;

		for (String line : Utils.STRING_SCOREBOARD) {
			Matcher matcher = SLAYER_PATTERN.matcher(line);
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
	 * Detects the player's spawned Slayer boss and Slayer minibosses from armorStands.
	 *
	 * <p>This method is responsible for the player's own boss/miniboss detection (not other players' bosses). When a boss or
	 * miniboss is detected, the quest/boss state is updated and alerts may be triggered.</p>
	 *
	 * @param armorStand an armor stand to inspect
	 */
	public static void checkSlayerBoss(ArmorStand armorStand) {
		if (!armorStand.hasCustomName() || !isInSlayerQuest() || isSlayerArmorStandAlive()) return;
		String entityName = armorStand.getName().getString();

		// Slayer Boss
		BossFight boss = findBoss(armorStand);
		if (boss != null) {
			bossFight = boss;
			slayerQuest.onBossSpawn();
			if (!slayerQuest.sentBossAlert) {
				BossFight.alert();
				slayerQuest.sentBossAlert = true;
			}
			return;
		}

		// Slayer Miniboss
		assert CLIENT.player != null;
		if (armorStand.closerThan(CLIENT.player, 15) && slayerQuest.slayerType.isMiniboss(entityName, slayerQuest.slayerTier)) {
			slayerQuest.onMinibossSpawn(armorStand);
		}
	}

	/**
	 * Resolves a {@link BossFight} from the given entity, or returns {@code null} if it is not related to a Slayer boss.
	 *
	 * <p>If {@code entity} is an armorStand and is the playerName armorStand, search for bossName.
	 * if it's the bossName armorStand, search for playerName, in this case the bossFight is treated as owned by the player.</p>
	 *
	 * <p>If {@code entity} is a mob, search for bossName and playerName (Optional) armorStands.
	 * if playerName is not found, the bossfight is treated as not owned by the player.</p>
	 *
	 * @param entity an armor stand or mob entity
	 * @return the resolved {@link BossFight}, or {@code null} if none could be resolved
	 */
	private static @Nullable BossFight findBoss(Entity entity) {
		String username = CLIENT.getUser().getName();

		ArmorStand bossStand = null;
		Entity boss;
		SlayerType slayerType = null;
		SlayerTier slayerTier = null;
		boolean playerBoss = false;

		// If entity is ArmorStand, check if it's player's or boss's name armorStand
		if (entity instanceof ArmorStand armorStand) {
			String entityName = armorStand.getName().getString();
			Matcher entityMatcher = SLAYER_PATTERN.matcher(entityName);
			boolean isPlayerStand = entityName.contains(username);
			boolean isBossStand = entityMatcher.find() && isValidBossArmorStand(armorStand);

			if (!isBossStand && !isPlayerStand) return null;

			for (ArmorStand stand : getEntityArmorStands(entity, 1.5f)) {
				String standName = stand.getName().getString();

				// If it's boss's name stand => search for player's name stand
				if (isBossStand && standName.contains(username)) {
					String bossName = entityMatcher.group(1);
					slayerType = SlayerType.fromBossName(bossName);
					slayerTier = SlayerTier.valueOf(entityMatcher.group(2), bossName);
					bossStand = armorStand;
					playerBoss = true;
					break;
				}

				// If it's player's name stand => search for boss's name stand
				if (isPlayerStand) {
					Matcher matcher = SLAYER_PATTERN.matcher(standName);
					if (matcher.find() && isValidBossArmorStand(stand)) {
						String bossName = matcher.group(1);
						slayerType = SlayerType.fromBossName(bossName);
						slayerTier = SlayerTier.valueOf(matcher.group(2), bossName);
						bossStand = stand;
						playerBoss = true;
						break;
					}
				}
			}
			boss = (slayerType != null) ? findClosestMobEntity(slayerType.mobType, armorStand) : null;
		} else {
			// Entity is Mob - find boss info from nearby armor stands
			boss = entity;
			for (ArmorStand stand : getEntityArmorStands(entity, 1.5f)) {
				String standName = stand.getName().getString();
				Matcher matcher = SLAYER_PATTERN.matcher(standName);
				if (matcher.find() && isValidBossArmorStand(stand)) {
					bossStand = stand;
					String bossName = matcher.group(1);
					slayerType = SlayerType.fromBossName(bossName);
					slayerTier = SlayerTier.valueOf(matcher.group(2), bossName);
				} else if (standName.contains(username)) {
					playerBoss = true;
				}
			}
		}

		return bossStand != null && boss != null && slayerType != null
				? new BossFight(bossStand, boss, playerBoss, slayerType, slayerTier) : null;
	}

	/**
	 * Gets nearby {@link ArmorStand} instances with custom names around the given entity.
	 *
	 * @param entity  the entity to search around
	 * @param expandY how much to expand the search bounding box vertically (Y axis)
	 * @return a list of nearby custom-named armor stands
	 */
	public static List<ArmorStand> getEntityArmorStands(Entity entity, float expandY) {
		return entity.level().getEntities(entity, entity.getBoundingBox().inflate(0.1F, expandY, 0.1F), x -> x instanceof ArmorStand && x.hasCustomName())
				.stream()
				.map(e -> (ArmorStand) e)
				.toList();
	}

	/**
	 * <p> Finds the closest matching Entity for the armorStand using entityType and armorStand age difference to filter
	 * out impossible candidates, returning the closest entity of those remaining in the search box by block distance </p>
	 *
	 * @param entityType the entity type of the Slayer (i.e. ZombieEntity.class)
	 * @param armorStand the entity that contains the display name of the Slayer (mini)boss
	 * @implNote This method is not perfect. Possible improvements could be sort by x and z distance only (ignore y difference).
	 */
	private static @Nullable <T extends Entity> T findClosestMobEntity(EntityType<T> entityType, ArmorStand armorStand) {
		List<T> mobEntities = armorStand.level().getEntities(entityType, armorStand.getBoundingBox().inflate(0, 1.5f, 0), Entity::isAlive);
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
	 * Returns whether the given entity is a slayer miniboss or boss and should be highlighted based on the given highlight type.
	 */
	public static boolean shouldGlow(Entity entity, SlayersConfig.HighlightSlayerEntities highlightType) {
		if (SkyblockerConfigManager.get().slayers.highlightMinis == highlightType && isInSlayerQuest() && slayerQuest.minibosses.contains(entity)) return true;
		return SkyblockerConfigManager.get().slayers.highlightBosses == highlightType && isSelectedBoss(entity.getUUID());
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
	 * Gets the current Boss Fight state.
	 *
	 * @return The BossFight instance, or null if no boss fight is active.
	 */
	public static @Nullable BossFight getBossFight() {
		return bossFight;
	}

	/**
	 * Checks if the player is currently in a Slayer Quest.
	 * Note: This does not check whether a boss has spawned.
	 *
	 * @return {@code true} if the player is in a Slayer Quest; {@code false} otherwise.
	 */
	public static boolean isInSlayerQuest() {
		return slayerQuest != null && slayerQuest.active;
	}

	/**
	 * Checks whether the player is currently fighting a Slayer boss.
	 *
	 *  @return {@code true} if a slayer boss fight is active and the boss is alive; {@code false} otherwise.
	 */
	public static boolean isFightingSlayer() {
		return bossFight != null && bossFight.boss.isAlive();
	}

	/**
	 * Checks whether the player is currently fighting their own Slayer boss.
	 *
	 * @return {@code true} if the player is fighting their own slayer boss; {@code false} otherwise
	 */
	public static boolean isFightingOwnedSlayer() {
		//noinspection DataFlowIssue
		return isFightingSlayer() && bossFight.playerBoss;
	}

	/**
	 * Checks whether the currently tracked Slayer boss fight matches the given {@link SlayerType}.
	 *
	 * @param slayerType the slayer type to match against the current boss fight
	 * @return {@code true} if a boss fight is present and its slayer type matches; {@code false} otherwise
	 */
	public static boolean isFightingSlayerType(SlayerType slayerType) {
		return bossFight != null && bossFight.slayerType.equals(slayerType);
	}

	/**
	 * Checks whether the given UUID belongs to the currently tracked Slayer boss.
	 *
	 * @param uuid the UUID to compare against the currently tracked boss entity
	 * @return {@code true} if a boss fight is present and the boss UUID matches; {@code false} otherwise
	 */
	public static boolean isSelectedBoss(UUID uuid) {
		return bossFight != null && bossFight.boss.getUUID().equals(uuid);
	}

	/**
	 * Gets the armor stand entity associated with the Slayer Boss.
	 *
	 * @return The armor stand entity, or null if no boss fight is active.
	 */
	public static @Nullable ArmorStand getSlayerArmorStand() {
		//noinspection DataFlowIssue
		return isFightingSlayer() ? bossFight.armorStand : null;
	}

	/**
	 * Checks whether the armor stand associated with the currently tracked Slayer boss is still valid/alive.
	 *
	 * @return {@code true} if the tracked slayer boss armor stand is still valid; {@code false} otherwise
	 */
	public static boolean isSlayerArmorStandAlive() {
		//noinspection DataFlowIssue
		return isFightingOwnedSlayer() && isValidBossArmorStand(bossFight.armorStand);
	}

	public static boolean isValidBossArmorStand(ArmorStand armorStandEntity) {
		return armorStandEntity.isAlive() && !armorStandEntity.getName().getString().endsWith(" 0❤");
	}

	public static class BossFight {
		public final ArmorStand armorStand;
		public final Entity boss;
		public final boolean playerBoss;
		public final SlayerType slayerType;
		public final SlayerTier slayerTier;

		private BossFight(ArmorStand armorStand, Entity boss, boolean playerBoss, SlayerType slayerType, SlayerTier slayerTier) {
			this.armorStand = armorStand;
			this.boss = boss;
			this.playerBoss = playerBoss;
			this.slayerType = slayerType;
			this.slayerTier = slayerTier;
		}

		private static void alert() {
			if (SkyblockerConfigManager.get().slayers.bossSpawnAlert) {
				TitleContainer.addTitleAndPlaySound(BOSS_SPAWN, 20);
			}
		}

		public static void remove() {
			SlayerManager.bossFight = null;
			ENTITIES_CACHE.clear();
		}
	}

	public static class SlayerQuest {
		public final SlayerType slayerType;
		public final SlayerTier slayerTier;
		public final List<ArmorStand> minibossesArmorStand = new ArrayList<>();
		public final List<Entity> minibosses = new ArrayList<>();
		public boolean active = true;
		public int level;
		public int xpRemaining;
		public int bossesNeeded;

		public @Nullable Instant bossSpawnTime;
		public @Nullable Instant bossDeathTime;
		public boolean bossSpawned;
		public boolean sentBossAlert;

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
			this.sentBossAlert = false;
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

		private void onMinibossSpawn(ArmorStand armorStand) {
			if (minibossesArmorStand.contains(armorStand)) return;
			minibossesArmorStand.add(armorStand);

			EntityType<?> minibossType = slayerType.getMinibossType(armorStand.getName().getString());
			Entity miniboss = findClosestMobEntity(minibossType, armorStand);
			if (miniboss == null) return;
			minibosses.add(miniboss);

			var slayersConfig = SkyblockerConfigManager.get().slayers;
			if (slayersConfig.miniBossSpawnAlert && slayersConfig.alertOtherMinibosses) {
				MutableComponent armorStandName = (MutableComponent) armorStand.getCustomName();
				Title title = slayersConfig.showMiniBossNameInAlert && armorStandName != null ? new Title(armorStandName) : MINIBOSS_SPAWN;
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
