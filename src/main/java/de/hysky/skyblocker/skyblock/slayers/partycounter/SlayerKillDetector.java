package de.hysky.skyblocker.skyblock.slayers.partycounter;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SlayerKillDetector {
		private static final Logger LOGGER = LogUtils.getLogger();
		private static final Minecraft CLIENT = Minecraft.getInstance();

		private static final Pattern SPAWNED_BY_PATTERN = Pattern.compile("Spawned by: (.+)");
		private static final Pattern LOOT_SHARE_PATTERN = Pattern.compile("LOOT SHARE You received loot for assisting ([A-Za-z0-9_]+)!");
		private static final Pattern QUEST_FAILED_PATTERN = Pattern.compile("^\\s*(?:Your Slayer Quest has been cancelled!|SLAYER QUEST FAILED!)\\s*$");
		private static final Pattern PARTY_DISBAND_PATTERN = Pattern.compile("^\\s*(?:The party was disbanded|You have been kicked from the party|You left the party|The party has been disbanded|You are not currently in a party).*$", Pattern.CASE_INSENSITIVE);

		private static final Pattern SLAYER_BOSS_ONLY = Pattern.compile(
						"\\b(?:"
										+ "Revenant Horror|Atoned Horror|"
										+ "Tarantula Broodfather|Primordial Broodfather|"
										+ "Sven Packmaster|"
										+ "Voidgloom Seraph|"
										+ "Inferno Demonlord|"
										+ "Riftstalker Bloodfiend"
										+ ")\\b",
						Pattern.CASE_INSENSITIVE
		);

		private static final Pattern NOT_A_SLAYER_BOSS = Pattern.compile(
						"\\b(?:Voidling Extremist)\\b",
						Pattern.CASE_INSENSITIVE
		);

		private static final Set<Integer> PROCESSED_ARMOR_STANDS = new HashSet<>();
		private static long lastCleanupTime = 0;
		private static final long CLEANUP_INTERVAL = 30_000L;

		private SlayerKillDetector() {
		}

		public static void init() {
				ClientReceiveMessageEvents.ALLOW_GAME.register(SlayerKillDetector::onChatMessage);
		}

		@SuppressWarnings("SameReturnValue")
		private static boolean onChatMessage(Component text, boolean overlay) {
				if (overlay || !Utils.isOnSkyblock()) return true;
				if (!SkyblockerConfigManager.get().slayers.partySlayerCounter.enablePartyCounter) return true;

				String message = ChatFormatting.stripFormatting(text.getString());
				if (message == null) return true;

				Matcher lootShareMatcher = LOOT_SHARE_PATTERN.matcher(message);
				if (lootShareMatcher.find()) {
						handleLootShare(lootShareMatcher.group(1).trim());
						return true;
				}

				if (QUEST_FAILED_PATTERN.matcher(message).find()) {
						PartySlayerCounter.onBossFailed();
						return true;
				}

				if (PARTY_DISBAND_PATTERN.matcher(message).find()) {
						PartySlayerCounter.onPartyDisband();
				}

				return true;
		}

		private static void handleLootShare(String assistedPlayerName) {
				if (CLIENT.player == null) return;
				String localPlayer = CLIENT.getUser().getName();
				if (assistedPlayerName.equalsIgnoreCase(localPlayer)) return;

				if (!isConfirmedPartyMember(assistedPlayerName)) return;
				if (SkyblockerConfigManager.get().slayers.partySlayerCounter.counterMode == CounterMode.MANUAL) return;

				if (PartySlayerCounter.wasTrackingBoss(assistedPlayerName)) {
						PartySlayerCounter.onBossKilled(assistedPlayerName);
						LOGGER.info("[Skyblocker Party Counter] Counted kill for {} via loot share", assistedPlayerName);
				}
		}

		public static void tick() {
				if (!SkyblockerConfigManager.get().slayers.partySlayerCounter.enablePartyCounter) return;
				if (!PartyTracker.isInParty()) return;
				if (CLIENT.level == null || CLIENT.player == null) return;

				PartySlayerCounter.tick();

				long now = System.currentTimeMillis();
				if (now - lastCleanupTime > CLEANUP_INTERVAL) {
						PROCESSED_ARMOR_STANDS.clear();
						lastCleanupTime = now;
				}

				AABB searchBox = CLIENT.player.getBoundingBox().inflate(25);
				for (Entity entity : CLIENT.level.getEntities(CLIENT.player, searchBox)) {
						if (entity instanceof ArmorStand armorStand) {
								scanArmorStand(armorStand);
						}
				}
		}

		private static void scanArmorStand(ArmorStand armorStand) {
				if (!armorStand.hasCustomName() || armorStand.getCustomName() == null) return;

				int standId = armorStand.getId();

				String name = armorStand.getCustomName().getString();
				String cleanName = ChatFormatting.stripFormatting(name);
				if (cleanName == null) return;

				Matcher spawnerMatcher = SPAWNED_BY_PATTERN.matcher(cleanName);
				if (!spawnerMatcher.find()) return;

				String spawnerName = spawnerMatcher.group(1).trim();
				if (spawnerName.isEmpty()) return;
				if (CLIENT.player == null) return;

				String localPlayer = CLIENT.getUser().getName();
				if (spawnerName.equalsIgnoreCase(localPlayer)) return;

				if (!isConfirmedPartyMember(spawnerName)) return;

				Entity bossEntity = findBossEntity(armorStand);
				if (bossEntity != null) {
						PartySlayerCounter.onBossDetected(spawnerName, bossEntity.getUUID());
						if (!PROCESSED_ARMOR_STANDS.contains(standId)) {
								PROCESSED_ARMOR_STANDS.add(standId);
								LOGGER.debug("[Skyblocker Party Counter] Found boss for: {} (UUID: {})", spawnerName, bossEntity.getUUID());
						}
				}
		}

		private static Entity findBossEntity(ArmorStand spawnedByStand) {
				if (!isActualBoss(spawnedByStand)) return null;
				if (CLIENT.level == null) return null;

				AABB searchBox = new AABB(
								spawnedByStand.getX() - 2, spawnedByStand.getY() - 6, spawnedByStand.getZ() - 2,
								spawnedByStand.getX() + 2, spawnedByStand.getY() + 1, spawnedByStand.getZ() + 2
				);

				Entity closest = null;
				double closestDist = Double.MAX_VALUE;

				for (Entity entity : CLIENT.level.getEntities(spawnedByStand, searchBox)) {
						if (entity instanceof Mob) {
								double dist = entity.distanceToSqr(spawnedByStand);
								if (dist < closestDist) {
										closestDist = dist;
										closest = entity;
								}
						}
				}

				return closest;
		}

		private static boolean isActualBoss(ArmorStand spawnedByStand) {
				if (!spawnedByStand.hasCustomName()) return false;

				String baseName = ChatFormatting.stripFormatting(spawnedByStand.getCustomName().getString());
				if (baseName == null) return false;

				if (NOT_A_SLAYER_BOSS.matcher(baseName).find()) return false;
				if (SLAYER_BOSS_ONLY.matcher(baseName).find()) return true;

				if (CLIENT.level == null) return false;

				AABB nearby = spawnedByStand.getBoundingBox().inflate(2.0);
				for (Entity e : CLIENT.level.getEntities(spawnedByStand, nearby)) {
						if (!(e instanceof ArmorStand other) || !other.hasCustomName()) continue;
						if (!isSameTextStack(spawnedByStand, other)) continue;

						String otherName = ChatFormatting.stripFormatting(other.getCustomName().getString());
						if (otherName == null) continue;

						if (NOT_A_SLAYER_BOSS.matcher(otherName).find()) return false;
						if (SLAYER_BOSS_ONLY.matcher(otherName).find()) return true;
				}

				return false;
		}

		private static boolean isSameTextStack(ArmorStand a, ArmorStand b) {
				double dx = Math.abs(a.getX() - b.getX());
				double dz = Math.abs(a.getZ() - b.getZ());
				double dy = b.getY() - a.getY();
				return dx < 0.25 && dz < 0.25 && dy > -0.5 && dy < 4.0;
		}

		private static boolean isConfirmedPartyMember(String playerName) {
				if (CLIENT.level == null) return false;
				for (var player : CLIENT.level.players()) {
						if (player.getGameProfile().name().equalsIgnoreCase(playerName)) {
								return PartyTracker.isPartyMember(player.getUUID());
						}
				}
				return false;
		}

		public static void reset() {
				PROCESSED_ARMOR_STANDS.clear();
		}
}
