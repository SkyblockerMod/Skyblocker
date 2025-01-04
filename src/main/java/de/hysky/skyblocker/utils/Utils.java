package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.util.UndashedUuid;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.MessageHandlerAccessor;
import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
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
	private static final Pattern PURSE = Pattern.compile("(Purse|Piggy): (?<purse>[0-9,.]+)( \\((?<change>[+\\-][0-9,.]+)\\))?");
    private static boolean isOnHypixel = false;
    private static boolean isOnSkyblock = false;

    /**
     * The player's rank.
     */
    @NotNull
    private static RankType rank = PackageRank.NONE;
    /**
     * Current Skyblock location (from the Mod API)
     */
    @NotNull
    private static Location location = Location.UNKNOWN;
    /**
     * The profile name parsed from the player list.
     */
    @NotNull
    private static String profile = "";
    /**
     * The profile id parsed from the chat.
     */
    @NotNull
    private static String profileId = "";
    /**
     * The server from which we last received the profile id message from.
     */
    @NotNull
    private static int profileIdRequest = 0;
    /**
     * The following fields store data returned from the Mod API: {@link #environment}, {@link #server}, {@link #gameType}, {@link #locationRaw}, and {@link #map}.
     */
    @SuppressWarnings("JavadocDeclaration")
    @NotNull
    private static Environment environment = Environment.PRODUCTION;
    @NotNull
    private static String server = "";
    @NotNull
    private static String gameType = "";
    @NotNull
    private static String locationRaw = "";
    @NotNull
    private static String map = "";
    @NotNull
    public static double purse = 0;

	private static boolean firstProfileUpdate = true;

    /**
     * @implNote The parent text will always be empty, the actual text content is inside the text's siblings.
     */
    public static final ObjectArrayList<Text> TEXT_SCOREBOARD = new ObjectArrayList<>();
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
        return location == Location.DWARVEN_MINES || location == Location.GLACITE_MINESHAFT;
    }

    public static boolean isInTheRift() {
        return location == Location.THE_RIFT;
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

    public static boolean isInModernForagingIsland() {
        return location == Location.MODERN_FORAGING_ISLAND;
    }

    /**
     * @return the profile parsed from the player list.
     */
    @NotNull
    public static String getProfile() {
        return profile;
    }

    @NotNull
    public static String getProfileId() {
        return profileId;
    }

    /**
     * @return the location parsed from the Mod API.
     */
    @NotNull
    public static Location getLocation() {
        return location;
    }

    /**
     * Can be used to restrict features to being active only on the Alpha network.
     *
     * @return the current environment parsed from the Mod API.
     */
    @NotNull
    public static Environment getEnvironment() {
        return environment;
    }

    /**
     * @return the server parsed from the Mod API.
     */
    @NotNull
    public static String getServer() {
        return server;
    }

    /**
     * @return the game type parsed from the Mod API.
     */
    @NotNull
    public static String getGameType() {
        return gameType;
    }

    /**
     * @return the location raw parsed from the the Mod API.
     */
    @NotNull
    public static String getLocationRaw() {
        return locationRaw;
    }

    /**
     * @return the map parsed from the Mod API.
     */
    @NotNull
    public static String getMap() {
        return map;
    }

    /**
     * @return the player's rank
     */
    @NotNull
    public static RankType getRank() {
        return rank;
    }

    @Init
    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(Utils::onChatMessage);
        ClientReceiveMessageEvents.GAME_CANCELED.register(Utils::onChatMessage); // Somehow this works even though onChatMessage returns a boolean
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
        MinecraftClient client = MinecraftClient.getInstance();
        updateScoreboard(client);
        updatePlayerPresence(client);
        updateFromPlayerList(client);
    }

    /**
     * Updates {@link #isOnSkyblock} if in a development environment and {@link #isOnHypixel} in all environments.
     */
    private static void updatePlayerPresence(MinecraftClient client) {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        if (client.world == null || client.isInSingleplayer()) {
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

    private static boolean isConnectedToHypixel(MinecraftClient client) {
        String serverAddress = (client.getCurrentServerEntry() != null) ? client.getCurrentServerEntry().address.toLowerCase() : "";
        String serverBrand = (client.player != null && client.player.networkHandler != null && client.player.networkHandler.getBrand() != null) ? client.player.networkHandler.getBrand() : "";

        return (!serverAddress.isEmpty() && serverAddress.equalsIgnoreCase(ALTERNATE_HYPIXEL_ADDRESS)) || serverAddress.contains("hypixel.net") || serverAddress.contains("hypixel.io") || serverBrand.contains("Hypixel BungeeCord");
    }

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

    private static void updateScoreboard(MinecraftClient client) {
        try {
            TEXT_SCOREBOARD.clear();
            STRING_SCOREBOARD.clear();

            ClientPlayerEntity player = client.player;
            if (player == null) return;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            ObjectArrayList<Text> textLines = new ObjectArrayList<>();
            ObjectArrayList<String> stringLines = new ObjectArrayList<>();

            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                //Limit to just objectives displayed in the scoreboard (specifically sidebar objective)
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());

                    if (team != null) {
                        Text textLine = Text.empty().append(team.getPrefix().copy()).append(team.getSuffix().copy());
                        String strLine = team.getPrefix().getString() + team.getSuffix().getString();

                        if (!strLine.trim().isEmpty()) {
                            String formatted = Formatting.strip(strLine);

                            textLines.add(textLine);
                            stringLines.add(formatted);
                        }
                    }
                }
            }

            if (objective != null) {
                stringLines.add(objective.getDisplayName().getString());
                textLines.add(Text.empty().append(objective.getDisplayName().copy()));

                Collections.reverse(stringLines);
                Collections.reverse(textLines);
            }

            TEXT_SCOREBOARD.addAll(textLines);
            STRING_SCOREBOARD.addAll(stringLines);
            Utils.updatePurse();
			SlayerManager.getSlayerBossInfo(true);
        } catch (NullPointerException e) {
            //Do nothing
        }
    }

	public static void updatePurse() {
		STRING_SCOREBOARD.stream().filter(s -> s.contains("Piggy:") || s.contains("Purse:")).findFirst().ifPresent(purseString -> {
			Matcher matcher = PURSE.matcher(purseString);
			if (matcher.find()) {
				double newPurse = Double.parseDouble(matcher.group("purse").replaceAll(",", ""));
				double changeSinceLast = newPurse - Utils.purse;
				if (changeSinceLast == 0) return;
				SkyblockEvents.PURSE_CHANGE.invoker().onPurseChange(changeSinceLast, PurseChangeCause.getCause(changeSinceLast));
				Utils.purse = newPurse;
			}
		});
	}

	private static void updateFromPlayerList(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return;
        }
        for (PlayerListEntry playerListEntry : client.getNetworkHandler().getPlayerList()) {
            if (playerListEntry.getDisplayName() == null) {
                continue;
            }
            String name = playerListEntry.getDisplayName().getString();
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
        map = "";
    }

    private static void onPacket(HypixelS2CPacket packet) {
        switch (packet) {
            case HelloS2CPacket(var environment) -> {
                Utils.environment = environment;

                //Request the player's rank information
                HypixelNetworking.sendPlayerInfoC2SPacket(1);
            }

            case LocationUpdateS2CPacket(var serverName, var serverType, var _lobbyName, var mode, var map) -> {
                Utils.server = serverName;
                String previousServerType = Utils.gameType;
                Utils.gameType = serverType.orElse("");
                Utils.locationRaw = mode.orElse("");
                Utils.location = Location.from(locationRaw);
                Utils.map = map.orElse("");

                SkyblockEvents.LOCATION_CHANGE.invoker().onSkyblockLocationChange(location);

                if (Utils.gameType.equals("SKYBLOCK")) {
                    isOnSkyblock = true;
                    tickProfileId();
					SlayerManager.getSlayerInfoOnJoin();

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

                ClientPlayerEntity player = MinecraftClient.getInstance().player;

                if (player != null) {
                    player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.utils.locationUpdateError").formatted(Formatting.RED)), false);
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
		        if (requestId == profileIdRequest) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/profileid", true);
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
    public static boolean onChatMessage(Text text, boolean overlay) {
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
                } else if (firstProfileUpdate) {
					SkyblockEvents.PROFILE_INIT.invoker().onSkyblockProfileInit(profileId);
	                firstProfileUpdate = false;
                }
            }
        }

        return true;
    }

    /**
     * Used to avoid triggering things like chat rules or chat listeners infinitely, do not use otherwise.
     * <p>
     * Bypasses MessageHandler#onGameMessage
     */
    public static void sendMessageToBypassEvents(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();

        client.inGameHud.getChatHud().addMessage(message);
        ((MessageHandlerAccessor) client.getMessageHandler()).invokeAddToChatLog(message, Instant.now());
        client.getNarratorManager().narrateSystemMessage(message);
    }

	public static UUID getUuid() {
		return MinecraftClient.getInstance().getSession().getUuidOrNull();
	}

    public static String getUndashedUuid() {
        return UndashedUuid.toString(getUuid());
    }
}
