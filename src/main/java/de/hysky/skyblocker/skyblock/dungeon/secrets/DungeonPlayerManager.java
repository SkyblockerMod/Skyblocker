package de.hysky.skyblocker.skyblock.dungeon.secrets;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Range;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class DungeonPlayerManager {
	/**
	 * Match a player entry.
	 * Group 1: name
	 * Group 2: class (or literal "EMPTY" pre dungeon start)
	 * Group 3: level (or nothing, if pre dungeon start)
	 * This regex filters out the ironman icon as well as rank prefixes and emblems
	 * \[\d*\] (?:\[[A-Za-z]+\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\((?<class>\S*) ?(?<level>[LXVI]*)\)
	 */
	public static final Pattern PLAYER_TAB_PATTERN = Pattern.compile("\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<name>[A-Za-z0-9_]*) (?:.* )?\\((?<class>\\S*) ?(?<level>[LXVI]*)\\)");
	private static final Object2ReferenceMap<String, DungeonClass> PLAYER_CLASSES = new Object2ReferenceOpenHashMap<>(5);

	@Init
	public static void init() {
		DungeonEvents.DUNGEON_LOADED.register(DungeonPlayerManager::onDungeonLoaded);
		DungeonEvents.DUNGEON_STARTED.register(DungeonPlayerManager::onDungeonStart);
	}

	public static DungeonClass getClassFromPlayer(PlayerEntity player) {
		return getClassFromPlayer(player.getGameProfile().getName());
	}

	public static DungeonClass getClassFromPlayer(String name) {
		return PLAYER_CLASSES.getOrDefault(name, DungeonClass.UNKNOWN);
	}

	/**
	 * Pre-fetches game profiles for rendering skins in the leap overlay and fancy dungeon map.
	 */
	private static void onDungeonLoaded() {
		assert MinecraftClient.getInstance().world != null;
		for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
			if (entity instanceof PlayerEntity player && Utils.STRING_SCOREBOARD.stream().anyMatch(s -> s.contains(player.getGameProfile().getName()))) {
				CompletableFuture.runAsync(() -> MinecraftClient.getInstance().getSessionService().fetchProfile(player.getUuid(), false));
			}
		}
	}

	private static void onDungeonStart() {
		reset();

		for (int i = 0; i < 5; i++) {
			Matcher matcher = getPlayerFromTab(i + 1);

			if (matcher != null) {
				String name = matcher.group("name");
				DungeonClass dungeonClass = DungeonClass.from(matcher.group("class"));

				if (dungeonClass != DungeonClass.UNKNOWN) PLAYER_CLASSES.put(name, dungeonClass);
			}
		}
	}

	private static void reset() {
		PLAYER_CLASSES.clear();
	}

	public static Matcher getPlayerFromTab(@Range(from = 1, to = 5) int index) {
		return PlayerListManager.regexAt(1 + (index - 1) * 4, PLAYER_TAB_PATTERN);
	}
}
