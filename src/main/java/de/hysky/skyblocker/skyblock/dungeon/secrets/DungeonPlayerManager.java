package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class DungeonPlayerManager {
	/**
	 * Match a player entry.
	 * Group 1: name
	 * Group 2: class (or literal "EMPTY" pre dungeon start)
	 * Group 3: level (or nothing, if pre dungeon start)
	 * This regex filters out the ironman icon as well as rank prefixes and emblems
	 * \[\d*\] (?:\[[A-Za-z]+\] )?(?&lt;name&gt;[A-Za-z0-9_]*) (?:.* )?\((?&lt;class&gt;\S*) ?(?&lt;level&gt;[LXVI]*)\)
	 */
	public static final Pattern PLAYER_TAB_PATTERN = Pattern.compile("\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\\((?<class>\\S*) ?(?<level>[LXVI]*)\\)");
	/**
	 * Use an array to ensure order, since the order of players in the player list is used to determine which decoration corresponds to which player in the dungeon map.
	 */
	private static final @Nullable DungeonPlayer[] players = new DungeonPlayer[5];

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DungeonPlayerManager::updatePlayers, 1);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
	}

	public static @Nullable DungeonPlayer[] getPlayers() {
		return players;
	}

	public static Optional<DungeonPlayer> getPlayer(String name) {
		return Arrays.stream(players).filter(Objects::nonNull).filter(p -> p.name.equals(name)).findAny();
	}

	public static DungeonClass getClassFromPlayer(PlayerEntity player) {
		return getClassFromPlayer(player.getGameProfile().getName());
	}

	public static DungeonClass getClassFromPlayer(String name) {
		return getPlayer(name).map(DungeonPlayer::dungeonClass).orElse(DungeonClass.UNKNOWN);
	}

	private static void updatePlayers() {
		if (!Utils.isInDungeons() && !DungeonManager.isClearingDungeon() && !DungeonManager.isInBoss()) return;

		for (int i = 0; i < 5; i++) {
			Matcher matcher = getPlayerFromTab(i + 1);

			if (matcher == null) {
				players[i] = null;
				continue;
			}

			String name = matcher.group("name");
			DungeonClass dungeonClass = DungeonClass.from(matcher.group("class"));

			if (players[i] != null && players[i].name.equals(name)) {
				players[i].update(dungeonClass);
			} else {
				players[i] = new DungeonPlayer(name, dungeonClass);
			}
		}
	}

	private static void reset() {
		Arrays.fill(players, null);
	}

	public static Matcher getPlayerFromTab(@Range(from = 1, to = 5) int index) {
		return PlayerListManager.regexAt(1 + (index - 1) * 4, PLAYER_TAB_PATTERN);
	}

	public static class DungeonPlayer {
		private @Nullable UUID uuid;
		private final @NotNull String name;
		private @NotNull DungeonClass dungeonClass;
		private boolean alive;

		public DungeonPlayer(@NotNull String name, @NotNull DungeonClass dungeonClass) {
			assert MinecraftClient.getInstance().world != null;
			this.uuid = findPlayerUuid(name);
			this.name = name;
			update(dungeonClass);

			// Pre-fetches game profiles for rendering skins in the leap overlay and fancy dungeon map.
			CompletableFuture.runAsync(() -> MinecraftClient.getInstance().getSessionService().fetchProfile(uuid, false));
		}

		private static @Nullable UUID findPlayerUuid(@NotNull String name) {
			return StreamSupport.stream(MinecraftClient.getInstance().world.getEntities().spliterator(), false)
					.filter(PlayerEntity.class::isInstance)
					.map(PlayerEntity.class::cast)
					.filter(player -> player.getGameProfile().getName().equals(name))
					.findAny()
					.map(PlayerEntity::getUuid)
					.orElse(null);
		}

		private void update(DungeonClass dungeonClass) {
			this.dungeonClass = dungeonClass;
			alive = dungeonClass != DungeonClass.UNKNOWN;
		}

		public @Nullable UUID uuid() {
			if (uuid == null) return uuid = findPlayerUuid(name); // Try to find the UUID. This shouldn't really happen, but this can act as failsafe.
			return uuid;
		}

		public @NotNull String name() {
			return name;
		}

		public @NotNull DungeonClass dungeonClass() {
			return dungeonClass;
		}

		public boolean alive() {
			return alive;
		}
	}
}
