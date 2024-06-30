package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

/**
 * Manager for Crystal Hollows waypoints that handles {@link #update() location detection},
 * {@link #extractLocationFromMessage(Text, Boolean) waypoints receiving}, {@link #shareWaypoint(String) sharing},
 * {@link #registerWaypointLocationCommands(CommandDispatcher, CommandRegistryAccess) commands}, and
 * {@link #render(WorldRenderContext) rendering}.
 */
public class CrystalsLocationsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    /**
     * A look-up table to convert between location names and waypoint in the {@link MiningLocationLabel.CrystalHollowsLocationsCategory} values.
     */
    private static final Map<String, MiningLocationLabel.CrystalHollowsLocationsCategory> WAYPOINT_LOCATIONS = Arrays.stream(MiningLocationLabel.CrystalHollowsLocationsCategory.values()).collect(Collectors.toMap(MiningLocationLabel.CrystalHollowsLocationsCategory::getName, Function.identity()));
    private static final Pattern TEXT_CWORDS_PATTERN = Pattern.compile("([0-9][0-9][0-9])\\D*([0-9][0-9][0-9]?)\\D*([0-9][0-9][0-9])");
    private static final int REMOVE_UNKNOWN_DISTANCE = 50;

    protected static Map<String, MiningLocationLabel> activeWaypoints = new HashMap<>();
    protected static List<String> verifiedWaypoints = new ArrayList<>();

    public static void init() {
        // Crystal Hollows Waypoints
        Scheduler.INSTANCE.scheduleCyclic(CrystalsLocationsManager::update, 40);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrystalsLocationsManager::render);
        ClientReceiveMessageEvents.GAME.register(CrystalsLocationsManager::extractLocationFromMessage);
        ClientCommandRegistrationCallback.EVENT.register(CrystalsLocationsManager::registerWaypointLocationCommands);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());

        // Nucleus Waypoints
        WorldRenderEvents.AFTER_TRANSLUCENT.register(NucleusWaypoints::render);
    }

    private static void extractLocationFromMessage(Text message, Boolean overlay) {
        String text = Formatting.strip(message.getString());
        if (!SkyblockerConfigManager.get().mining.crystalsWaypoints.findInChat || !Utils.isInCrystalHollows() || overlay || text == null) {
            return;
        }
        try {
            //make sure that it is only reading user messages and not from skyblocker
            if (text.contains(":") && !text.startsWith(Constants.PREFIX.get().getString())) {
                String userMessage = text.split(":", 2)[1];

                //get the message text
                Matcher matcher = TEXT_CWORDS_PATTERN.matcher(userMessage);
                //if there are coordinates in the message try to get them and what they are talking about
                if (matcher.find()) {
                    BlockPos blockPos = new BlockPos(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
                    String location = blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
                    //if position is not in the hollows do not add it
                    if (!checkInCrystals(blockPos)) {
                        return;
                    }

                    //see if there is a name of a location to add to this
                    for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
                        if (Arrays.stream(waypointLocation.toLowerCase().split(" ")).anyMatch(word -> userMessage.toLowerCase().contains(word))) { //check if contains a word of location
                            //all data found to create waypoint
                            addCustomWaypoint(waypointLocation, blockPos);
                            return;
                        }
                    }

                    //if the location is not found ask the user for the location (could have been in a previous chat message)
                    if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                        return;
                    }

                    CLIENT.player.sendMessage(getLocationInputText(location), false);
                }
            }

        } catch (Exception e) {
            LOGGER.error("[Skyblocker Crystals Locations Manager] Encountered an exception while extracing a location from a chat message!", e);
        }

        //move waypoint to be more accurate based on locational chat messages if not already verifed
        if (CLIENT.player != null && SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled) {
            for (MiningLocationLabel.CrystalHollowsLocationsCategory waypointLocation : WAYPOINT_LOCATIONS.values()) {
                String waypointLinkedMessage = waypointLocation.getLinkedMessage();
                String waypointName = waypointLocation.getName();
                if (waypointLinkedMessage != null && text.contains(waypointLinkedMessage) && !verifiedWaypoints.contains(waypointName)) {
                    addCustomWaypoint(waypointLocation.getName(), CLIENT.player.getBlockPos());
                    verifiedWaypoints.add(waypointName);
                }
            }
        }
    }

    protected static Boolean checkInCrystals(BlockPos pos) {
        //checks if a location is inside crystal hollows bounds
        return pos.getX() >= 202 && pos.getX() <= 823
                && pos.getZ() >= 202 && pos.getZ() <= 823
                && pos.getY() >= 31 && pos.getY() <= 188;
    }

    private static void registerWaypointLocationCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("crystalWaypoints")
                        .then(literal("add")
                                .then(argument("pos", BlockPosArgumentType.blockPos())
                                        .then(argument("place", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> suggestMatching(WAYPOINT_LOCATIONS.keySet(), builder))
                                                .executes(context -> addWaypointFromCommand(context.getSource(), getString(context, "place"), context.getArgument("pos", PosArgument.class)))
                                        )
                                ))
                        .then(literal("share")
                                .then(argument("place", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestMatching(WAYPOINT_LOCATIONS.keySet(), builder))
                                        .executes(context -> shareWaypoint(getString(context, "place")))
                                )
                        )
                        .then(literal("remove")
                                .then(argument("place", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestMatching(WAYPOINT_LOCATIONS.keySet(), builder))
                                        .executes(context -> removeWaypoint(getString(context, "place")))
                                )
                        )
                )
        );
    }

    protected static Text getSetLocationMessage(String location, BlockPos blockPos) {
        MutableText text = Constants.PREFIX.get();
        text.append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.addedWaypoint"));
        int locationColor = WAYPOINT_LOCATIONS.get(location).getColor();
        text.append(Text.literal(location).withColor(locationColor));
        text.append(Text.literal(" ").append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.addedWaypoint.at")));
        text.append(Text.literal(" : " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ() + "."));

        return text;
    }

    private static Text getLocationInputText(String location) {
        MutableText text = Constants.PREFIX.get();

        for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
            int locationColor = WAYPOINT_LOCATIONS.get(waypointLocation).getColor();
            text.append(Text.literal("[" + waypointLocation + "]").withColor(locationColor).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker crystalWaypoints add " + location + " " + waypointLocation))));
        }

        return text;
    }

    public static int addWaypointFromCommand(FabricClientCommandSource source, String place, PosArgument location) {
        // TODO Less hacky way with custom ClientBlockPosArgumentType
        BlockPos blockPos = location.toAbsoluteBlockPos(new ServerCommandSource(null, source.getPosition(), source.getRotation(), null, 0, null, null, null, null));

        if (WAYPOINT_LOCATIONS.containsKey(place)) {
            addCustomWaypoint(place, blockPos);

            //tell the client it has done this
            if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                return 0;
            }

            CLIENT.player.sendMessage(getSetLocationMessage(place, blockPos), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int shareWaypoint(String place) {
        if (activeWaypoints.containsKey(place)) {
            Vec3d pos = activeWaypoints.get(place).centerPos();
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + " " + place + ": " + (int) pos.getX() + ", " + (int) pos.getY() + ", " + (int) pos.getZ());
        } else {
            //send fail message
            if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                return 0;
            }
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFail").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int removeWaypoint(String place) {
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
            return 0;
        }
        if (activeWaypoints.containsKey(place)) {
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.removeSuccess").formatted(Formatting.GREEN)).append(Text.literal(place).withColor(WAYPOINT_LOCATIONS.get(place).getColor())), false);
            activeWaypoints.remove(place);
            verifiedWaypoints.remove(place);
        } else {
            //send fail message
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.mining.crystalsWaypoints.removeFail").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }


    protected static void addCustomWaypoint(String waypointName, BlockPos pos) {
        removeUnknownNear(pos);
        MiningLocationLabel.CrystalHollowsLocationsCategory category = WAYPOINT_LOCATIONS.get(waypointName);
        MiningLocationLabel waypoint = new MiningLocationLabel(category, pos);
        activeWaypoints.put(waypointName, waypoint);
    }

    /**
     * Removes unknown waypoint from active waypoints if it's close to a location
     * @param location center location
     */
    private static void removeUnknownNear(BlockPos location) {
        String name = MiningLocationLabel.CrystalHollowsLocationsCategory.UNKNOWN.getName();
        MiningLocationLabel unknownWaypoint =  activeWaypoints.getOrDefault(name, null);
        if (unknownWaypoint != null) {
            double distance = unknownWaypoint.centerPos().distanceTo(location.toCenterPos());
            if (distance < REMOVE_UNKNOWN_DISTANCE) {
                activeWaypoints.remove(name);
            }
        }
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled) {
            for (MiningLocationLabel crystalsWaypoint : activeWaypoints.values()) {
                crystalsWaypoint.render(context);
            }
        }
    }

    private static void reset() {
        activeWaypoints.clear();
        verifiedWaypoints.clear();
    }

    public static void update() {
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null || !SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled || !Utils.isInCrystalHollows()) {
            return;
        }

        //get if the player is in the crystals
        String location = Utils.getIslandArea().substring(2);
        //if new location and needs waypoint add waypoint
        if (!location.equals("Unknown") && WAYPOINT_LOCATIONS.containsKey(location) && !activeWaypoints.containsKey(location)) {
            //add waypoint at player location
            BlockPos playerLocation = CLIENT.player.getBlockPos();
            addCustomWaypoint(location, playerLocation);
        }
    }
}
