package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.ChatListenerAccessor;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.purse.PurseChangeCause;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.azureaaron.hmapi.data.rank.PackageRank;
import net.azureaaron.hmapi.data.rank.RankType;
import net.azureaaron.hmapi.data.server.Environment;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HelloS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.PlayerInfoS2CPacket;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility variables and methods for retrieving Skyblock related information.
 */
public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
	private static final String ALTERNATE_HYPIXEL_ADDRESS = System.getProperty("skyblocker.alternateHypixelAddress", "");

	private static final String PROFILE_PREFIX = "Profile: ";
	private static final String PROFILE_MESSAGE_PREFIX = "§aYou are playing on profile: §e";
	public static final String PROFILE_ID_PREFIX = "Profile ID: ";
	private static final String PROFILE_ID_SUGGEST_PREFIX = "CLICK THIS TO SUGGEST IT IN CHAT";
	private static final Pattern PURSE = Pattern.compile("(Purse|Piggy): (?<purse>[0-9,.]+)( \\((?<change>[+\\-][0-9,.]+)\\))?");
	private static final HolderLookup.Provider LOOKUP = VanillaRegistries.createLookup();
	private static boolean isOnHypixel = false;
	private static boolean isOnSkyblock = false;

	/**
	 * The player's rank.
	 */
	private static RankType rank = PackageRank.NONE;
	/**
	 * Current Skyblock location (from the Mod API)
	 */
	private static Location location = Location.UNKNOWN;
	/**
	 * Current Skyblock island area.
	 */
	private static Area area = Area.UNKNOWN;
	/**
	 * The profile name parsed from the player list.
	 */
	private static String profile = "";
	/**
	 * The profile id parsed from the chat.
	 */
	private static String profileId = "";
	/**
	 * The server from which we last received the profile id message from.
	 */
	private static int profileIdRequest = 0;
	private static int profileSuggestionMessages = Integer.MAX_VALUE / 2;
	/**
	 * The following fields store data returned from the Mod API: {@link #environment}, {@link #server}, {@link #gameType}, {@link #locationRaw}, and {@link #map}.
	 */
	@SuppressWarnings("JavadocDeclaration")
	private static Environment environment = Environment.PRODUCTION;
	private static String server = "";
	private static String gameType = "";
	private static String locationRaw = "";
	private static String map = "";
	public static double purse = 0;

	/**
	 * @implNote The parent text will always be empty, the actual text content is inside the text's siblings.
	 */
	public static final ObjectArrayList<Component> TEXT_SCOREBOARD = new ObjectArrayList<>();
	public static final ObjectArrayList<String> STRING_SCOREBOARD = new ObjectArrayList<>();

	public static boolean isOnHypixel() {
		return isOnHypixel;
	}

	public static boolean isOnSkyblock() {
		return isOnSkyblock;
	}

	public static boolean isInDungeons() {
		return location == Location.DUNGEON;
	}

	public static boolean isInCrystalHollows() {
		return location == Location.CRYSTAL_HOLLOWS;
	}

	public static boolean isInDwarvenMines() {
		return location == Location.DWARVEN_MINES || location == Location.GLACITE_MINESHAFTS;
	}

	public static boolean isInTheRift() {
		return location == Location.THE_RIFT;
	}

	public static boolean isInGarden() {
		return location == Location.GARDEN;
	}

	/**
	 * @return if the player is in the end island
	 */
	public static boolean isInTheEnd() {
		return location == Location.THE_END;
	}

	public static boolean isInKuudra() {
		return location == Location.KUUDRAS_HOLLOW;
	}

	public static boolean isInCrimson() {
		return location == Location.CRIMSON_ISLE;
	}

	public static boolean isInSpidersDen() {
		return location == Location.SPIDERS_DEN;
	}

	public static boolean isInFarm() {
		return location == Location.THE_FARMING_ISLAND;
	}

	public static boolean isInGalatea() { return location == Location.GALATEA; }

	public static boolean isInHub() { return location == Location.HUB; }

	public static boolean isInPrivateIsland() { return location == Location.PRIVATE_ISLAND; }

	public static boolean isInPark() { return location == Location.THE_PARK; }

	public static boolean isOnBingo() {
		return profile.endsWith("Ⓑ");
	}

	/**
	 * @return the profile parsed from the player list.
	 */
	public static String getProfile() {
		return profile;
	}

	public static String getProfileId() {
		return profileId;
	}

	/**
	 * @return the location parsed from the Mod API.
	 */
	public static Location getLocation() {
		return location;
	}

	/**
	 * <b>Note: Under no circumstances should you skip checking the location if you also need the area.</b>
	 *
	 * @return the area parsed from the scoreboard.
	 */
	public static Area getArea() {
		return area;
	}

	/**
	 * Can be used to restrict features to being active only on the Alpha network.
	 *
	 * @return the current environment parsed from the Mod API.
	 */
	public static Environment getEnvironment() {
		return environment;
	}

	/**
	 * @return the server parsed from the Mod API.
	 */
	public static String getServer() {
		return server;
	}

	/**
	 * @return the game type parsed from the Mod API.
	 */
	public static String getGameType() {
		return gameType;
	}

	/**
	 * @return the raw location from the Mod API.
	 */
	public static String getLocationRaw() {
		return locationRaw;
	}

	/**
	 * @return the map parsed from the Mod API.
	 */
	public static String getMap() {
		return map;
	}

	/**
	 * @return the player's rank
	 */
	public static RankType getRank() {
		return rank;
	}

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(Utils::onChatMessage);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onDisconnect());

		//Register Mod API stuff
		HypixelNetworking.registerToEvents(Util.make(new Object2IntOpenHashMap<>(), map -> map.put(LocationUpdateS2CPacket.ID, 1)));
		HypixelPacketEvents.HELLO.register(Utils::onPacket);
		HypixelPacketEvents.LOCATION_UPDATE.register(Utils::onPacket);
		HypixelPacketEvents.PLAYER_INFO.register(Utils::onPacket);
	}

	/**
	 * Updates all the fields stored in this class from the sidebar, and player list.
	 */
	public static void update() {
		Minecraft client = Minecraft.getInstance();
		updateScoreboard(client);
		updatePlayerPresence(client);
		updateFromPlayerList(client);
	}

	/**
	 * Updates {@link #isOnSkyblock} if in a development environment and {@link #isOnHypixel} in all environments.
	 */
	private static void updatePlayerPresence(Minecraft client) {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		if (client.level == null || client.isLocalServer()) {
			if (fabricLoader.isDevelopmentEnvironment()) { // Pretend we're always in skyblock when in dev
				isOnSkyblock = true;
			}
		}

		if (fabricLoader.isDevelopmentEnvironment() || isConnectedToHypixel(client)) {
			if (!isOnHypixel) {
				isOnHypixel = true;
			}
		} else if (isOnHypixel) {
			isOnHypixel = false;
		}
	}

	private static boolean isConnectedToHypixel(Minecraft client) {
		String serverAddress = (client.getCurrentServer() != null) ? client.getCurrentServer().ip.toLowerCase(Locale.ENGLISH) : "";
		String serverBrand = (client.player != null && client.player.connection != null && client.player.connection.serverBrand() != null) ? client.player.connection.serverBrand() : "";

		return (!serverAddress.isEmpty() && serverAddress.equalsIgnoreCase(ALTERNATE_HYPIXEL_ADDRESS)) || serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord");
	}

	/**
	 * @deprecated use type safe {@link #getArea()}.
	 */
	@Deprecated
	public static String getIslandArea() {
		try {
			for (String sidebarLine : STRING_SCOREBOARD) {
				if (sidebarLine.contains("⏣") || sidebarLine.contains("ф") /* Rift */) {
					return sidebarLine.strip();
				}
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to get location from sidebar", e);
		}
		return "Unknown";
	}

	public static double getPurse() {
		return purse;
	}

	public static int getBits() {
		int bits = 0;
		String bitsString = null;
		try {
			for (String sidebarLine : STRING_SCOREBOARD) {
				if (sidebarLine.contains("Bits")) bitsString = sidebarLine;
			}
			if (bitsString != null) {
				bits = Integer.parseInt(bitsString.replaceAll("[^0-9.]", "").strip());
			}
		} catch (IndexOutOfBoundsException e) {
			LOGGER.error("[Skyblocker] Failed to get bits from sidebar", e);
		}
		return bits;
	}

	private static void updateScoreboard(Minecraft client) {
		try {
			TEXT_SCOREBOARD.clear();
			STRING_SCOREBOARD.clear();

			ClientLevel world = client.level;
			if (world == null) return;

			Scoreboard scoreboard = world.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1));
			ObjectArrayList<Component> textLines = new ObjectArrayList<>();
			ObjectArrayList<String> stringLines = new ObjectArrayList<>();

			for (ScoreHolder scoreHolder : scoreboard.getTrackedPlayers()) {
				//Limit to just objectives displayed in the scoreboard (specifically sidebar objective)
				if (scoreboard.listPlayerScores(scoreHolder).containsKey(objective)) {
					PlayerTeam team = scoreboard.getPlayersTeam(scoreHolder.getScoreboardName());

					if (team != null) {
						Component textLine = Component.empty().append(team.getPlayerPrefix().copy()).append(team.getPlayerSuffix().copy());
						String strLine = team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString();

						if (!strLine.trim().isEmpty()) {
							String formatted = ChatFormatting.stripFormatting(strLine);

							textLines.add(textLine);
							stringLines.add(formatted);
						}
					}
				}
			}

			if (objective != null) {
				stringLines.add(objective.getDisplayName().getString());
				textLines.add(Component.empty().append(objective.getDisplayName().copy()));

				Collections.reverse(stringLines);
				Collections.reverse(textLines);
			}

			TEXT_SCOREBOARD.addAll(textLines);
			STRING_SCOREBOARD.addAll(stringLines);
			if (isOnSkyblock) {
				Utils.updatePurse();
				SlayerManager.checkSlayerQuest();
				updateArea();
			}
		} catch (NullPointerException e) {
			//Do nothing
		}
	}

	private static void updateArea() {
		String areaName = getIslandArea().replaceAll("[⏣ф]", "").strip();
		Area oldArea = area;
		area = Area.from(areaName);

		if (!oldArea.equals(area)) SkyblockEvents.AREA_CHANGE.invoker().onSkyblockAreaChange(area);
	}

	public static void updatePurse() {
		STRING_SCOREBOARD.stream().filter(s -> s.contains("Piggy:") || s.contains("Purse:")).findFirst().ifPresent(purseString -> {
			Matcher matcher = PURSE.matcher(purseString);
			if (matcher.find()) {
				try {
					double newPurse = Double.parseDouble(matcher.group("purse").replaceAll(",", ""));
					double changeSinceLast = newPurse - Utils.purse;
					if (changeSinceLast == 0) return;
					SkyblockEvents.PURSE_CHANGE.invoker().onPurseChange(changeSinceLast, PurseChangeCause.getCause(changeSinceLast));
					Utils.purse = newPurse;
				} catch (NumberFormatException e) {
					LOGGER.error("[Skyblocker] Failed to parse purse string. Input: '{}'", purseString, e);
				}
			}
		});
	}

	private static void updateFromPlayerList(Minecraft client) {
		if (client.getConnection() == null) {
			return;
		}
		for (PlayerInfo playerListEntry : client.getConnection().getOnlinePlayers()) {
			if (playerListEntry.getTabListDisplayName() == null) {
				continue;
			}
			String name = playerListEntry.getTabListDisplayName().getString();
			if (name.startsWith(PROFILE_PREFIX)) {
				profile = name.substring(PROFILE_PREFIX.length());
			}
		}
	}

	private static void onDisconnect() {
		if (isOnSkyblock) SkyblockEvents.LEAVE.invoker().onSkyblockLeave();

		isOnSkyblock = false;
		server = "";
		gameType = "";
		locationRaw = "";
		location = Location.UNKNOWN;
		area = Area.UNKNOWN;
		map = "";
	}

	private static void onPacket(HypixelS2CPacket packet) {
		switch (packet) {
			case HelloS2CPacket(var serverEnvironment) -> {
				environment = serverEnvironment;

				//Request the player's rank information
				HypixelNetworking.sendPlayerInfoC2SPacket(1);
			}

			case LocationUpdateS2CPacket(var serverName, var serverType, var _lobbyName, var mode, var mapName) -> {
				Utils.server = serverName;
				String previousServerType = Utils.gameType;
				Utils.gameType = serverType.orElse("");
				Utils.locationRaw = mode.orElse("");
				Utils.location = Location.from(locationRaw);
				Utils.map = mapName.orElse("");

				SkyblockEvents.LOCATION_CHANGE.invoker().onSkyblockLocationChange(location);

				if (Utils.gameType.equals("SKYBLOCK")) {
					isOnSkyblock = true;
					tickProfileId();

					if (!previousServerType.equals("SKYBLOCK")) SkyblockEvents.JOIN.invoker().onSkyblockJoin();
				} else if (previousServerType.equals("SKYBLOCK")) {
					isOnSkyblock = false;
					SkyblockEvents.LEAVE.invoker().onSkyblockLeave();
				}
			}

			case ErrorS2CPacket(var id, var error) when id.equals(LocationUpdateS2CPacket.ID) -> {
				server = "";
				gameType = "";
				locationRaw = "";
				location = Location.UNKNOWN;
				map = "";

				LocalPlayer player = Minecraft.getInstance().player;

				if (player != null) {
					player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.utils.locationUpdateError").withStyle(ChatFormatting.RED)), false);
				}

				LOGGER.error("[Skyblocker] Failed to update your current location! Some features of the mod may not work correctly :( - Error: {}", error);
			}

			case PlayerInfoS2CPacket(var playerRank, var packageRank, var monthlyPackageRank, var _prefix) -> {
				rank = RankType.getEffectiveRank(playerRank, packageRank, monthlyPackageRank);
			}

			default -> {} //Do Nothing
		}
	}

	/**
	 * After 8 seconds of having swapped servers we check if we've been sent the profile id message on
	 * this server and if we haven't then we send the /profileid command.
	 */
	private static void tickProfileId() {
		profileIdRequest++;

		Scheduler.INSTANCE.schedule(new Runnable() {
			private final int requestId = profileIdRequest;

			@Override
			public void run() {
				if (requestId == profileIdRequest) {
					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/profileid", true);
					profileSuggestionMessages = 0;
				}
			}
		}, 20 * 8); //8 seconds
	}

	/**
	 * Parses /locraw chat message and updates {@link #server}, {@link #gameType}, {@link #locationRaw}, {@link #map}
	 * and {@link #location}
	 *
	 * @param message json message from chat
	 * @deprecated Retained just in case the mod api doesn't work or gets disabled.
	 */
	@Deprecated
	private static void parseLocRaw(String message) {
		JsonObject locRaw = JsonParser.parseString(message).getAsJsonObject();

		if (locRaw.has("server")) {
			server = locRaw.get("server").getAsString();
		}
		if (locRaw.has("gametype")) {
			gameType = locRaw.get("gametype").getAsString();
			isOnSkyblock = gameType.equals("SKYBLOCK");
		}
		if (locRaw.has("mode")) {
			locationRaw = locRaw.get("mode").getAsString();
			location = Location.from(locationRaw);
		} else {
			location = Location.UNKNOWN;
		}
		if (locRaw.has("map")) {
			map = locRaw.get("map").getAsString();
		}
	}

	/**
	 * Parses the /locraw reply from the server and updates the player's profile id
	 *
	 * @return not display the message in chat if the command is sent by the mod
	 */
	public static boolean onChatMessage(Component text, boolean overlay) {
		if (overlay) return true;
		String message = text.getString();

		if (message.startsWith("{\"server\":") && message.endsWith("}")) {
			parseLocRaw(message);
		}

		if (isOnSkyblock) {
			if (message.startsWith(PROFILE_MESSAGE_PREFIX)) {
				profile = message.substring(PROFILE_MESSAGE_PREFIX.length()).split("§b")[0];
			} else if (message.startsWith(PROFILE_ID_PREFIX)) {
				String prevProfileId = profileId;
				profileId = message.substring(PROFILE_ID_PREFIX.length());
				profileIdRequest++;

				if (!prevProfileId.equals(profileId)) {
					SkyblockEvents.PROFILE_CHANGE.invoker().onSkyblockProfileChange(prevProfileId, profileId);
				}
			} else if (ChatFormatting.stripFormatting(message).startsWith(PROFILE_ID_SUGGEST_PREFIX)) {
				int suggestions = profileSuggestionMessages;
				profileSuggestionMessages++;

				return suggestions >= 2;
			}
		}

		return true;
	}

	/**
	 * Used to avoid triggering things like chat rules or chat listeners infinitely, do not use otherwise.
	 * <p>
	 * Bypasses MessageHandler#onGameMessage
	 */
	public static void sendMessageToBypassEvents(Component message) {
		Minecraft client = Minecraft.getInstance();

		client.gui.getChat().addMessage(message);
		((ChatListenerAccessor) client.getChatListener()).invokeLogSystemMessage(message, Instant.now());
		client.getNarrator().saySystemQueued(message);
	}

	public static UUID getUuid() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	public static String getUndashedUuid() {
		return UndashedUuid.toString(getUuid());
	}

	/**
	 * Tries to get the dynamic registry manager instance currently in use or else returns {@link #LOOKUP}
	 */
	public static HolderLookup.Provider getRegistryWrapperLookup() {
		Minecraft client = Minecraft.getInstance();
		// Null check on client for tests
		return client != null && client.getConnection() != null && client.getConnection().registryAccess() != null ? client.getConnection().registryAccess() : LOOKUP;
	}

	/**
	 * Parses an int from a string
	 * @param input the string to parse
	 * @return the int parsed or an empty optional if it failed
	 * @implNote Does not log the exception if thrown
	 */
	public static OptionalInt parseInt(String input) {
		try {
			return OptionalInt.of(Integer.parseInt(input));
		} catch (NumberFormatException e) {
			return OptionalInt.empty();
		}
	}

	/**
	 * Get players eye height from the servers point of view based on it's minecraft version
	 *
	 * @return offset from players pos to their eyes
	 */
	public static float getEyeHeight(Player player) {
		if (player == null || !player.isShiftKeyDown()) return 1.62f;
		//sneaking height is different depending on server
		return getLocation().isModern() ? 1.27f : 1.54f;
	}

	/**
	 * Used to prevent third-party resource packs from modifying resources they shouldn't be. This will "lose" the game.
	 */
	public static void checkForIllegalResourceModification(Identifier id, Resource resource, String error) {
		if (!resource.sourcePackId().equals(SkyblockerMod.NAMESPACE)) {
			LOGGER.error("!".repeat(50));
			LOGGER.error(LogUtils.FATAL_MARKER, error, id, resource.sourcePackId());
			LOGGER.error("!".repeat(50));
			Blaze3D.youJustLostTheGame();
		}
	}
}
